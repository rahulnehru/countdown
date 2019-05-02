package models

import java.time.Duration

import cats.data.NonEmptyList
import org.scalatest.{Matchers, WordSpec}

class StandupContextSpec extends WordSpec with Matchers {

  trait InitialisedStandupContext {

    val testStandups = Set(
      Standup(id = 1, name = "test", displayName="Test Standup", teams = NonEmptyList(
        Team(id = 1, name = "First Team", speaker = "First Speaker", Duration.ofSeconds(45)),
        List(
          Team(id = 2, name = "Second Team", speaker = "Second Speaker", Duration.ofSeconds(45)),
          Team(id = 3, name = "Third Team", speaker = "Third Speaker", Duration.ofSeconds(45))
        )
      )),
      Standup(id = 2, name = "test-two", displayName = "Test Standup Two", teams = NonEmptyList(
        Team(id = 1, name = "First Team", speaker = "First Speaker", Duration.ofSeconds(45)),
        List(
          Team(id = 2, name = "Second Team", speaker = "Second Speaker", Duration.ofSeconds(45)),
          Team(id = 3, name = "Third Team", speaker = "Third Speaker", Duration.ofSeconds(45))
        )
      ))
    )

    val standupContext = StandupContext("test", testStandups)

    def sleep(duration: Duration): Unit = Thread.sleep(duration.toMillis)
  }

  "Standup context" should {
    "not have team update when standup is not started" in new InitialisedStandupContext {

      standupContext.inProgress() shouldBe None
    }

    "be able to start the team update when asked to start" in new InitialisedStandupContext {
      standupContext.startNext()

      standupContext.inProgress().map(_.team) shouldBe Some(Team(id = 1, name = "First Team", speaker = "First Speaker", Duration.ofSeconds(45)))
    }

    "reduce the time left with the passage of time" in new InitialisedStandupContext {
      standupContext.startNext()
      sleep(Duration ofSeconds 2)
      standupContext.inProgress().map(_.countdown.remaining()).exists(_ < 45) shouldBe true
    }

    "be able to start the next update" in new InitialisedStandupContext {
      standupContext.startNext()
      standupContext.startNext()
      standupContext.inProgress().map(_.team) shouldBe Some(Team(id = 2, name = "Second Team", speaker = "Second Speaker", Duration.ofSeconds(45)))
    }

    "be able to pause the update" in new InitialisedStandupContext {
      standupContext.startNext()
      standupContext.pause()
      val rem = standupContext.inProgress().map(_.countdown.remaining())
      sleep(Duration ofSeconds 2)

      rem shouldBe standupContext.inProgress().map(_.countdown.remaining())
    }

    "be able to unpause the update" in new InitialisedStandupContext {
      standupContext.startNext()
      standupContext.pause()
      val pausedTeam: Team = standupContext.inProgress().get.team
      val pausedTimeRemaining: Long = standupContext.inProgress().get.countdown.remaining()
      standupContext.unpause()
      sleep(Duration ofSeconds 2)
      val currentTeam: Team = standupContext.inProgress().get.team
      val currentTimeRemaining: Long = standupContext.inProgress().get.countdown.remaining()

      currentTimeRemaining should be < pausedTimeRemaining
      currentTeam should equal(pausedTeam)
    }

    "detect when standup is finished" in new InitialisedStandupContext {
      standupContext.startNext()
      standupContext.startNext()
      standupContext.startNext()
      standupContext.startNext()
      standupContext.inProgress().map(_.team) shouldBe None
    }

  }
}
