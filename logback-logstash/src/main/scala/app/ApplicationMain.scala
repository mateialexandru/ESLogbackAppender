package app

import grizzled.slf4j.Logging

/**
 * Created by alex on 23.06.2014.
 */
object Application extends Logging {
  def main(args: Array[String]): Unit = {
    println("Logging continously!")

    for (i<-1 to 15)
      new Thread(new MessageGeneratorThread).start

  }
}
