package models

import java.time.Duration

import play.api.libs.functional.syntax.unlift
import play.api.libs.json._
import play.api.libs.functional.syntax._

case class Team(id: Long, name: String, speaker: String, allocationInSeconds: Duration)

case object Team {

  val durationWrite: Writes[Duration] = (o: Duration) =>
    JsNumber(o.getSeconds)

  val durationRead: Reads[Duration] =
    Reads.of[Int].map(Duration.ofSeconds(_))

  implicit val reads: Reads[Team] = (
    (JsPath \ "id").read[Long] and
      (JsPath \ "name").read[String] and
      (JsPath \ "speaker").read[String] and
      (JsPath \ "allocationInSeconds").read(durationRead)
    ) (Team.apply _)

  implicit val writes: Writes[Team] = (
    (JsPath \ "id").write[Long] and
      (JsPath \ "name").write[String] and
      (JsPath \ "speaker").write[String] and
      (JsPath \ "allocationInSeconds").write(durationWrite)
    ) (unlift(Team.unapply))

  implicit val teamFormat: Format[Team] = Format[Team](reads, writes)

}