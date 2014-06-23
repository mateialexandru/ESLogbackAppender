package com.busymachines.logback.appenders

import java.util.concurrent.LinkedBlockingQueue

import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.settings.ImmutableSettings
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

/**
 * Created by alex on 23.06.2014.
 */
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
