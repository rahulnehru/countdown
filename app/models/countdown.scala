package models

import java.time.Duration
import java.util.concurrent.atomic.AtomicLong
import java.util.{Timer, TimerTask}

import cats.data.NonEmptyList
import play.api.libs.json._

import scala.collection.mutable
import scala.collection.mutable.Queue
import scala.util.Try


object countdown {

  trait DB {
    def standups: List[Standup]
  }

  object InMemoryDB extends DB {
    override def standups: List[Standup] = List(
      Standup(id = 1, name = "S1", teams = NonEmptyList(
        Team(id = 1, name = "Team 1", speaker = "Dave", Duration.ofSeconds(20)),
        List(Team(id = 2, name = "Team 2", speaker = "Tom", Duration.ofSeconds(10)))
      )),
      Standup(id = 2, name = "S2", teams = NonEmptyList(
        Team(id = 1, name = "Team 3", speaker = "Jack", Duration.ofSeconds(90)),
        List(Team(id = 1, name = "Team 4", speaker = "Jerry", Duration.ofSeconds(90)))
      ))
    )

  }

  implicit val durationWrite: Writes[Duration] = (o: Duration) => {
    JsNumber(o.getSeconds)
  }

  case class Team(id: Long, name: String, speaker: String, allocationInSeconds: Duration)
  case object Team {
    implicit val teamFormat = Json.format[Team]
  }

  case class Standup(id: Long, name: String, teams: NonEmptyList[Team])

  case object Standup {

    implicit val nelStandupWrites: Writes[NonEmptyList[Team]] = Writes { nelTeams =>
      Json.toJson(nelTeams.toList)
    }

    implicit val standupFormat = Json.format[Standup]

    implicit val nelErrorReads: Reads[NonEmptyList[Team]] = Reads {
      case JsArray(teams) =>
        teams.toList match {
          case head :: tail => ??? // return type should be JsResult[NEL[Error]]
          case Nil => JsError("expected a NonEmptyList but got empty list")
        }
      case other: JsValue =>
        JsError(s"expected an array but got ${other.toString}")

    }
  }

  case class Countdown(duration: Duration) {

    val atomicCounter = new AtomicLong(duration.getSeconds)

    var timer: Option[Timer] = None

    def start() = {
      atomicCounter.getAndSet(duration.getSeconds)

      timer = Some(new Timer("countdown"))
      timer.foreach(_.scheduleAtFixedRate(new TimerTask() {
        override def run(): Unit = {
          atomicCounter.decrementAndGet()
          ()
        }
      }, 0, 1000))
    }

    def pause() = timer.foreach(_.cancel())

    def remaining(): Long = atomicCounter.get()

  }

  object Countdown {
    import models.countdown.durationWrite

    implicit val countdownWrites: Writes[Countdown] = Writes { countdown =>
      Json.toJson(countdown.duration)
    }

    implicit val countdownReads: Reads[Countdown] =
      (__ \ "duration").read[String].map(duration => Countdown(Duration.ofSeconds(duration.toLong)))

    implicit val countdownFormat: Format[Countdown] =
      Format(countdownReads, countdownWrites)


  }

  case class TeamUpdate(team: Team, countdown: Countdown)
  object TeamUpdate {
    implicit val teamUpdateWrites: Writes[TeamUpdate] = (update: TeamUpdate) => {
      Json.obj(
        "name" -> update.team.name,
        "name" -> update.team.name,
        "speaker" -> update.team.speaker,
        "remainingSeconds" -> update.countdown.remaining()
      )
    }

    //implicit val teamUpdateFormat = Json.format[TeamUpdate]
  }

  case class StandupContext(standupName: String, val standups: List[Standup]){

    private val queue: mutable.Queue[TeamUpdate] = standups.find(_.name == standupName).map { standup =>
        val teamUpdateQueue = Queue[TeamUpdate]()
        standup.teams.toList.foreach(t => teamUpdateQueue.enqueue(TeamUpdate(t, new Countdown(t.allocationInSeconds))))
        teamUpdateQueue
      }.getOrElse(Queue.empty[TeamUpdate])

    private var current: Option[TeamUpdate] = None

    private def next(): Option[TeamUpdate] = Try {
      val n = queue.dequeue()
      current = Some(n)
      n
    }.toOption

    def inProgress(): Option[TeamUpdate] = current

    def left() = List(queue)

    def pause(): Option[TeamUpdate] = {
      current.foreach(_.countdown.pause())
      current
    }

    def startNext(): Option[TeamUpdate] = {
      current = next()
      current.foreach(_.countdown.start())
      current
    }

    def timeLeft(): Long = current.map(_.countdown.remaining()).getOrElse(0L)
  }

  case class Message(message: String)
  case object Message {
    implicit val messageFormat: Format[Message] = Json.format[Message]
  }



}
