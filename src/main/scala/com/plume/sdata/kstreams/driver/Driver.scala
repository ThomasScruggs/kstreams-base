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
  def parseAppConfig(path: String): ApplicationConfig

  def getEngines(config: ApplicationConfig): List[Engine]

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
  lazy val consoleReporter: ConsoleReporter = ConsoleReporter.forRegistry(metricRegistry)
    .convertRatesTo(TimeUnit.MINUTES)
    .convertDurationsTo(TimeUnit.MINUTES)
    .build()

  lazy val jmxReporter: JmxReporter = JmxReporter.forRegistry(metricRegistry)
    .convertRatesTo(TimeUnit.MINUTES)
    .convertDurationsTo(TimeUnit.MINUTES)
    .build()
}
