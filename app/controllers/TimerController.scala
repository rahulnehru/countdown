package controllers

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.stream.Materializer
import com.google.inject.{Inject, Singleton}
import play.api.mvc._
import akka.stream.scaladsl._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.Duration
import scala.concurrent.duration._


@Singleton
class TimerController @Inject()(cc: ControllerComponents)
                               (implicit system: ActorSystem, mat: Materializer, implicit val ec: ExecutionContext)
  extends AbstractController(cc) {


  def start = WebSocket.accept[String, String] { request =>

    val in = Sink.ignore

    var i = 60
    Flow[String].map { msg =>

      def countdown() = {
        system.scheduler.schedule(0 seconds, 60 seconds) {
          val out = Source.single(i.toString)
          Flow.fromSinkAndSource(in, out)
        }
      }
      countdown()
      "Time's up!"

    }


  }
}