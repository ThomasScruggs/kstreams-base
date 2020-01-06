package com.plume.sdata.kstreams.driver

import java.time.Duration
import java.util.concurrent.TimeUnit

import com.codahale.metrics.{ConsoleReporter, Reporter, ScheduledReporter}
import com.codahale.metrics.jmx.JmxReporter
import com.plume.sdata.kstreams.config.ApplicationConfig
import com.plume.sdata.kstreams.engine.Engine
import nl.grons.metrics4.scala.DefaultInstrumented

import scala.sys.ShutdownHookThread

trait Driver extends DefaultInstrumented {

  /**
    * Parse the Application config given the passed path.Usually this overrides the default values which are in the
    * resources. Implementers will have to cast to their specific implementation so that appropriate engine configs
    * can be passed to the various engines for this service
    *
    * @param path - The path that contains the type safe config.
    * @return the application config for this service
    */
  def parseAppConfig(path: String): ApplicationConfig

  /**
    * Get the list of engines that will be run.Implementers will have to cast ApplicationConfig to their specific
    * implementation so that appropriate engine configs  can be passed to the various engines for this service
    *
    * @param config - The parsed application config from above
    * @return The list of engines(topologies) that will be run
    */
  def getEngines(config: ApplicationConfig): List[Engine]

  /**
    * Run the app
    * @param engines - Run the topology in the engines
    * @param metricReporters - The various metric reporters to output the metrics reported by the engines
    * @return
    */
  def run(engines: List[Engine], metricReporters: List[Reporter]): ShutdownHookThread = {
    val streams = engines.map { e => {
      val topology = e.topology(e.s3Client, e.topics: _*)
      e.run(topology)
    }
    }
    metricReporters.foreach(m => {
      if (m.isInstanceOf[ConsoleReporter]) {
        m.asInstanceOf[ScheduledReporter].start(60, TimeUnit.SECONDS)
      }
      else {
        m.asInstanceOf[JmxReporter].start()
      }
    })
    sys.ShutdownHookThread {
      streams.foreach(s => s.close(Duration.ofSeconds(30)))
    }
  }

  //metrics reporters
  /**
    * If you would like to report metrics on the console add this to your list before calling run
    */
  lazy val consoleReporter: ConsoleReporter = ConsoleReporter.forRegistry(metricRegistry)
    .convertRatesTo(TimeUnit.MINUTES)
    .convertDurationsTo(TimeUnit.MINUTES)
    .build()

  /**
    * If you would like JMX metrics add this to your list before calling run
    */
  lazy val jmxReporter: JmxReporter = JmxReporter.forRegistry(metricRegistry)
    .convertRatesTo(TimeUnit.MINUTES)
    .convertDurationsTo(TimeUnit.MINUTES)
    .build()
}
