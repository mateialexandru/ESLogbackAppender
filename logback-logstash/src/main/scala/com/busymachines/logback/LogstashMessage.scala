package com.busymachines.logback

import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat

case class LogstashMessage(
    source:String="file: //appender-test/Log4jExample.java/foo.Log4jExample/main",
    sourceHost:String="appender-test",
    sourcePath:String="foo.Log4jExample",
    file:String="Log4jExample.java",
    message:String="Hello this is an fatal message",
    tags:Seq[String]=Nil,
    timestamp:DateTime=DateTime.now(DateTimeZone.UTC),
    classFile:String="foo.Log4jExample",
    fqn:String="org.apache.log4j.Category",
    level:String="FATAL",
    thread:String="main",
    lineNumber:Int=0,
    stackTrace:String="java.io.IOException: Ithrewanexception\\n\\tatfoo.Log4jExample.main(Log4jExample.java: 20)"
    ) {

  def toJson = 
  s"""
	{
	    "@source": "$source",
	    "@source_host": "$sourceHost",
	    "@source_path": "$sourcePath",
	    "@file": "$file",
	    "@message": "$message",
	    "@tags": [${tags.mkString("\"","\",\"","\"")}],
	    "@timestamp": "${timestamp}",
	    "@class_file": "$classFile",
	    "@fqn": "$fqn",
	    "@level": "$level",
	    "@thread": "$thread",
	    "@line_number": "$lineNumber",
	    "@stacktrace": "$stackTrace"
	}  
  """
}