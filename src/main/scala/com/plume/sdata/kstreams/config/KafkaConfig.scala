package com.plume.sdata.kstreams.config

import java.util.Properties

import com.plume.sdata.kstreams.utils.KStreamsProductionHandler
import com.typesafe.config.Config
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.errors.{DefaultProductionExceptionHandler, DeserializationExceptionHandler, LogAndContinueExceptionHandler}

import scala.collection.JavaConverters._

case class KafkaConfig(bootstrapServers: List[String],
                       streamThreads: Int,
                       retentionBytes: Long,
                       retentionMs: Long,
                       offsetReset: String,
                       topicPrefix: String,
                       retries: Long = 5,
                       requestTimeoutMs: Long = 240000,
                       retryBackoffMs: Long = 60000,
                       replicationFactor: Int = 2) {

  def toProperties(applicationId: String,
                   defaultProductionExceptionHandler:DefaultProductionExceptionHandler = new KStreamsProductionHandler(),
                   deserializationExceptionHandler: DeserializationExceptionHandler = new LogAndContinueExceptionHandler()): Properties = {
    val kafkaProps = new Properties()
    kafkaProps.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId)
    kafkaProps.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers.mkString(","))
    kafkaProps.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, streamThreads.toString)
    kafkaProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, offsetReset)
    kafkaProps.put(StreamsConfig.RETRIES_CONFIG, retries.toString)
    kafkaProps.put(StreamsConfig.REQUEST_TIMEOUT_MS_CONFIG, requestTimeoutMs.toString)
    kafkaProps.put(StreamsConfig.RETRY_BACKOFF_MS_CONFIG, retryBackoffMs.toString)
    kafkaProps.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, retryBackoffMs.toString)
    kafkaProps.put(StreamsConfig.REPLICATION_FACTOR_CONFIG, replicationFactor.toString)
    kafkaProps.put(StreamsConfig.topicPrefix("retention.bytes"), retentionBytes.toString)
    kafkaProps.put(StreamsConfig.topicPrefix("retention.ms"), retentionMs.toString)
    kafkaProps.put(StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG, deserializationExceptionHandler.getClass)
    kafkaProps.put(StreamsConfig.DEFAULT_PRODUCTION_EXCEPTION_HANDLER_CLASS_CONFIG, defaultProductionExceptionHandler.getClass)
    kafkaProps
  }
}

object KafkaConfig {
  def apply(cfg: Config): KafkaConfig = {
    KafkaConfig(cfg.getStringList("bootstrapServers").asScala.toList,
      cfg.getInt("streamThreads"),
      cfg.getLong("retentionBytes"),
      cfg.getLong("retentionMs"),
      cfg.getString("offsetReset"),
      cfg.getString("topicPrefix"))
  }
}

