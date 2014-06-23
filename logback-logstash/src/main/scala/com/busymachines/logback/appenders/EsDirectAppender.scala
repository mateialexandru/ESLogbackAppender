package com.busymachines.logback.appenders

import ch.qos.logback.classic.spi.{ ILoggingEvent }
import ch.qos.logback.core.{ Layout, LayoutBase, UnsynchronizedAppenderBase }
import com.busymachines.logback.LogstashAppenderLayout
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

class EsDirectAppender[E] extends UnsynchronizedAppenderBase[E] {

  def initializeMetrics = {
    val metrics: MetricRegistry = new MetricRegistry();
    val timer: com.codahale.metrics.Timer = metrics.timer("EsLogstashAppenderTimer")
    val reporter: ConsoleReporter = ConsoleReporter.forRegistry(metrics)
      .convertRatesTo(TimeUnit.SECONDS)
      .convertDurationsTo(TimeUnit.MILLISECONDS)
      .build();
    val reporter2: CsvReporter = CsvReporter.forRegistry(metrics)
      .formatFor(Locale.US)
      .convertRatesTo(TimeUnit.SECONDS)
      .convertDurationsTo(TimeUnit.MILLISECONDS)
      .build(new File("/home/alex/Documents/Thesis/logger/metrics/ESAppender/"));
     reporter.start(1, TimeUnit.SECONDS);
    //reporter2.start(1, TimeUnit.SECONDS);

    timer
  }

  @BeanProperty var indexNamePrefix: String = "logstash"
  @BeanProperty var indexNameDateFormat: String = "YYYY.MM.dd"
  @BeanProperty var clusterName = "elasticsearch"
  @BeanProperty var indexDocumentType = "logs"
  @BeanProperty var hostNames = ""
  @BeanProperty var port = "9300"

  /* Logstash specific settings */
  @BeanProperty var logstashTags: String = "test, development"
  @BeanProperty var logstashIdentity: String = "appender-test"
  @BeanProperty var logstashSourceHost: String = java.net.InetAddress.getLocalHost.getHostName

  val defaultApplicationName = "app"

  lazy val layout: Layout[E] = new LogstashAppenderLayout(getLogstashSourceHost, toList(getLogstashTags))

  lazy val client = new TransportClient(ImmutableSettings.settingsBuilder().put("cluster.name", clusterName))
    .addTransportAddresses(hostNames.split(",").map(new InetSocketTransportAddress(_, port.toInt)): _*)

  lazy val actualIndexName = s"$indexNamePrefix-${DateTimeFormat.forPattern(indexNameDateFormat).print(DateTime.now)}"

  val timer = initializeMetrics

  def append(eventObject: E) =
    send(layout.doLayout(eventObject))

  private def send(data: String) = {
    val context: Timer.Context = timer.time
    try{
    client.prepareIndex(
      actualIndexName, indexDocumentType)
      .setSource(data)
      .execute
      .actionGet
    }catch{
      case ex:Exception=>
    }
    finally{
      context.stop
    }
  }

  override def stop = {
    client.close
    super.stop
  }
}