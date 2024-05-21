package modules.event.repository

import modules.event.Event
import slick.dbio.DBIO
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.PostgresProfile.api._
import util.DBTables
import util.DBTables.EventEntity

import java.time.{Instant, LocalDate, ZoneId}
import java.util.Date
import scala.concurrent.Future
import server.Server.executionContext

case class EventDBRepo(db: Database) extends EventRepository {
  private val eventTable = DBTables.eventTable


  override def addEvent(event: Event): Future[Unit] = {
    val action = DBIO.seq(eventTable += transformEvent(event))
    for {
      _ <- db.run(action)
    } yield {}
  }

  override def getEvents: Future[Set[Event]] = {
    for{
      result <- db.run(eventTable.sortBy(_.id).result)
    } yield {
      transformToEventSet(result)
    }
  }

  override def byId(id: Int): Future[Option[Event]] = {
    val q = eventTable.filter(_.id === id)
    for {
      eventEntity <- db.run(q.result.headOption)
    } yield {
      if(eventEntity.isEmpty) None
      else Some(transformEventEntity(eventEntity.get))
    }
  }

  override def deleteById(id: Int): Future[Boolean] = {
    val q = eventTable.filter(_.id === id).delete
    for {
      _ <- db.run(q)
    } yield {
      true
    }
  }

  override def changeEvent(id: Int, newEvent: Event): Future[Unit] = {
    val eventEntity = transformEvent(newEvent)
    val q = eventTable.filter(_.id === id).update(eventEntity)
    for {
      _ <- db.run(q)
    } yield {}
  }

  private def transformToEventSet(seq: Seq[EventEntity]): Set[Event] = {
    seq.toSet.map(
      eventEntity => transformEventEntity(eventEntity)
    )
  }
  private def transformEvent(event: Event): EventEntity = {
    EventEntity(Some(event.getId), event.getName, event.getDescription, event.getCreatorId, dateToLocalDate(event.getDate))
  }
  private def transformEventEntity(eventEntity: EventEntity): Event = {
    Event(eventEntity.name, eventEntity.description, eventEntity.creatorId, localDateToDate(eventEntity.date), eventEntity.id.get)
  }

  private def dateToLocalDate(date: Date): LocalDate = {
    LocalDate.ofInstant(date.toInstant, ZoneId.systemDefault())
  }
  private def localDateToDate(localDate: LocalDate): Date = {
    Date.from(Instant.from(localDate))
  }
}
