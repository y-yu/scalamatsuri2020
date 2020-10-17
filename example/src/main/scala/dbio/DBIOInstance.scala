package dbio

import cats.Monad

object DBIOInstance {
  implicit val dbioMonadInstance: Monad[DBIO] = new Monad[DBIO] {
    def pure[A](x: A): DBIO[A] =
      DBIO(_ => x)

    def flatMap[A, B](fa: DBIO[A])(f: A => DBIO[B]): DBIO[B] =
      fa.flatMap(f)

    def tailRecM[A, B](a: A)(f: A => DBIO[Either[A, B]]): DBIO[B] =
      f(a).flatMap {
        case Right(b) =>
          pure(b)
        case Left(a) =>
          tailRecM(a)(f)
      }
  }
}
