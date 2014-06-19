package com.busymachines.logback

import org.scalatest.FlatSpec
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
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
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.search.facet.terms.TermsFacetBuilder
import org.elasticsearch.search.facet.terms.TermsFacet
import org.elasticsearch.index.query.QueryBuilder
import org.elasticsearch.search.facet.FacetBuilders
import org.elasticsearch.index.query.QueryBuilders

@RunWith(classOf[JUnitRunner])
class LogTes2t extends FlatSpec with grizzled.slf4j.Logging {

  def initializeMetrics = {
    val metrics: MetricRegistry = new MetricRegistry();
    val timer: com.codahale.metrics.Timer = metrics.timer("ESFacetTimer")
    val reporter: ConsoleReporter = ConsoleReporter.forRegistry(metrics)
      .convertRatesTo(TimeUnit.SECONDS)
      .convertDurationsTo(TimeUnit.MILLISECONDS)
      .build();
    reporter.start(1, TimeUnit.SECONDS);

    timer
  }

  val indexNamePrefix: String = "logstash"
  val indexNameDateFormat: String = "YYYY.MM.dd"
  val clusterName = "elasticsearch"
  val indexDocumentType = "logs"
  val hostNames = ""
  val port = "9300"

  val client = new TransportClient(ImmutableSettings.settingsBuilder().put("cluster.name", clusterName))
    .addTransportAddresses(hostNames.split(",").map(new InetSocketTransportAddress(_, port.toInt)): _*)
  val actualIndexName = s"$indexNamePrefix-${DateTimeFormat.forPattern(indexNameDateFormat).print(DateTime.now)}"

  val facet: TermsFacetBuilder = FacetBuilders.termsFacet("level").field("@message");
  val query = QueryBuilders.queryString("Alex");

  val timer = initializeMetrics

  "LogTest" should "de able to log" in {

    while (true) {
      val context: Timer.Context = timer.time


        val response2 =client.prepareSearch()
        .addFacet(facet)
        .setQuery(query)
        .execute().actionGet().getHits().getTotalHits()
        
        println(response2)
      context.stop
      //    println(response)
    }
  }
}