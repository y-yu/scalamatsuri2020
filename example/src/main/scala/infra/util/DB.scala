package infra.util

import com.typesafe.scalalogging.LazyLogging
import dbio.Session
import infra.ScalikeJDBCSession
import scalikejdbc.{DB => ScalikeJDBCDB}
import scala.util.Failure
import scala.util.Success
import scala.util.Try

object DB extends LazyLogging {
  def withTransaction[A](
    f: Session => A
  ): Either[Throwable, A] = {
    logger.info("Transaction is on.")

    Try {
      ScalikeJDBCDB.localTx { dbSession =>
        val session = ScalikeJDBCSession(dbSession)
        f(session)
      }
    } match {
      case Success(a) =>
        logger.info("Transaction is successful")
        Right(a)
      case Failure(e) =>
        logger.error("Transaction failed!!!", e)
        Left(e)
    }
  }
}
