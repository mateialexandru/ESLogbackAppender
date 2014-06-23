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
