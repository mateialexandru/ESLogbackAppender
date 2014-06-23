package com.busymachines.logback.appenders

import ch.qos.logback.classic.spi.{ ILoggingEvent }
import ch.qos.logback.core.{ Layout, LayoutBase, UnsynchronizedAppenderBase }
import scala.util.matching.Regex
import java.util.Locale
import org.joda.time.DateTime
import scala.beans.BeanProperty
import scala.beans.BooleanBeanProperty
import ch.qos.logback.classic.spi.StackTraceElementProxy
import com.busymachines.logback.LogHelper._
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.settings.ImmutableSettings
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.elasticsearch.common.xcontent.XContentFactory._
import org.joda.time.format.DateTimeFormat
import com.codahale.metrics.{ Timer, MetricRegistry, ConsoleReporter, CsvReporter }
import java.util.concurrent.TimeUnit
import java.io.File
import scala.slick.driver.PostgresDriver.simple._
import java.sql.Time
import java.sql.Timestamp
import scala.slick.driver.PostgresDriver
import scala.slick.driver.PostgresDriver.backend.Session
import scala.slick.ast.ColumnOption.DBType

/**
 * Created by alex on 23.06.2014.
 */
class SqlLogstashAppender[E] extends UnsynchronizedAppenderBase[E] {
  val logMessages = TableQuery[LogMessages]

  @BeanProperty var host = "localhost"
  @BeanProperty var port = "5432"
  @BeanProperty var database = "log"
  @BeanProperty var user = "postgres"
  @BeanProperty var password = "HOew6xq31J7rBBA"
  @BeanProperty var driver = "org.postgresql.Driver"
  val jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + database

  val db = Database.forURL(jdbcUrl, user, password, driver = driver)

  def intializeDatabase = {
    println(s"intitalizing database: $jdbcUrl")
    db.withSession {
      implicit session =>
      //logMessages.ddl.create
    }
  }

  def intializeMetrics = {
    val metrics: MetricRegistry = new MetricRegistry();
    val timer: com.codahale.metrics.Timer = metrics.timer("SqlLogstashAppenderTimer")
    val reporter: ConsoleReporter = ConsoleReporter.forRegistry(metrics)
      .convertRatesTo(TimeUnit.SECONDS)
      .convertDurationsTo(TimeUnit.MILLISECONDS)
      .build();
    val reporter2: CsvReporter = CsvReporter.forRegistry(metrics)
      .formatFor(Locale.US)
      .convertRatesTo(TimeUnit.SECONDS)
      .convertDurationsTo(TimeUnit.MILLISECONDS)
      .build(new File("/home/alex/Documents/Thesis/logger/metrics/SQLAppender/"));
    //    reporter.start(1, TimeUnit.MILLISECONDS);
    reporter2.start(1, TimeUnit.MINUTES);
    timer
  }

  intializeDatabase
  val timer = intializeMetrics

  val defaultApplicationName = "app"

  lazy val layout: Layout[E] = new LogstashSQLAppenderLayout

  def append(eventObject: E) =
    send(layout.doLayout(eventObject))

  private def send(data: String) = {

    data.split(",").toList match {
      case List(source, sourceHost, sourcePath, file, message, timestamp, classFile, fqn, level, thread, lineNumber, stackTrace) =>
        db.withSession {
          implicit session =>
            val context: Timer.Context = timer.time();
            try {
              logMessages += (None, source, sourceHost, sourcePath, file, message, None, classFile, fqn, level, thread, lineNumber, stackTrace)
            } catch {
              case ex: Exception =>
            } finally {
              context.stop()
            }
        }
      case _ => //println("not logging")
    }

  }

  override def stop = {
    super.stop
  }
}
