
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

object Testing {

  val indexNamePrefix: String = "logstash"        //> indexNamePrefix  : String = logstash
  val indexNameDateFormat: String = "YYYY.MM.dd"  //> indexNameDateFormat  : String = YYYY.MM.dd
  val clusterName = "elasticsearch"               //> clusterName  : String = elasticsearch
  val indexDocumentType = "logs"                  //> indexDocumentType  : String = logs
  val hostNames = ""                              //> hostNames  : String = ""
  val port = "9300"                               //> port  : String = 9300

  val client = new TransportClient(ImmutableSettings.settingsBuilder().put("cluster.name", clusterName))
    .addTransportAddresses(hostNames.split(",").map(new InetSocketTransportAddress(_, port.toInt)): _*)
                                                  //> SLF4J: The following loggers will not work because they were created
                                                  //| SLF4J: during the default configuration phase of the underlying logging sys
                                                  //| tem.
                                                  //| SLF4J: See also http://www.slf4j.org/codes.html#substituteLogger
                                                  //| SLF4J: org.elasticsearch.plugins
                                                  //| SLF4J: com.codahale.metrics.CsvReporter
                                                  //| SLF4J: org.elasticsearch.common.compress.lzf
                                                  //| client  : org.elasticsearch.client.transport.TransportClient = org.elastics
                                                  //| earch.client.transport.TransportClient@567c3f1b
  val actualIndexName = s"$indexNamePrefix-${DateTimeFormat.forPattern(indexNameDateFormat).print(DateTime.now)}"
                                                  //> actualIndexName  : String = logstash-2014.06.19

  val facet: TermsFacetBuilder = FacetBuilders.termsFacet("level").field("@message");
                                                  //> facet  : org.elasticsearch.search.facet.terms.TermsFacetBuilder = org.elast
                                                  //| icsearch.search.facet.terms.TermsFacetBuilder@147ed19
  val query = QueryBuilders.queryString("Alex");  //> query  : org.elasticsearch.index.query.QueryStringQueryBuilder = {
                                                  //|   "query_string" : {
                                                  //|     "query" : "Alex"
                                                  //|   }
                                                  //| }
  val response = client.prepareSearch()
    .addFacet(facet)
    .setQuery(query)
    .execute().actionGet().getFacets().facetsAsMap()
                                                  //> response  : java.util.Map[String,org.elasticsearch.search.facet.Facet] = {l
                                                  //| evel=org.elasticsearch.search.facet.terms.strings.InternalStringTermsFacet@
                                                  //| 104272b2}
  val r:Long=response.get("level").asInstanceOf[TermsFacet].getTotalCount()
                                                  //> r  : Long = 3940016|
  }