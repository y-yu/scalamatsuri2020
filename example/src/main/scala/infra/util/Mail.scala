package infra.util

import com.typesafe.scalalogging.LazyLogging

object Mail extends LazyLogging {
  import mail.Mail

  def sendMail(
    mail: Mail
  ): Either[Throwable, Unit] = {
    pprint.pprintln(mail)
    logger.info("Mail was sent")
    Right(())
  }
}
