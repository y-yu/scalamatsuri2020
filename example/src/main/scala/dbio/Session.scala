package dbio

import scalikejdbc.DBSession

/**
  * Database session
  */
trait Session {
  def value: DBSession
}
