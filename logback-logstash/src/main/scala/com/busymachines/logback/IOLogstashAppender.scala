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
import java.io.File
import com.codahale.metrics.{ Timer, MetricRegistry, ConsoleReporter, CsvReporter }
import java.util.concurrent.TimeUnit
import java.io.FileWriter
import java.io.PrintWriter
import java.io.Writer

class IOLogstashAppender[E] extends UnsynchronizedAppenderBase[E] {

  def intializeMetrics = {
    val metrics: MetricRegistry = new MetricRegistry();
    val timer: com.codahale.metrics.Timer = metrics.timer("IOLogstashAppenderTimer")
    val reporter: ConsoleReporter = ConsoleReporter.forRegistry(metrics)
      .convertRatesTo(TimeUnit.SECONDS)
      .convertDurationsTo(TimeUnit.MILLISECONDS)
      .build();
    val csvReporter: CsvReporter = CsvReporter.forRegistry(metrics)
      .formatFor(Locale.US)
      .convertRatesTo(TimeUnit.SECONDS)
      .convertDurationsTo(TimeUnit.MILLISECONDS)
      .build(new File("/home/alex/Documents/Thesis/logger/metrics/IOAppender/"));
    // reporter.start(1, TimeUnit.MILLISECONDS);
    csvReporter.start(1, TimeUnit.SECONDS);
    timer
  }

  @BeanProperty var filePath: String = "/home/alex/Documents/Thesis/logger/logsIO.txt"

  lazy val layout: Layout[E] = new LogstashAppenderLayout

  val writer: Writer = new PrintWriter(new File(filePath))

  val timer = intializeMetrics

  def append(eventObject: E) =
    send(layout.doLayout(eventObject))

  private def send(data: String) = {

    val context: Timer.Context = timer.time();
    try {
      writer.append(data)
      writer.flush()
    } catch {
      case ex: Exception =>
    } finally {
      context.stop();
    }
  }

  override def stop = {
  }
}