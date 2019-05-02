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

  val standUps = Set(
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
    val standupRepo = mock[StandUpRepository]

    val controller = new StandupController(stubControllerComponents(), standupRepo)
  }

  val s1: Standup = standUps.head
  val s2: Standup = standUps.tail.head

  def bodyToJson(result: Future[Result]): JsValue = Json.parse(contentAsString(result))

  "getAllStandUps" should {

    "return a JSON with all standups names" in new Setup {

      when(standupRepo.getAll).thenReturn(Future.successful(standUps))

      val result: Future[Result] = controller.getAllStandUps.apply(FakeRequest())

      status(result) shouldBe 200
      bodyToJson(result).as[List[StandupNames]] shouldBe List(
        StandupNames(s1.name, s1.displayName),
        StandupNames(s2.name, s2.displayName)
      )
    }
  }

  "get" should {

    "return a JSON with standup specified if it exists" in new Setup {
      when(standupRepo.find(any[String])).thenReturn(Future.successful(Some(standUps.head)))

      val result: Future[Result] = controller.get("test").apply(FakeRequest().withJsonBody(Json.toJson(Map("a"->"a"))))

      status(result) shouldBe OK
      bodyToJson(result).as[Standup] shouldBe s1
    }
    "return a 404 if standup does not exist" in new Setup {
      when(standupRepo.find(any[String])).thenReturn(Future.successful(None))

      val result: Future[Result] = controller.get("test").apply(FakeRequest())

      status(result) shouldBe NOT_FOUND
    }

  }

  "add" should {

    "should return the standup and call the standup repo add" in new Setup {
      when(standupRepo.add(any[Standup])).thenReturn(Future.successful(s1))
      when(standupRepo.find(any[String])).thenReturn(Future.successful(None))

      val fakeReq: FakeRequest[AnyContentAsJson] = FakeRequest().withJsonBody(Json.toJson(s1))
      val result: Future[Result] = controller.add.apply(fakeReq)

      verify(standupRepo).add(any[Standup])
      status(result) shouldBe CREATED
      bodyToJson(result).as[Standup] shouldBe s1
    }

    "should return a 409 if the standup is valid but name already exists" in new Setup {
      when(standupRepo.add(any[Standup])).thenReturn(Future.successful(s1))
      when(standupRepo.find(any[String])).thenReturn(Future.successful(Some(s1)))

      val fakeReq: FakeRequest[AnyContentAsJson] = FakeRequest().withJsonBody(Json.toJson(s1))
      val result: Future[Result] = controller.add.apply(fakeReq)

      verify(standupRepo, times(0)).add(any[Standup])
      status(result) shouldBe CONFLICT
    }

    "should return a 422 and not save to db" in new Setup {
      val fakeReq: FakeRequest[AnyContentAsJson] = FakeRequest().withJsonBody(Json.toJson("""NOT JSON"""))
      val result: Future[Result] = controller.add.apply(fakeReq)

      verify(standupRepo, times(0)).add(any[Standup])

      status(result) shouldBe UNPROCESSABLE_ENTITY
    }
  }

  "edit" should {

    "should return the new standup and call the standup repo edit" in new Setup {
      when(standupRepo.find(any[String])).thenReturn(Future.successful(Some(s2)))
      when(standupRepo.edit(any[Standup])).thenReturn(Future.successful(s2))

      val fakeReq: FakeRequest[AnyContentAsJson] = FakeRequest().withJsonBody(Json.toJson(s2))
      val eventualResult: Future[Result] = controller.edit.apply(fakeReq)

      bodyToJson(eventualResult).as[Standup] shouldBe s2
      status(eventualResult) shouldBe OK
      whenReady(eventualResult) { _ =>
        verify(standupRepo).edit(s2)
      }

    }

    "should return a 404 and not save to db" in new Setup {
      when(standupRepo.find(any[String])).thenReturn(Future.successful(None))

      val fakeReq: FakeRequest[AnyContentAsJson] = FakeRequest().withJsonBody(Json.toJson(s1))
      val result: Future[Result] = controller.edit.apply(fakeReq)

      verify(standupRepo, times(0)).edit(any[Standup])

      status(result) shouldBe NOT_FOUND
    }

    "should return a 422 and not save to db" in new Setup {
      val fakeReq: FakeRequest[AnyContentAsJson] = FakeRequest().withJsonBody(Json.toJson("""NOT JSON"""))
      val result: Future[Result] = controller.edit.apply(fakeReq)

      verify(standupRepo, times(0)).edit(any[Standup])

      status(result) shouldBe UNPROCESSABLE_ENTITY
    }
  }

  "remove" should {

    "should return 200 and call delete from the repo standup" in new Setup {
      when(standupRepo.find(any[String])).thenReturn(Future.successful(Some(s1)))
      when(standupRepo.delete(any[Standup])).thenReturn(Future.successful(true))

      val fakeReq: FakeRequest[AnyContentAsJson] = FakeRequest().withJsonBody(Json.toJson(s1))
      val result: Future[Result] = controller.remove.apply(fakeReq)

      status(result) shouldBe OK
      whenReady(result)(_ => verify(standupRepo).delete(any[Standup]))
    }

    "should return a 404 and not save to db" in new Setup {
      when(standupRepo.find(any[String])).thenReturn(Future.successful(None))

      val fakeReq: FakeRequest[AnyContentAsJson] = FakeRequest().withJsonBody(Json.toJson(s1))
      val result: Future[Result] = controller.remove.apply(fakeReq)

      verify(standupRepo, times(0)).delete(any[Standup])

      status(result) shouldBe NOT_FOUND
    }

    "should return a 422 and not save to db" in new Setup {
      val fakeReq: FakeRequest[AnyContentAsJson] = FakeRequest().withJsonBody(Json.toJson("""NOT JSON"""))
      val result: Future[Result] = controller.remove.apply(fakeReq)

      verify(standupRepo, times(0)).delete(any[Standup])

      status(result) shouldBe UNPROCESSABLE_ENTITY
    }
  }

  "isStandUpLive" should {

    val dummyTeamUpdate =
      TeamUpdate(
        Team(id = 1, name = "First Team", speaker = "First Speaker", Duration.ofSeconds(45)),
        Countdown(Duration.ofSeconds(20))
      )

    "return 200 if standup is found and is live" in new Setup {
      when(standupRepo.find(any[String])).thenReturn(Future.successful(Some(s1)))
      when(standupRepo.status(any[String])).thenReturn(Some(dummyTeamUpdate))

      val result: Future[Result] = controller.isStandUpLive("name").apply(FakeRequest())

      status(result) shouldBe OK
    }

    "return 410 if standup is found and not live" in new Setup  {
      when(standupRepo.find(any[String])).thenReturn(Future.successful(Some(s1)))
      when(standupRepo.status(any[String])).thenReturn(None)

      val result: Future[Result] = controller.isStandUpLive("name").apply(FakeRequest())

      status(result) shouldBe GONE
    }

    "return 404 if standup is not found" in new Setup  {
      when(standupRepo.find(any[String])).thenReturn(Future.successful(None))

      val result: Future[Result] = controller.isStandUpLive("name").apply(FakeRequest())

      status(result) shouldBe NOT_FOUND
    }

  }
}
