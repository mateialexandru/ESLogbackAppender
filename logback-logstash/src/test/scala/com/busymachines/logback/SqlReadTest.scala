package com.busymachines.logback

import com.busymachines.logback.misc.LogMessages
import org.scalatest.FlatSpec
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
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

@RunWith(classOf[JUnitRunner])
class SqlReadTest extends FlatSpec with grizzled.slf4j.Logging {

  def initializeMetrics = {
    val metrics: MetricRegistry = new MetricRegistry();
    val timer: com.codahale.metrics.Timer = metrics.timer("SQLQueryTimer")
    val reporter: ConsoleReporter = ConsoleReporter.forRegistry(metrics)
      .convertRatesTo(TimeUnit.SECONDS)
      .convertDurationsTo(TimeUnit.MILLISECONDS)
      .build();
    reporter.start(1, TimeUnit.SECONDS);

    timer
  }

  val logMessages = TableQuery[LogMessages]

  val host = "localhost"
  val port = "5432"
  val database = "log"
  val user = "postgres"
  val password = "HOew6xq31J7rBBA"
  val driver = "org.postgresql.Driver"
  val jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + database

  val db = Database.forURL(jdbcUrl, user, password, driver = driver)
  val timer = initializeMetrics
  "LogTest" should "de able to log" in {
    do {
      db.withSession {
        implicit session =>
          val context: Timer.Context = timer.time();
          val q=for {
            log <- logMessages if log.message like "%Alex%"
          } yield log
          println(q.length.run)
          context.stop
      }
    } while (true)
  }
}