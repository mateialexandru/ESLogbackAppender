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

class LogstashAppenderLayout [E](sourceHost: String = java.net.InetAddress.getLocalHost.getHostName, defaultTags: Seq[String] = Nil) extends LayoutBase[E] {
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
      iLoggingEventToLogstashMessage(e, sourceHost,
        (tags.isEmpty, defaultTags.isEmpty) match {
          case (true, true) => Nil
          case (false, true) => tags
          case (true, false) => defaultTags
          case (false, false) => tags ++ defaultTags
        }).toJson
    } catch {
      case x: Throwable =>
        x.printStackTrace()
        null
    }
  }

  private def iLoggingEventToLogstashMessage(e: ILoggingEvent, sourceHost: String, tags: Seq[String]): LogstashMessage = {

    val (cause, stackTraces) = toOption(e.getThrowableProxy) match {
      case None => (None, Nil)
      case Some(proxy) =>
        (toOption(proxy.getCause()),
          proxy.getStackTraceElementProxyArray.map(_.getStackTraceElement()) toSeq)
    }

    new LogstashMessage(
      source = e.getLoggerName,
      sourceHost = sourceHost,
      sourcePath = cause match { case None => "" case Some(c) => c.getClassName },
      file = cause match { case None => "" case Some(c) => c.getClassName },
      message = e.getFormattedMessage,
      tags = tags,
      timestamp = new DateTime(e.getTimeStamp()),
      classFile = cause match { case None => "" case Some(c) => c.getClassName },
      fqn = "org.apache.log4j.Category",
      level = e.getLevel().levelStr,
      thread = e.getThreadName(),
      lineNumber = 0,
      stackTrace = LogHelper.stackTracesToString(stackTraces))
  }

}