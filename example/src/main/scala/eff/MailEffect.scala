package eff

import mail.Mail
import org.atnos.eff.Eff
import org.atnos.eff.Interpret
import org.atnos.eff.Interpreter
import org.atnos.eff.Member
import cats.syntax.all._
import org.atnos.eff.|=
import cats.Traverse
import infra.util.Mail.sendMail
import org.atnos.eff.Continuation
import org.atnos.eff.NoFx
import eff.DBIOEffect._

object MailEffect
  extends MailCreation
  with MailOps

trait MailTypes {
  sealed trait MailAction[A]
  case class Tell(
    mail: Mail
  ) extends MailAction[Unit]

  type _mail[R] = MailAction |= R
}

trait MailCreation extends MailTypes {
  def sendMailEff[R: _mail](
    mail: Mail
  ): Eff[R, Unit] = Eff.send[MailAction, R, Unit](Tell(mail))
}

trait MailOps extends MailTypes {
  def runMailAfterDBIO[R: _mail: _dbio, U, A](
    eff: Eff[R, A]
  )(
    implicit m1: Member.Aux[MailAction, R, U], m2: Member.Aux[DBIOAction, U, NoFx]
  ): Either[Throwable, A] = {
    val mailRemoved = Interpret.runInterpreter(eff)(new Interpreter[MailAction, U, A, (List[Mail], A)] {
      def onPure(a: A): Eff[U, (List[Mail], A)] =
        Eff.pure((Nil, a))

      def onEffect[X](
        x: MailAction[X],
        continuation: Continuation[U, X, (List[Mail], A)]
      ): Eff[U, (List[Mail], A)] =
        x match {
          case Tell(mail) =>
            // `Tell extends MailEffect[Unit]` so in this case `X` is `Unit`
            continuation(()).map {
              case (mails, a) => (mail :: mails, a)
            }
        }

      def onLastEffect[X](
        x: MailAction[X],
        continuation: Continuation[U, X, Unit]
      ): Eff[U, Unit] = {
        x match {
          case Tell(mail) =>
            // TODO: how should we do `mail`?????
            val result = continuation(())
            sendMail(mail)
            result
        }
      }

      def onApplicativeEffect[X, T[_]: Traverse](
        xs: T[MailAction[X]],
        continuation: Continuation[U, T[X], (List[Mail], A)]
      ): Eff[U, (List[Mail], A)] =
        continuation
          .apply(xs.map { case Tell(_) => () })
          .map {
            case (mails, a) =>
              (
                xs.foldLeft(mails) {
                  case (acc, Tell(mail)) =>
                    mail :: acc
                },
                a
              )
          }
    })

    runDBIO(mailRemoved).flatMap {
      case (mails, a) =>
        mails.traverse(sendMail).map(_ => a)
    }
  }
}