package repository.postgres

import java.time.Duration

import cats.data.NonEmptyList
import db.PostgresContainerSetup
import models.{Standup, Team}
import org.scalatest.{Matchers, OptionValues, WordSpec}
import play.api.db.slick.DatabaseConfigProvider

//TODO use with GuiceOneAppPerSuite

class PostgresStandUpRepositorySpec extends WordSpec with Matchers with PostgresContainerSetup with OptionValues {

  private val existingStandUpName = "S1"

  private val standUp = Standup(1, existingStandUpName, "standUp 1", NonEmptyList(
    Team(1, "T1", "Speaker 1", Duration.ofMinutes(2)),
    Team(2, "T2", "Speaker 2", Duration.ofMinutes(1))::Nil)
  )

  def initialiseDBRecords = repository.add(standUp)

  override def beforeAll(): Unit = {
    super.beforeAll()
    whenReady(initialiseDBRecords) { standUp =>
      assert(standUp.name == existingStandUpName)
    }
  }

  override def afterAll(): Unit = super.afterAll()

  private lazy val repository = new PostgresStandUpRepository(fakeapp.injector.instanceOf(classOf[DatabaseConfigProvider]))

  "A repository" should {

    "find a standUp" in {
        whenReady(repository.find(existingStandUpName))(standUp => standUp.map(_.name).value shouldBe "S1")
    }

    "add a team" in {
      val result = repository.addTeams(existingStandUpName, Set(Team(3, "T3", "Speaker 3", Duration.ofMinutes(1))))
      whenReady(result) { teamsAdded =>
        teamsAdded shouldBe 1
        whenReady(repository.find(existingStandUpName))(standUp => standUp.map(_.teams.size).value shouldBe 3)
      }
    }

    "remove a team" in {
      val result = repository.removeTeams(Set("T3"))
      whenReady(result) { teamsRemoved =>
        teamsRemoved shouldBe 1
        whenReady(repository.find(existingStandUpName))(standUp => standUp.map(_.teams.size).value shouldBe 2)
      }
    }

    "edit a stand up" in {
      whenReady(repository.edit(standUp.copy(displayName = "new name"))){ result =>
        result.displayName shouldBe "new name"
      }
    }

    "delete a stand up" in {
      val result = for {
        s <- repository.find (existingStandUpName)
        if(s.isDefined)
        deleted <- repository.delete(s.get)
        deletedStandUp <- repository.find(existingStandUpName)
      } yield (deleted, deletedStandUp)
      whenReady(result) { r =>
        r._1 shouldBe true
        r._2 shouldBe None
      }
    }

  }

}
