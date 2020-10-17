package infra

import dbio.Session
import scalikejdbc.DBSession

case class ScalikeJDBCSession(
  value: DBSession
) extends Session
