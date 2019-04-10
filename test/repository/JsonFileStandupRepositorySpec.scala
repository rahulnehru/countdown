package repository

import java.io.File
import java.time.Duration

import cats.data.NonEmptyList
import models.{Standup, Team}
import org.scalatest.{BeforeAndAfter, Matchers, WordSpec}
import utils.AsyncHelpers

class JsonFileStandupRepositorySpec extends WordSpec with Matchers with BeforeAndAfter with AsyncHelpers {

  val testFilePath: String = "test.json"
  var testFile: File = new File(testFilePath)

  val repo: JsonFileStandupRepository = new JsonFileStandupRepository() {
    override val fileName: String = testFilePath
  }

  before {
    testFile = new File(testFilePath)
    testFile.createNewFile()
  }

  after {
    testFile.delete()
  }

  val standups = List(
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

  val s: Standup = standups.head
  val s2: Standup = standups(1)
  def addStandup(s: Standup): Standup = await(repo.add(s))

  "getAll" should {

    "return empty when no teams added" in {
      repo.getAll shouldBe List.empty
    }

    "return all teams when 2 teams are added" in {
      addStandup(s)
      addStandup(s2)

      repo.getAll shouldBe standups
    }
  }

  "add" should {

    "be able to add a standup to the db file" in {
      addStandup(s) shouldBe s
      repo.getAll shouldBe List(standups.head)
    }

  }

  "get" should {

    "be able to get a standup by name from the db file" in {
      addStandup(s)
      addStandup(s2)

      repo.get(s2.name) shouldBe Some(s2)
    }
  }

  "edit" should {

    "be able to edit a standup in the db file" in {
      addStandup(s)

      val editedStandup = standups.head.copy(name = "newNameForThisStandup")
      await(repo.edit(editedStandup)) shouldBe editedStandup

      repo.getAll shouldBe List(editedStandup)
    }
  }

  "delete" should {

    "be able to remove a standup from the db file" in {
      addStandup(s)

      await(repo.delete(s)) shouldBe true

      repo.getAll shouldBe List.empty
    }
  }

}
