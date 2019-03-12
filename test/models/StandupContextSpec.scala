package models

import java.time.Duration

import models.countdown.{InMemoryDB, StandupContext, Team}
import org.scalatest.{Matchers, WordSpec}

class StandupContextSpec extends WordSpec with Matchers {

  trait InitialisedStandupContext {
    val standupContext = new StandupContext("S1", InMemoryDB.standups)

    def sleep(duration: Duration): Unit = Thread.sleep(duration.toMillis)
  }

  "Standup context" should {
    "not have team update when standup is not started" in new InitialisedStandupContext {

      standupContext.inProgress() shouldBe None
    }

    "be able to start the team update when asked to start" in new InitialisedStandupContext {
      standupContext.startNext()

      standupContext.inProgress().map(_.team) shouldBe Some(Team(id = 1, name = "Team 1", speaker = "Dave", Duration.ofSeconds(180)))
    }

    "reduce the time left with the passage of time" in new InitialisedStandupContext {
      standupContext.startNext()
      sleep(Duration ofSeconds 2)
      standupContext.inProgress().map(_.countdown.remaining()).exists(_ < 179) shouldBe true
    }

    "be able to start the next update" in new InitialisedStandupContext {
      standupContext.startNext()
      standupContext.startNext()
      standupContext.inProgress().map(_.team) shouldBe Some(Team(id = 2, name = "Team 2", speaker = "Tom", Duration.ofSeconds(120)))
    }

    "be able to pause the update" in new InitialisedStandupContext {
      standupContext.startNext()
      standupContext.inProgress().foreach(_.countdown.pause())
      val rem = standupContext.inProgress().map(_.countdown.remaining())
      sleep(Duration ofSeconds 2)

      rem shouldBe standupContext.inProgress().map(_.countdown.remaining())
    }

    "detect when standup is finished" in new InitialisedStandupContext {
      standupContext.startNext()
      standupContext.startNext()
      standupContext.startNext()
      standupContext.inProgress().map(_.team) shouldBe None
    }

  }
}
