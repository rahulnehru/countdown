package repository

import com.google.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}


@Singleton
class TeamRepository @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {
  import profile.api._

  private val Teams = TableQuery[TeamTable]
  case class Team(name: String)
  private class TeamTable(tag: Tag) extends Table[Team](tag, "team") {
    def name = column[String]("name", O.PrimaryKey)
    def * = name <> (Team.apply, Team.unapply)
  }

  def all(): Future[Seq[Team]] = db.run(Teams.result)

  def insert(name: String): Future[Unit] = db.run(Teams += Team(name)).map { _ => () }

  def drop(name: String): Future[Unit] = db.run(Teams.filter(a => a.name === name).delete).map { _ => () }


}
