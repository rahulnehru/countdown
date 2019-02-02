package repository

import com.google.inject.{Inject, Singleton}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}


@Singleton
class ParticipantRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {
  import profile.api._

  private val Participants = TableQuery[ParticipantTable]
  case class Participant(name: String, teamName: String)
  private class ParticipantTable(tag: Tag) extends Table[Participant](tag, "participant") {
    def name = column[String]("name", O.PrimaryKey)
    def teamName = column[String]("team_id")
    def * = (name, teamName) <> (Participant.tupled, Participant.unapply)
  }

  def all(): Future[Seq[Participant]] = db.run(Participants.result)

  def getByTeam(team: String): Future[Seq[Participant]] = db.run(Participants.filter(a => a.teamName === team).result)

  def insert(name: String, teamName: String): Future[Unit] = db.run(Participants += Participant(name, teamName)).map { _ => () }

  def drop(name: String, teamName: String): Future[Unit] = db.run(Participants.filter(a => a.name === name && a.teamName === teamName).delete).map { _ => () }


}
