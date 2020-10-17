package infra.dao

import dbio.DBIO
import scalikejdbc._

case class User(
  id: Long,
  name: String
)

object UserDAO {
  def create(
    name: String
  ): DBIO[User] = DBIO.ask { session =>
    implicit val ctx: DBSession = session.value

    val sql = sql"""insert into user (name) values ($name)"""
    val id = sql.updateAndReturnGeneratedKey().apply()
    User(id, name)
  }

  def read(
    id: Long
  ): DBIO[Option[User]] = DBIO.ask { session =>
    implicit val ctx: DBSession = session.value

    val sql = sql"""select * from user where id = ${id}"""
    sql.map(rs => User(rs.long("id"), rs.string("name"))).single().apply()
  }

  def update(
    user: User
  ): DBIO[Boolean] =
    DBIO.ask { session =>
      implicit val ctx: DBSession = session.value

      val sql = sql"""update user set name = ${user.name} where id = ${user.id}"""
      sql.update().apply() > 0
    }

  def delete(id: Long): DBIO[Boolean] =
    DBIO.ask { session =>
      implicit val ctx: DBSession = session.value

      val sql = sql"""delete user where id = ${id}"""
      sql.update().apply() > 0
    }
}