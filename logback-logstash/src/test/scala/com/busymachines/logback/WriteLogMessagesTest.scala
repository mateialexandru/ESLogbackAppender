package com.busymachines.logback

import java.io.{IOException, FileNotFoundException}

import app.MessageGeneratorThread
import org.scalatest.FlatSpec
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import scala.util.Random

@RunWith(classOf[JUnitRunner])
class WriteLogMessagesTest extends FlatSpec with grizzled.slf4j.Logging {



  "WriteLogMessagesTest" should " log messages" in {
    do {
      if(Random.nextInt(4)==3)
        debug("Bad arguments sent", new IllegalArgumentException("some exception message 1"))
      if(Random.nextInt(20)==1)
        debug("Reached end of list", new IndexOutOfBoundsException("some exception message 2"))

      if(Random.nextInt(10)==3)
        info("File could not be found", new FileNotFoundException("input.txt"))

      if(Random.nextInt(5)==1)
        error("Fatal error! IO failure", new IOException("some exception message 3"))

      if(Random.nextInt(20)==1)
        error("Runtime exception", new RuntimeException("some expections message 4"))
    } while (true)
  }

  "WriteLogMessagesTest" should "start 15 threads that continuously log messages" in {
   for(x <- 1 to 15)
     new Thread(new MessageGeneratorThread()).start()
  }
  while(true){}
}