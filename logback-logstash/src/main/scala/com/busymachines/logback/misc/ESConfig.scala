package com.busymachines.logback.misc

/**
 * Created by alex on 23.06.2014.
 */
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
