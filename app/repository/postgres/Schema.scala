package repository.postgres

import slick.jdbc.PostgresProfile.api._
import slick.lifted.Tag

object Schema {

  class StandUps(tag: Tag) extends Table[(Long, String, String)](tag, "stand_ups") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def name = column[String]("name", O.Unique)

    def displayName = column[String]("display_name")

    override def * = (id, name, displayName)
  }

  val standUps = TableQuery[StandUps]

  class Teams(tag: Tag) extends Table[(Long, String, String, Int, Long)](tag, "teams") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def name = column[String]("name", O.Unique)

    def speaker = column[String]("speaker")

    def allocationInSeconds = column[Int]("allocation_in_seconds")

    def standUpId = column[Long]("stand_up_id")

    def standUp = foreignKey("stand_up_FK", standUpId, standUps)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

    override def * = (id, name, speaker, allocationInSeconds, standUpId)
  }

  val teams = TableQuery[Teams]
}
