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
import java.nio.file.Files
import java.nio.file.Paths
import java.io.File
import java.nio.file.StandardOpenOption
import com.codahale.metrics.{ Timer, MetricRegistry, ConsoleReporter, CsvReporter }
import java.util.concurrent.TimeUnit

class NIOLogstashAppender[E] extends UnsynchronizedAppenderBase[E] {

  def intializeMetrics = {
    val metrics: MetricRegistry = new MetricRegistry();
    val timer: com.codahale.metrics.Timer = metrics.timer("NIOLogstashAppenderTimer")
    val reporter: ConsoleReporter = ConsoleReporter.forRegistry(metrics)
      .convertRatesTo(TimeUnit.SECONDS)
      .convertDurationsTo(TimeUnit.MILLISECONDS)
      .build();
    val reporter2: CsvReporter = CsvReporter.forRegistry(metrics)
      .formatFor(Locale.US)
      .convertRatesTo(TimeUnit.SECONDS)
      .convertDurationsTo(TimeUnit.MILLISECONDS)
      .build(new File("/home/alex/Documents/Thesis/logger/metrics/NIOAppender/"));
    //    reporter.start(1, TimeUnit.MILLISECONDS);
    reporter2.start(1, TimeUnit.SECONDS);
    timer
  }

  @BeanProperty var filePath: String = "/home/alex/Documents/Thesis/logger/logsNIO.txt"

  val defaultApplicationName = "app"

  lazy val layout: Layout[E] = new LogstashAppenderLayout

    val timer = intializeMetrics

  def append(eventObject: E) =
    send(layout.doLayout(eventObject))

  private def send(data: String) = {

    val context: Timer.Context = timer.time();
    try {
      Files.write(Paths.get(new File(filePath).toURI()), data.getBytes("utf-8"), StandardOpenOption.CREATE, StandardOpenOption.APPEND)
    } catch {
      case ex: Exception =>
    } finally {
      context.stop();
    }
  }

  override def stop = {

  }
}