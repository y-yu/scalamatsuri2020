import eff.DBIOEffect._
import eff.MailEffect._
import infra.Database
import infra.dao.User
import infra.dao.UserDAO
import mail.Mail
import org.atnos.eff.Eff
import org.atnos.eff.Fx

object Main {
  def userUpdateEff[R: _dbio: _mail](
    newUserInfo: User
  ): Eff[R, Unit] =
    for {
      _ <- UserDAO.update(newUserInfo).fromDBIO
      _ <- sendMailEff(
        Mail(
          to = "to@example.com",
          from = "from@example.com",
          body = "hello wolrd!"
        )
      )
    } yield ()

  def main(args: Array[String]): Unit = {
    type R = Fx.fx2[MailAction, DBIOAction]

    Database.setUp()
    Database.createTable()

    runMailAfterDBIO(
      for {
        user <- UserDAO.create("foo").fromDBIO[R]
        _ <- UserDAO.create("bar").fromDBIO[R]
        _ <- UserDAO.create("baz").fromDBIO[R]
        _ <- userUpdateEff[R](user.copy(name = "hogehoge"))
      } yield ()
    )
    /*
    withTransaction { session =>
      userUpdate(User(1, "fuga")).run(session)
      throw new RuntimeException()
    }

     */

    pprint.pprintln(Database.getAllUsers)
  }
}
