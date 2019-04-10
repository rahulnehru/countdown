package utils

import scala.concurrent.{Await, Awaitable}
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration._

trait AsyncHelpers {

  val waitTime: FiniteDuration = 2 seconds

  def await[T](await: Awaitable[T]): T = Await.result(await, waitTime)

}
