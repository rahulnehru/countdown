package models

import cats.data.NonEmptyList
import play.api.libs.functional.syntax._
import play.api.libs.json._

case class Standup(id: Long, name: String, displayName: String, teams: NonEmptyList[Team])

case class StandupNames(name: String, displayName: String)

case object Standup {

  val nelWrites: Writes[NonEmptyList[Team]] = Writes { nelTeams =>
    Json.toJson(nelTeams.toList)
  }

  val nelReads: Reads[NonEmptyList[Team]] =
    Reads
      .of[List[Team]]
      .collect(
        JsonValidationError("expected a non empty list but got an empty list")
      )
      {
        case head :: tail => NonEmptyList(head, tail)
      }

  implicit val nelFormats: Format[NonEmptyList[Team]] = Format[NonEmptyList[Team]](nelReads, nelWrites)

  val reads: Reads[Standup] = (
    (JsPath \ "id").read[Long] and
      (JsPath \ "name").read[String] and
      (JsPath \ "displayName").read[String] and
      (JsPath \ "teams").read[NonEmptyList[Team]]
    ) (Standup.apply _)

  val writes: Writes[Standup] = (
    (JsPath \ "id").write[Long] and
      (JsPath \ "name").write[String] and
      (JsPath \ "displayName").write[String] and
      (JsPath \ "teams").write[NonEmptyList[Team]]
    ) (unlift(Standup.unapply))

  implicit val formats: Format[Standup] = Format[Standup](reads, writes)

}
