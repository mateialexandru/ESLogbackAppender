package com.busymachines.logback

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

class LogstashSQLAppenderLayout[E](sourceHost: String = java.net.InetAddress.getLocalHost.getHostName, defaultTags: Seq[String] = Nil) extends LayoutBase[E] {
  @BeanProperty
  var applicationName: String = _

  private val TAG_REGEX: Regex = """(?iu)\B#([^,#=!\s./]+)([\s,.]|$)""".r

  private def parseTags(msg: String) = {
    TAG_REGEX.findAllIn(msg).matchData.map(_.group(1).toUpperCase(Locale.ENGLISH)).toSeq
  }

  def doLayout(event: E) = {
    try {
      val e = event.asInstanceOf[ILoggingEvent]
      val tags = parseTags(e.getFormattedMessage())
      iLoggingEventToSQLLogstashMessage(e, sourceHost,
        (tags.isEmpty, defaultTags.isEmpty) match {
          case (true, true) => Nil
          case (false, true) => tags
          case (true, false) => defaultTags
          case (false, false) => tags ++ defaultTags
        })
    } catch {
      case x: Throwable =>
        x.printStackTrace()
        null
    }
  }

  def prepareForDb(event: E) = {
    try {
      val e = event.asInstanceOf[ILoggingEvent]
      val tags = parseTags(e.getFormattedMessage())
      iLoggingEventToSQLLogstashMessage(e, sourceHost,
        (tags.isEmpty, defaultTags.isEmpty) match {
          case (true, true) => Nil
          case (false, true) => tags
          case (true, false) => defaultTags
          case (false, false) => tags ++ defaultTags
        })
    } catch {
      case x: Throwable =>
        x.printStackTrace()
        null
    }
  }

  private def iLoggingEventToSQLLogstashMessage(e: ILoggingEvent, sourceHost: String, tags: Seq[String]): String = {

    val (cause, stackTraces) = toOption(e.getThrowableProxy) match {
      case None => (None, Nil)
      case Some(proxy) =>
        (toOption(proxy.getCause()),
          proxy.getStackTraceElementProxyArray.map(_.getStackTraceElement()) toSeq)
    }

    s"${e.getLoggerName},${sourceHost},${cause match { case None => "" case Some(c) => c.getClassName }},${cause match { case None => "" case Some(c) => c.getClassName }},${e.getFormattedMessage},${e.getTimeStamp()},${cause match { case None => "" case Some(c) => c.getClassName }},org.apache.log4j.Category,${e.getLevel().levelStr},${e.getThreadName()},0,${LogHelper.stackTracesToString(stackTraces)},"
  }

}

case class LogMessageRow(
  id: Option[Int] = None,
  source: String = "file: //appender-test/Log4jExample.java/foo.Log4jExample/main",
  sourceHost: String = "appender-test",
  sourcePath: String = "foo.Log4jExample",
  file: String = "Log4jExample.java",
  message: String = "Hello this is an fatal message",
  timestamp: DateTime = DateTime.now,
  classFile: String = "foo.Log4jExample",
  fqn: String = "org.apache.log4j.Category",
  level: String = "FATAL",
  thread: String = "main",
  lineNumber: Int = 0,
  stackTrace: String = "java.io.IOException: Ithrewanexception\\n\\tatfoo.Log4jExample.main(Log4jExample.java: 20)")
// Definition of the SQL LogMessage table
class LogMessages(tag: Tag) extends Table[(Option[Int], String, String, String, String, String, Option[Timestamp], String, String, String, String, String, String)](tag, "LOGMESSAGES") {
  def id = column[Int]("ID", O.PrimaryKey, O.AutoInc) // This is the primary key column
  def source = column[String]("SOURCE")
  def sourceHost = column[String]("SOURCE_HOST")
  def sourcePath = column[String]("SOURCE_PATH")
  def file = column[String]("FILE")
  def message = column[String]("MESSAGE")
  def timestamp = column[Timestamp]("TIMESTAMP", O.Nullable)
  def classFile = column[String]("CLASS_FILE")

  def fqn = column[String]("FQN")
  def level = column[String]("LEVEL")
  def thread = column[String]("THREAD")
  def lineNumber = column[String]("LINE_NUMBER")
  def stackTrace = column[String]("STACK_TRACE", DBType("VARCHAR(30000)"))
  // Every table needs a * projection with the same type as the table's type parameter
  def * = (id.?, source, sourceHost, sourcePath, file, message, timestamp.?, classFile, fqn, level, thread, lineNumber, stackTrace)
}

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