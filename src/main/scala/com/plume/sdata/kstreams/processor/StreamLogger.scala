package com.plume.sdata.kstreams.processor

import org.apache.kafka.streams.processor.{AbstractProcessor, Processor, ProcessorSupplier}
import org.apache.kafka.streams.scala.kstream.KStream
import org.slf4j.Logger
import org.slf4j.event.Level

case class StreamLogger[K, V](logger: Logger = null, level: Level = Level.DEBUG)(label: String = "")
  extends ProcessorSupplier [K, V] {
  override def get(): Processor[K, V] = new AbstractProcessor[K, V] {
    override def process(key: K, value: V): Unit = {
      val logStatement = s"[$label] [$key] [$value]"
      if(logger == null) {
        println(logStatement)
      } else {
        level match {
          case Level.DEBUG => logger.debug(logStatement)
          case Level.INFO => logger.info(logStatement)
          case Level.WARN => logger.warn(logStatement)
          case Level.ERROR => logger.error(logStatement)
          case Level.TRACE => logger.trace(logStatement)
        }
      }
      context.forward(key, value)
    }
  }
  def print(stream: KStream[K, V]): Unit = stream.process(() =>get())
}

