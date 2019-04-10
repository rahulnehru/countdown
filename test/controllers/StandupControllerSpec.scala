package controllers

import java.time.Duration

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import cats.data.NonEmptyList
import models._
import org.mockito.ArgumentMatchers.{any => catchAny}
import org.scalatest.{Matchers, WordSpec}
import play.api.test.{FakeRequest, StubControllerComponentsFactory}
import play.api.test.Helpers._
import repository.JsonFileStandupRepository
import org.specs2.mock.Mockito
import org.mockito.Mockito._
import play.api.libs.json.{JsValue, Json, OFormat}
import play.api.mvc.{AnyContentAsJson, Result}
import utils.AsyncHelpers

import scala.concurrent.Future

class StandupControllerSpec extends WordSpec with Matchers with Mockito with StubControllerComponentsFactory with AsyncHelpers {

  implicit val actorSystem: ActorSystem = ActorSystem("Testing")
  implicit val materializer: Materializer = ActorMaterializer()
  implicit val standupNamesFormats: OFormat[StandupNames] = Json.format[StandupNames]

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

  trait Setup {
    val standupRepo: JsonFileStandupRepository = mock[JsonFileStandupRepository]

    val controller = new StandupController(stubControllerComponents(), standupRepo)
  }

  val s1: Standup = standups.head
  val s2: Standup = standups(1)

  def bodyToJson(result: Future[Result]): JsValue = Json.parse(contentAsString(result))

  "getAllStandups" should {

    "return a JSON with all standups names" in new Setup {

      when(standupRepo.getAll).thenReturn(standups)

      val result: Future[Result] = controller.getAllStandups.apply(FakeRequest())

      status(result) shouldBe 200
      bodyToJson(result).as[List[StandupNames]] shouldBe List(
        StandupNames(s1.name, s1.displayName),
        StandupNames(s2.name, s2.displayName)
      )
    }
  }

  "get" should {

    "return a JSON with standup specified if it exists" in new Setup {
      when(standupRepo.get(catchAny[String])).thenReturn(Some(standups.head))

      val result: Future[Result] = controller.get("test").apply(FakeRequest())

      status(result) shouldBe OK
      bodyToJson(result).as[Standup] shouldBe s1
    }
    "return a 404 if standup does not exist" in new Setup {
      when(standupRepo.get(catchAny[String])).thenReturn(None)

      val result: Future[Result] = controller.get("test").apply(FakeRequest())

      status(result) shouldBe NOT_FOUND
    }

  }

  "add" should {

    "should return the standup and call the standup repo add" in new Setup {
      when(standupRepo.add(catchAny[Standup])).thenReturn(Future.successful(s1))

      val fakeReq: FakeRequest[AnyContentAsJson] = FakeRequest().withJsonBody(Json.toJson(s1))
      val result: Future[Result] = controller.add.apply(fakeReq)

      verify(standupRepo).add(catchAny[Standup])
      status(result) shouldBe CREATED
      bodyToJson(result).as[Standup] shouldBe s1
    }

    "should return a 422 and not save to db" in new Setup {
      val fakeReq: FakeRequest[AnyContentAsJson] = FakeRequest().withJsonBody(Json.toJson("""NOT JSON"""))
      val result: Future[Result] = controller.add.apply(fakeReq)

      verify(standupRepo, times(0)).add(catchAny[Standup])

      status(result) shouldBe UNPROCESSABLE_ENTITY
    }
  }

  "edit" should {

    "should return the new standup and call the standup repo edit" in new Setup {
      when(standupRepo.get(catchAny[String])).thenReturn(Some(s1))
      when(standupRepo.edit(catchAny[Standup])).thenReturn(Future.successful(s1))

      val fakeReq: FakeRequest[AnyContentAsJson] = FakeRequest().withJsonBody(Json.toJson(s1))
      val result: Future[Result] = controller.edit.apply(fakeReq)

      verify(standupRepo).edit(catchAny[Standup])
      status(result) shouldBe OK
      bodyToJson(result).as[Standup] shouldBe s1
    }

    "should return a 404 and not save to db" in new Setup {
      when(standupRepo.get(catchAny[String])).thenReturn(None)

      val fakeReq: FakeRequest[AnyContentAsJson] = FakeRequest().withJsonBody(Json.toJson(s1))
      val result: Future[Result] = controller.edit.apply(fakeReq)

      verify(standupRepo, times(0)).edit(catchAny[Standup])

      status(result) shouldBe NOT_FOUND
    }

    "should return a 422 and not save to db" in new Setup {
      val fakeReq: FakeRequest[AnyContentAsJson] = FakeRequest().withJsonBody(Json.toJson("""NOT JSON"""))
      val result: Future[Result] = controller.edit.apply(fakeReq)

      verify(standupRepo, times(0)).edit(catchAny[Standup])

      status(result) shouldBe UNPROCESSABLE_ENTITY
    }
  }

  "remove" should {

    "should return 200 and call delete from the repo standup" in new Setup {
      when(standupRepo.get(catchAny[String])).thenReturn(Some(s1))
      when(standupRepo.delete(catchAny[Standup])).thenReturn(Future.successful(true))

      val fakeReq: FakeRequest[AnyContentAsJson] = FakeRequest().withJsonBody(Json.toJson(s1))
      val result: Future[Result] = controller.remove.apply(fakeReq)

      verify(standupRepo).delete(catchAny[Standup])
      status(result) shouldBe OK
    }

    "should return a 404 and not save to db" in new Setup {
      when(standupRepo.get(catchAny[String])).thenReturn(None)

      val fakeReq: FakeRequest[AnyContentAsJson] = FakeRequest().withJsonBody(Json.toJson(s1))
      val result: Future[Result] = controller.remove.apply(fakeReq)

      verify(standupRepo, times(0)).delete(catchAny[Standup])

      status(result) shouldBe NOT_FOUND
    }

    "should return a 422 and not save to db" in new Setup {
      val fakeReq: FakeRequest[AnyContentAsJson] = FakeRequest().withJsonBody(Json.toJson("""NOT JSON"""))
      val result: Future[Result] = controller.remove.apply(fakeReq)

      verify(standupRepo, times(0)).delete(catchAny[Standup])

      status(result) shouldBe UNPROCESSABLE_ENTITY
    }
  }

  "isStandupLive" should {

    val dummyTeamUpdate =
      TeamUpdate(
        Team(id = 1, name = "First Team", speaker = "First Speaker", Duration.ofSeconds(45)),
        Countdown(Duration.ofSeconds(20))
      )

    "return 200 if standup is found and is live" in new Setup {
      when(standupRepo.get(catchAny[String])).thenReturn(Some(s1))
      when(standupRepo.status(catchAny[String])).thenReturn(Some(dummyTeamUpdate))

      val result: Future[Result] = controller.isStandupLive("name").apply(FakeRequest())

      status(result) shouldBe OK
    }

    "return 409 if standup is found and not live" in new Setup  {
      when(standupRepo.get(catchAny[String])).thenReturn(Some(s1))
      when(standupRepo.status(catchAny[String])).thenReturn(None)

      val result: Future[Result] = controller.isStandupLive("name").apply(FakeRequest())

      status(result) shouldBe GONE
    }

    "return 404 if standup is not found" in new Setup  {
      when(standupRepo.get(catchAny[String])).thenReturn(None)

      val result: Future[Result] = controller.isStandupLive("name").apply(FakeRequest())

      status(result) shouldBe NOT_FOUND
    }

  }
}
