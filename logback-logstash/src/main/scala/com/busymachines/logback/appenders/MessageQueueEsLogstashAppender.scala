package com.busymachines.logback.appenders

import java.io.File
import java.util.concurrent.LinkedBlockingQueue
import com.busymachines.logback.LogHelper.toList
import ch.qos.logback.core.{Layout, UnsynchronizedAppenderBase}
import com.busymachines.logback.LogstashAppenderLayout
import com.codahale.metrics.{Timer, ConsoleReporter, CsvReporter, MetricRegistry}
import java.util.concurrent.TimeUnit
import java.util.Locale
import scala.beans.BeanProperty

/**
 * Created by alex on 23.06.2014.
 */
class MessageQueueEsLogstashAppender[E] extends UnsynchronizedAppenderBase[E] {

  val messageQueue = new LinkedBlockingQueue[String](15024)

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

  def intializeMessageConsumer = new Thread(new MessageConsumer(messageQueue, ESConfig(indexNamePrefix, indexNameDateFormat, clusterName, indexDocumentType, hostNames, port, logstashTags, logstashIdentity, logstashSourceHost))).start()

  def initializeMetrics = {
    val metrics: MetricRegistry = new MetricRegistry();
    val timer: com.codahale.metrics.Timer = metrics.timer("MQESLogstashAppenderTimer")
    val csvReporter: CsvReporter = CsvReporter.forRegistry(metrics)
      .formatFor(Locale.US)
      .convertRatesTo(TimeUnit.SECONDS)
      .convertDurationsTo(TimeUnit.MILLISECONDS)
      .build(new File("/home/alex/Documents/Thesis/logger/metrics/MessageQueueESAppender/"));
val reporter: ConsoleReporter = ConsoleReporter.forRegistry(metrics)
      .convertRatesTo(TimeUnit.SECONDS)
      .convertDurationsTo(TimeUnit.MILLISECONDS)
      .build();
    reporter.start(1, TimeUnit.SECONDS);

    timer
  }

  val defaultApplicationName = "app"

  lazy val layout: Layout[E] = new LogstashAppenderLayout(getLogstashSourceHost, toList(getLogstashTags))

  val y=intializeMessageConsumer

  val timer = initializeMetrics

  def append(eventObject: E) =
    send(layout.doLayout(eventObject))

  private def send(data: String) = {
    val context: Timer.Context = timer.time();
    try {
      messageQueue.put(data)
    } catch {
      case ex: Exception =>
    } finally {
      context.stop()
    }
  }

  override def stop = {
    //    client.close
    super.stop
  }
}
