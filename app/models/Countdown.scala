package models

import java.time.Duration
import java.util.{Timer, TimerTask}
import java.util.concurrent.atomic.AtomicLong

import play.api.libs.json._

case class Countdown(duration: Duration) {

  val atomicCounter = new AtomicLong(duration.getSeconds)

  var timer: Option[Timer] = None

  def start(): Unit = {
    atomicCounter.getAndSet(duration.getSeconds)
    startTimer()
  }

  def pause(): Unit = timer.foreach(_.cancel())

  def unpause(): Unit = startTimer()

  def remaining(): Long = atomicCounter.get()

  private def startTimer(): Unit = {
    timer = Some(new Timer("countdown"))
    timer.foreach(_.scheduleAtFixedRate(new TimerTask() {
      override def run(): Unit = {
        atomicCounter.decrementAndGet()
        ()
      }
    }, 0, 1000))
  }

}

object Countdown {

  implicit val countdownWrites: Writes[Countdown] = Writes { countdown =>
    Json.toJson(countdown.duration)
  }

  implicit val countdownReads: Reads[Countdown] =
    (__ \ "duration").read[String].map(duration => Countdown(Duration.ofSeconds(duration.toLong)))

  implicit val countdownFormat: Format[Countdown] =
    Format(countdownReads, countdownWrites)

}
