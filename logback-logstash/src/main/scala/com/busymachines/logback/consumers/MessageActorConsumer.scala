package com.busymachines.logback.consumers

import com.busymachines.logback.misc.ESConfig
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.settings.ImmutableSettings
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

import scala.actors.Actor


/**
 * Created by alex on 23.06.2014.
 */

class MessageActorConsumer (esConfig: ESConfig) extends Actor {

  lazy val client = new TransportClient (ImmutableSettings.settingsBuilder ().put ("cluster.name", esConfig.clusterName))
    .addTransportAddresses (esConfig.hostNames.split (",").map (new InetSocketTransportAddress (_, esConfig.port.toInt)): _*)

  lazy val actualIndexName = s"${esConfig.indexNamePrefix}-${DateTimeFormat.forPattern (esConfig.indexNameDateFormat).print (DateTime.now)}"

  def act =
    loop {
      react {
        case message:String =>
          try {
            client.prepareIndex (
              actualIndexName, esConfig.indexDocumentType)
              .setSource(message)
              .execute ()
              .actionGet ()
          } catch {
            case ex: Exception => println (ex)
          }
        case _ =>
      }
    }
}
