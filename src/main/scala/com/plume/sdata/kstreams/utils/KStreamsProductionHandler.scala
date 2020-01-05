package com.plume.sdata.kstreams.utils

import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.streams.errors.DefaultProductionExceptionHandler
import org.apache.kafka.streams.errors.ProductionExceptionHandler.ProductionExceptionHandlerResponse
import org.slf4j.LoggerFactory

class KStreamsProductionHandler extends  DefaultProductionExceptionHandler {
  val logger = LoggerFactory.getLogger(this.getClass)
  override def handle(record: ProducerRecord[Array[Byte], Array[Byte]],
                      exception: Exception): ProductionExceptionHandlerResponse = {
    logger.warn("Produce records failed with exception " +  exception)
    ProductionExceptionHandlerResponse.CONTINUE
  }
}
