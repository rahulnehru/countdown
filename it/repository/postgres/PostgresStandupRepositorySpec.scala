package repository.postgres

import java.time.Duration

import cats.data.NonEmptyList
import db.PostgresContainerSetup
import models.{Standup, Team}
import org.scalatest.{Matchers, OptionValues, WordSpec}
import play.api.db.slick.DatabaseConfigProvider

//TODO use with GuiceOneAppPerSuite

class PostgresStandupRepositorySpec extends WordSpec with Matchers with PostgresContainerSetup with OptionValues {


  override def beforeAll(): Unit = super.beforeAll()


  override def afterAll(): Unit = super.afterAll()

  private lazy val repository = new PostgresStandupRepository(fakeapp.injector.instanceOf(classOf[DatabaseConfigProvider]))

  "A repository" should {
    "add and find a standUp" in {
      val newStandUp = repository.add(Standup(1, "S1", "standUp 1", NonEmptyList(Team(1, "T1", "Speaker 1", Duration.ofMinutes(2)), Nil)))
      whenReady(newStandUp) { standUp =>
        repository.find(standUp.name).map(_.name) shouldBe Some("S1")
      }
    }
  }

}
