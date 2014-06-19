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
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

class MessageConsumer(messageQueue: LinkedBlockingQueue[String], esConfig: ESConfig) extends Runnable {

  lazy val client = new TransportClient(ImmutableSettings.settingsBuilder().put("cluster.name", esConfig.clusterName))
    .addTransportAddresses(esConfig.hostNames.split(",").map(new InetSocketTransportAddress(_, esConfig.port.toInt)): _*)

  lazy val actualIndexName = s"${esConfig.indexNamePrefix}-${DateTimeFormat.forPattern(esConfig.indexNameDateFormat).print(DateTime.now)}"

  def run() {
    do {
      try{
      
      client.prepareIndex(
        actualIndexName, esConfig.indexDocumentType)
        .setSource(messageQueue.take)
        .execute()
        .actionGet()
      }catch{
        case ex:Exception=>println(ex)
      }
    }while(true)
  }
}
case class ESConfig(
  indexNamePrefix: String,
  indexNameDateFormat: String,
  clusterName: String,
  indexDocumentType: String,
  hostNames: String,
  port: String,
  logstashTags: String,
  logstashIdentity: String,
  logstashSourceHost: String)

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