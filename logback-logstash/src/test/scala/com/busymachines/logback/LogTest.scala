package com.busymachines.logback

import org.scalatest.FlatSpec
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class LogTest3 extends FlatSpec with grizzled.slf4j.Logging {
  "LogTest" should "de able to log" in {
    do {
     // debug("test Hello paul 1", new IllegalArgumentException("some exception message 1"))
      debug("test Hello yoo 2", new IndexOutOfBoundsException("get out of here!"))
    } while (true)
  }
}