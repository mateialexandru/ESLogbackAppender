package app

import java.io.{IOException, FileNotFoundException}

import grizzled.slf4j.Logging

import scala.util.Random

/**
 * Created by alex on 23.06.2014.
 */
class MessageGeneratorThread extends Runnable with Logging{
  def run={
    println("Thread started!")
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
}
