package com.busymachines.logback.appenders

import java.io.File
import java.util.Locale
import java.util.concurrent.{LinkedBlockingQueue, TimeUnit}

import ch.qos.logback.core.{Layout, UnsynchronizedAppenderBase}
import com.busymachines.logback.LogHelper.toList
import com.busymachines.logback.LogstashAppenderLayout
import com.busymachines.logback.consumers.{MessageActorConsumer, MessageQueueConsumer}
import com.busymachines.logback.misc.ESConfig
import com.codahale.metrics.{ConsoleReporter, CsvReporter, MetricRegistry, Timer}

import scala.beans.BeanProperty

/**
 * Created by alex on 23.06.2014.
 */
class ActorEsAppender[E] extends UnsynchronizedAppenderBase[E] {

  @BeanProperty var indexNamePrefix: String = "logstash"
  @BeanProperty var indexNameDateFormat: String = "YYYY.MM.dd"
  @BeanProperty var clusterName = "elasticsearch"
  @BeanProperty var indexDocumentType = "logs"
  @BeanProperty var hostNames = ""
  @BeanProperty var port = "9300"

  /* Logstash specific settings */
  @BeanProperty var logstashTags: String = "paul1,paul2"
  @BeanProperty var logstashIdentity: String = "appender-test"
  @BeanProperty var logstashSourceHost: String = java.net.InetAddress.getLocalHost.getHostName

  def intializeMessageActor = new MessageActorConsumer (
    ESConfig (
      indexNamePrefix,
      indexNameDateFormat,
      clusterName,
      indexDocumentType,
      hostNames,
      port,
      logstashTags,
      logstashIdentity,
      logstashSourceHost))

  def initializeMetrics = {
    val metrics: MetricRegistry = new MetricRegistry ();
    val timer: com.codahale.metrics.Timer = metrics.timer ("ActorESAppenderTimer")
    val csvReporter: CsvReporter = CsvReporter.forRegistry (metrics)
      .formatFor (Locale.US)
      .convertRatesTo (TimeUnit.SECONDS)
      .convertDurationsTo (TimeUnit.MILLISECONDS)
      .build (new File ("/home/alex/Documents/Thesis/logger/metrics/ActorESAppender/"));
    val reporter: ConsoleReporter = ConsoleReporter.forRegistry (metrics)
      .convertRatesTo (TimeUnit.SECONDS)
      .convertDurationsTo (TimeUnit.MILLISECONDS)
      .build ();
    reporter.start (1, TimeUnit.MINUTES);

    timer
  }

  val defaultApplicationName = "app"

  lazy val layout: Layout[E] = new LogstashAppenderLayout (getLogstashSourceHost, toList (getLogstashTags))

  val actor = intializeMessageActor

  actor.start

  val timer = initializeMetrics

  def append (eventObject: E) =
    send (layout.doLayout (eventObject))

  private def send (data: String) = {
    val context: Timer.Context = timer.time ();
    try {
      actor ! data
    } catch {
      case ex: Exception =>
    } finally {
      context.stop ()
    }
  }

  override def stop = {
    //    client.close
    super.stop
  }
}
