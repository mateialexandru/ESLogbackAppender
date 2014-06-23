package com.busymachines.logback.appenders

import org.joda.time.DateTime

/**
 * Created by alex on 23.06.2014.
 */
case class LogMessageRow(
  id: Option[Int] = None,
  source: String = "file: //appender-test/Log4jExample.java/foo.Log4jExample/main",
  sourceHost: String = "appender-test",
  sourcePath: String = "foo.Log4jExample",
  file: String = "Log4jExample.java",
  message: String = "Hello this is an fatal message",
  timestamp: DateTime = DateTime.now,
  classFile: String = "foo.Log4jExample",
  fqn: String = "org.apache.log4j.Category",
  level: String = "FATAL",
  thread: String = "main",
  lineNumber: Int = 0,
  stackTrace: String = "java.io.IOException: Ithrewanexception\\n\\tatfoo.Log4jExample.main(Log4jExample.java: 20)")
