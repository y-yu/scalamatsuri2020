package dbio

case class DBIO[A](
  run: Session => A
) {
  def map[B](f: A => B): DBIO[B] =
    flatMap(a => DBIO(_ => f(a)))

  def flatMap[B](f: A => DBIO[B]): DBIO[B] =
    DBIO(s => f(run(s)).run(s))
}

object DBIO {
  def ask[A](f: Session => A): DBIO[A] =
    DBIO(f)

  def apply[A](a: A): DBIO[A] =
    DBIO((_: Session) => a)
}
