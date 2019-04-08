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
      Standup(id = 1, name = "main", displayName="Access UK Main Standup", teams = NonEmptyList(
        Team(id = 1, name = "Releases", speaker = "Steff", Duration.ofSeconds(45)),
        List(
          Team(id = 2, name = "Fes", speaker = "Tommy", Duration.ofSeconds(45)),
          Team(id = 3, name = "L3", speaker = "David", Duration.ofSeconds(45)),
          Team(id = 4, name = "Out of Country", speaker = "Victor", Duration.ofSeconds(45)),
          Team(id = 5, name = "CI", speaker = "Katie", Duration.ofSeconds(45)),
          Team(id = 6, name = "Tech CI", speaker = "Dominic", Duration.ofSeconds(45)),
          Team(id = 7, name = "Standard Sections", speaker = "Iuliana", Duration.ofSeconds(45)),
          Team(id = 8, name = "Small projects", speaker = "Jeremy", Duration.ofSeconds(45)),
          Team(id = 9, name = "CWI", speaker = "Shiv", Duration.ofSeconds(45)),
          Team(id = 10, name = "Actions", speaker = "Matt", Duration.ofSeconds(45))
        ),
      )),
      Standup(id = 2, name = "ba", displayName="Access UK BA Standup",teams = NonEmptyList(
        Team(id = 1, name = "Katie", speaker = "Katie", Duration.ofSeconds(90)),
        List(
          Team(id = 2, name = "Kate", speaker = "Kate", Duration.ofSeconds(45)),
          Team(id = 3, name = "Samier", speaker = "Samier", Duration.ofSeconds(45)),
          Team(id = 4, name = "Jeremy", speaker = "Jeremy", Duration.ofSeconds(45)),
          Team(id = 5, name = "Thomas", speaker = "Thomas", Duration.ofSeconds(45)),
          Team(id = 6, name = "Tommy", speaker = "Tommy", Duration.ofSeconds(45)),
          Team(id = 7, name = "Harry", speaker = "Harry", Duration.ofSeconds(45)),
          Team(id = 8, name = "Alice", speaker = "Alice", Duration.ofSeconds(45)),
          Team(id = 9, name = "Dean", speaker = "Dean", Duration.ofSeconds(45)),
          Team(id = 10, name = "Eoin", speaker = "Eoin", Duration.ofSeconds(45)),
          Team(id = 11, name = "Victor", speaker = "Victor", Duration.ofSeconds(45)),
          Team(id = 12, name = "Fred", speaker = "Fred", Duration.ofSeconds(45)),
          Team(id = 13, name = "Jamie", speaker = "Jamie", Duration.ofSeconds(45))
        )
      )),
      Standup(id = 3, name = "team5", displayName="Access UK Team 5 Standup",teams = NonEmptyList(
        Team(id = 1, name = "Team 5", speaker = "Cristi", Duration.ofSeconds(90)),
        List(
          Team(id = 2, name = "Team 5", speaker = "Tiberiu", Duration.ofSeconds(45)),
          Team(id = 3, name = "Team 5", speaker = "Alan", Duration.ofSeconds(45)),
          Team(id = 4, name = "Team 5", speaker = "Alejandro", Duration.ofSeconds(45)),
          Team(id = 5, name = "Team 5", speaker = "Raaj", Duration.ofSeconds(45)),
          Team(id = 6, name = "Team 5", speaker = "Ajay", Duration.ofSeconds(45)),
          Team(id = 7, name = "Team 5", speaker = "Jeremy", Duration.ofSeconds(45))
        )
      )),
      Standup(id = 4, name = "dev", displayName="Access UK Dev Symposium",teams = NonEmptyList(
        Team(id = 1, name = "Releases", speaker = "Steff", Duration.ofSeconds(90)),
        List(
          Team(id = 2, name = "L3", speaker = "David", Duration.ofSeconds(45)),
          Team(id = 3, name = "CWI", speaker = "Shiv", Duration.ofSeconds(45)),
          Team(id = 4, name = "Out of Country", speaker = "Alua", Duration.ofSeconds(45)),
          Team(id = 5, name = "Standard Sections", speaker = "Daniel N", Duration.ofSeconds(45)),
          Team(id = 6, name = "Tech CI", speaker = "Dom / Parvez", Duration.ofSeconds(45)),
          Team(id = 7, name = "CI", speaker = "Rahul / Elliot", Duration.ofSeconds(45)),
          Team(id = 8, name = "FES", speaker = "Daniel T", Duration.ofSeconds(45)),
          Team(id = 9, name = "Team 5", speaker = "Alan / Alejandro", Duration.ofSeconds(45)),
          Team(id = 10, name = "EEA FP", speaker = "Ethan / Adam", Duration.ofSeconds(45))
        )
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

  case class Standup(id: Long, name: String, displayName: String, teams: NonEmptyList[Team])

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

    private val queue: mutable.Queue[TeamUpdate] =
      standups.find(_.name == standupName).map { standup =>
        mutable.Queue[TeamUpdate](standup.teams.map(t => TeamUpdate(t, new Countdown(t.allocationInSeconds))).toList: _*)
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

    def unpause(): Option[TeamUpdate] = {
      current.foreach(_.countdown.unpause())
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
