package repository

import java.io.{FileInputStream, PrintWriter}

import models.Standup
import play.api.libs.json.{Format, Json}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}

class JsonFileStandupRepository extends StandupRepository {

  val fileName = "standups.json"
  implicit val formats: Format[Standup] = Standup.formats

  override def get(standupName: String): Option[Standup] = {
    standups.find(_.name == standupName)
  }

  override def getAll: List[Standup] = {
    standups
  }

  override def add(s: Standup): Future[Standup] = {
    val id: Long = standups.map(_.id).fold[Long](0){
      (a,b) => if (a > b) a else b
    } + 1
    val newStandup = s.copy(id = id)
    val newStandupsList = standups :+ newStandup
    saveStandups(newStandupsList).map(v => newStandup)
  }

  override def edit(s: Standup): Future[Standup] = {
    val newStandupsList =
      standups.collect {
        case standup if standup.id == s.id =>
          standup.copy(
            name = s.name,
            displayName = s.displayName,
            teams = s.teams
          )
        case standup => standup
      }
    saveStandups(newStandupsList).map(v => s)
  }

  override def delete(s: Standup): Future[Boolean] = {
    val newStandupsList = standups.filter(_.id != s.id)
    saveStandups(newStandupsList).map(v => true)
  }

  private def standups: List[Standup] = {
    val stream = new FileInputStream(fileName)
    val result = Try(Json.parse(stream)) match {
      case Success(value) => Json.parse(value.toString).as[List[Standup]]
      case Failure(value) => List.empty
    }
    stream.close()
    result
  }

  private def saveStandups(standups: List[Standup]): Future[Unit] = Future {
    val writer = new PrintWriter(fileName)
    writer.write(Json.prettyPrint(Json.toJson(standups)))
    writer.close()
  }
}
