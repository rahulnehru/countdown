package repository.postgres

import java.time.Duration

import cats.data.NonEmptyList
import db.PostgresContainerSetup
import models.{Standup, Team}
import org.scalatest.{Matchers, OptionValues, WordSpec}
import play.api.db.slick.DatabaseConfigProvider

//TODO use with GuiceOneAppPerSuite

class PostgresStandupRepositorySpec extends WordSpec with Matchers with PostgresContainerSetup with OptionValues {

  val existingStandUpName = "S1"

  def initialiseDBRecords = repository.add(Standup(1, existingStandUpName, "standUp 1", NonEmptyList(
    Team(1, "T1", "Speaker 1", Duration.ofMinutes(2)),
    Team(2, "T2", "Speaker 2", Duration.ofMinutes(1))::Nil)
  ))

  override def beforeAll(): Unit = {
    super.beforeAll()
    whenReady(initialiseDBRecords) { standUp =>
      assert(standUp.name == existingStandUpName)
    }
  }


  override def afterAll(): Unit = super.afterAll()

  private lazy val repository = new PostgresStandupRepository(fakeapp.injector.instanceOf(classOf[DatabaseConfigProvider]))

  "A repository" should {

    "find a standUp" in {
        repository.find(existingStandUpName).map(_.name) shouldBe Some("S1")
    }

    "add a team" in {
      val result = repository.addTeams(existingStandUpName, Set(Team(3, "T3", "Speaker 3", Duration.ofMinutes(1))))
      whenReady(result) { teamsAdded =>
        teamsAdded shouldBe 1
        repository.find(existingStandUpName).map(_.teams.size) shouldBe Some(3)
      }
    }

    "remove a team" in {
      val result = repository.removeTeams(Set("T3"))
      whenReady(result) { teamsRemoved =>
        teamsRemoved shouldBe 1
        repository.find(existingStandUpName).map(_.teams.size) shouldBe Some(2)
      }
    }

  }

}
