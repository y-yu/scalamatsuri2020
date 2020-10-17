package eff

import cats.Traverse
import dbio.DBIO
import cats.syntax.all._
import dbio.Session
import org.atnos.eff.Eff
import org.atnos.eff.|=
import infra.util.DB._
import org.atnos.eff.Continuation
import org.atnos.eff.Interpret
import org.atnos.eff.Interpreter
import org.atnos.eff.Member
import org.atnos.eff.NoFx

object DBIOEffect
  extends DBIOCreation
  with DBIOOps

trait DBIOTypes {
  sealed trait DBIOAction[A]
  case class Ask() extends DBIOAction[Session]
  case class Execute[A](
    value: A
  ) extends DBIOAction[A]

  type _dbio[R] = DBIOAction |= R
}

trait DBIOCreation extends DBIOTypes { self =>
  def fromDBIO[R: _dbio, A](
    dbio: DBIO[A]
  ): Eff[R, A] =
    for {
      session <- Eff.send[DBIOAction, R, Session](Ask())
      a <- Eff.send[DBIOAction, R, A](Execute(dbio.run(session)))
    } yield a

  implicit class FromDBIO[A](val dbio: DBIO[A]) {
    def fromDBIO[R: _dbio]: Eff[R, A] =
      self.fromDBIO(dbio)
  }
}

trait DBIOOps extends DBIOTypes {

  def runDBIO[R: _dbio, A](
    eff: Eff[R, A]
  )(implicit m: Member.Aux[DBIOAction, R, NoFx]): Either[Throwable, A] = {
    withTransaction { session =>
      Eff.run(
        Interpret.runInterpreter(eff)(
          new Interpreter[DBIOAction, NoFx, A, A] {
            def onPure(a: A): Eff[NoFx, A] =
              Eff.pure(a)

            def onEffect[X](
              x: DBIOAction[X],
              continuation: Continuation[NoFx, X, A]
            ): Eff[NoFx, A] = x match {
              case Ask() =>
                continuation(session)
              case Execute(v) =>
                continuation(v)
            }

            def onLastEffect[X](
              x: DBIOAction[X],
              continuation: Continuation[NoFx, X, Unit]
            ): Eff[NoFx, Unit] = x match {
              case Ask() =>
                continuation(session)
              case Execute(v) =>
                continuation(v)
            }

            def onApplicativeEffect[X, T[_] : Traverse](
              xs: T[DBIOAction[X]],
              continuation: Continuation[NoFx, T[X], A]
            ): Eff[NoFx, A] =
              continuation.apply(
                xs.map {
                  case Ask() => session
                  case Execute(v) => v
                }
              )
          }
        )
      )
    }
  }
}