package controllers

import java.time.Duration

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import cats.data.NonEmptyList
import models._
import org.mockito.ArgumentMatchersSugar
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{Matchers, WordSpec}
import play.api.libs.json.{JsValue, Json, OFormat}
import play.api.mvc.{AnyContentAsJson, Result}
import play.api.test.Helpers._
import play.api.test.{FakeRequest, StubControllerComponentsFactory}
import repository.StandUpRepository
import utils.AsyncHelpers

import scala.concurrent.Future

class StandUpControllerSpec extends WordSpec with Matchers with MockitoSugar with ArgumentMatchersSugar with StubControllerComponentsFactory with AsyncHelpers with ScalaFutures {

  implicit val actorSystem: ActorSystem = ActorSystem("Testing")
  implicit val materializer: Materializer = ActorMaterializer()
  implicit val standUpNamesFormats: OFormat[StandupNames] = Json.format[StandupNames]

  val teamsInStandUp1 = List(
    Team(id = 1, name = "First Team", speaker = "First Speaker", Duration.ofSeconds(45)),
    Team(id = 2, name = "Second Team", speaker = "Second Speaker", Duration.ofSeconds(45)),
    Team(id = 3, name = "Third Team", speaker = "Third Speaker", Duration.ofSeconds(45))
  )

  val standUps = Set(
    Standup(id = 1, name = "test", displayName="Test Standup", teams = NonEmptyList(
      teamsInStandUp1.head, teamsInStandUp1.tail
    )),
    Standup(id = 2, name = "test-two", displayName = "Test Standup Two", teams = NonEmptyList(
      Team(id = 1, name = "First Team", speaker = "First Speaker", Duration.ofSeconds(45)),
      List(
        Team(id = 2, name = "Second Team", speaker = "Second Speaker", Duration.ofSeconds(45)),
        Team(id = 3, name = "Third Team", speaker = "Third Speaker", Duration.ofSeconds(45))
      )
    ))
  )

  trait Setup {
    val standUpRepo = mock[StandUpRepository]

    val controller = new StandupController(stubControllerComponents(), standUpRepo)
  }

  val s1: Standup = standUps.head
  val s2: Standup = standUps.tail.head

  def bodyToJson(result: Future[Result]): JsValue = Json.parse(contentAsString(result))

  "getAllStandUps" should {

    "return a JSON with all standUps names" in new Setup {

      when(standUpRepo.getAll).thenReturn(Future.successful(standUps))

      val result: Future[Result] = controller.getAllStandUps.apply(FakeRequest())

      status(result) shouldBe 200
      bodyToJson(result).as[List[StandupNames]] shouldBe List(
        StandupNames(s1.name, s1.displayName),
        StandupNames(s2.name, s2.displayName)
      )
    }
  }

  "get" should {

    "return a JSON with standUp specified if it exists" in new Setup {
      when(standUpRepo.find(any[String])).thenReturn(Future.successful(Some(standUps.head)))

      val result: Future[Result] = controller.get("test").apply(FakeRequest().withJsonBody(Json.toJson(Map("a"->"a"))))

      status(result) shouldBe OK
      bodyToJson(result).as[Standup] shouldBe s1
    }
    "return a 404 if standUp does not exist" in new Setup {
      when(standUpRepo.find(any[String])).thenReturn(Future.successful(None))

      val result: Future[Result] = controller.get("test").apply(FakeRequest())

      status(result) shouldBe NOT_FOUND
    }

  }

  "add" should {

    "should return the standUp and call the standUp repo add" in new Setup {
      when(standUpRepo.add(any[Standup])).thenReturn(Future.successful(s1))
      when(standUpRepo.find(any[String])).thenReturn(Future.successful(None))

      val fakeReq: FakeRequest[AnyContentAsJson] = FakeRequest().withJsonBody(Json.toJson(s1))
      val result: Future[Result] = controller.add.apply(fakeReq)

      verify(standUpRepo).add(any[Standup])
      status(result) shouldBe CREATED
      bodyToJson(result).as[Standup] shouldBe s1
    }

    "should return a 409 if the standUp is valid but name already exists" in new Setup {
      when(standUpRepo.add(any[Standup])).thenReturn(Future.successful(s1))
      when(standUpRepo.find(any[String])).thenReturn(Future.successful(Some(s1)))

      val fakeReq: FakeRequest[AnyContentAsJson] = FakeRequest().withJsonBody(Json.toJson(s1))
      val result: Future[Result] = controller.add.apply(fakeReq)

      verify(standUpRepo, times(0)).add(any[Standup])
      status(result) shouldBe CONFLICT
    }

    "should return a 422 and not save to db" in new Setup {
      val fakeReq: FakeRequest[AnyContentAsJson] = FakeRequest().withJsonBody(Json.toJson("""NOT JSON"""))
      val result: Future[Result] = controller.add.apply(fakeReq)

      verify(standUpRepo, times(0)).add(any[Standup])

      status(result) shouldBe UNPROCESSABLE_ENTITY
    }
  }

  "add teams" should {
    "add teams to existing standUp" in new Setup {
      when(standUpRepo.addTeams(any[String], any[Set[Team]])).thenReturn(Future.successful(3))

      val fakeReq: FakeRequest[AnyContentAsJson] = FakeRequest().withJsonBody(Json.toJson(teamsInStandUp1))
      val result: Future[Result] = controller.addTeams("test").apply(fakeReq)

      status(result) shouldBe OK
      whenReady(result) { _ =>
        verify(standUpRepo).addTeams("test", teamsInStandUp1.toSet)
      }
    }

    "not add team when stand up does not exist" in new Setup {
      when(standUpRepo.addTeams(any[String], any[Set[Team]])).thenReturn(Future.failed(new IllegalArgumentException()))

      val fakeReq: FakeRequest[AnyContentAsJson] = FakeRequest().withJsonBody(Json.toJson(teamsInStandUp1))
      val result: Future[Result] = controller.addTeams("test").apply(fakeReq)

      status(result) shouldBe BAD_REQUEST

      whenReady(result) { _ =>
        verify(standUpRepo).addTeams("test", teamsInStandUp1.toSet)
      }
    }
  }


  "remove teams" should {
    "remove teams from existing standUp" in new Setup {
      when(standUpRepo.removeTeams(any[Set[String]])).thenReturn(Future.successful(3))

      val fakeReq: FakeRequest[AnyContentAsJson] = FakeRequest().withJsonBody(Json.toJson(Set("test")))

      val result: Future[Result] = controller.removeTeams().apply(fakeReq)

      status(result) shouldBe OK
      whenReady(result) { _ =>
        verify(standUpRepo).removeTeams(Set("test"))
      }
    }

    "not not remove teams when they do not exist" in new Setup {
      when(standUpRepo.removeTeams(any[Set[String]])).thenReturn(Future.failed(new IllegalArgumentException()))

      val fakeReq: FakeRequest[AnyContentAsJson] = FakeRequest().withJsonBody(Json.toJson(Set("test")))

      val result: Future[Result] = controller.removeTeams().apply(fakeReq)

      status(result) shouldBe BAD_REQUEST

      whenReady(result) { _ =>
        verify(standUpRepo).removeTeams(Set("test"))
      }
    }
  }

  "edit" should {

    "should return the new standUp and call the standUp repo edit" in new Setup {
      when(standUpRepo.find(any[String])).thenReturn(Future.successful(Some(s2)))
      when(standUpRepo.edit(any[Standup])).thenReturn(Future.successful(s2))

      val fakeReq: FakeRequest[AnyContentAsJson] = FakeRequest().withJsonBody(Json.toJson(s2))
      val eventualResult: Future[Result] = controller.edit.apply(fakeReq)

      bodyToJson(eventualResult).as[Standup] shouldBe s2
      status(eventualResult) shouldBe OK
      whenReady(eventualResult) { _ =>
        verify(standUpRepo).edit(s2)
      }

    }

    "should return a 404 and not save to db" in new Setup {
      when(standUpRepo.find(any[String])).thenReturn(Future.successful(None))

      val fakeReq: FakeRequest[AnyContentAsJson] = FakeRequest().withJsonBody(Json.toJson(s1))
      val result: Future[Result] = controller.edit.apply(fakeReq)

      verify(standUpRepo, times(0)).edit(any[Standup])

      status(result) shouldBe NOT_FOUND
    }

    "should return a 422 and not save to db" in new Setup {
      val fakeReq: FakeRequest[AnyContentAsJson] = FakeRequest().withJsonBody(Json.toJson("""NOT JSON"""))
      val result: Future[Result] = controller.edit.apply(fakeReq)

      verify(standUpRepo, times(0)).edit(any[Standup])

      status(result) shouldBe UNPROCESSABLE_ENTITY
    }
  }

  "remove stand up" should {

    "should return 200 and call delete from the repo standUp" in new Setup {
      when(standUpRepo.find(any[String])).thenReturn(Future.successful(Some(s1)))
      when(standUpRepo.delete(any[Standup])).thenReturn(Future.successful(true))

      val result: Future[Result] = controller.removeStandUp(s1.name).apply(FakeRequest())

      status(result) shouldBe OK
      whenReady(result)(_ => verify(standUpRepo).delete(any[Standup]))
    }

    "should return a 404 and not save to db" in new Setup {
      when(standUpRepo.find(any[String])).thenReturn(Future.successful(None))

      val result: Future[Result] = controller.removeStandUp(s1.name).apply(FakeRequest())

      verify(standUpRepo, times(0)).delete(any[Standup])

      status(result) shouldBe NOT_FOUND
    }
  }

  "isStandUpLive" should {

    val dummyTeamUpdate =
      TeamUpdate(
        Team(id = 1, name = "First Team", speaker = "First Speaker", Duration.ofSeconds(45)),
        Countdown(Duration.ofSeconds(20))
      )

    "return 200 if standUp is found and is live" in new Setup {
      when(standUpRepo.find(any[String])).thenReturn(Future.successful(Some(s1)))
      when(standUpRepo.status(any[String])).thenReturn(Some(dummyTeamUpdate))

      val result: Future[Result] = controller.isStandUpLive("name").apply(FakeRequest())

      status(result) shouldBe OK
    }

    "return 410 if standUp is found and not live" in new Setup  {
      when(standUpRepo.find(any[String])).thenReturn(Future.successful(Some(s1)))
      when(standUpRepo.status(any[String])).thenReturn(None)

      val result: Future[Result] = controller.isStandUpLive("name").apply(FakeRequest())

      status(result) shouldBe GONE
    }

    "return 404 if standUp is not found" in new Setup  {
      when(standUpRepo.find(any[String])).thenReturn(Future.successful(None))

      val result: Future[Result] = controller.isStandUpLive("name").apply(FakeRequest())

      status(result) shouldBe NOT_FOUND
    }

  }
}
