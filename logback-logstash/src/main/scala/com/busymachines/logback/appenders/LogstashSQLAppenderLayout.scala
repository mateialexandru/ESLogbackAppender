package com.busymachines.logback.appenders

import ch.qos.logback.classic.spi.{ ILoggingEvent }
import ch.qos.logback.core.{ Layout, LayoutBase, UnsynchronizedAppenderBase }
import com.busymachines.logback.LogHelper
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
