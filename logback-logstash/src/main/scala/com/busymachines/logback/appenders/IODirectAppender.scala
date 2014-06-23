package com.busymachines.logback.appenders

import java.io.{File, PrintWriter, Writer}
import java.util.Locale
import java.util.concurrent.TimeUnit

import ch.qos.logback.core.{Layout, UnsynchronizedAppenderBase}
import com.busymachines.logback.LogstashAppenderLayout
import com.codahale.metrics.{ConsoleReporter, CsvReporter, MetricRegistry, Timer}

import scala.beans.BeanProperty

class IODirectAppender[E] extends UnsynchronizedAppenderBase[E] {

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