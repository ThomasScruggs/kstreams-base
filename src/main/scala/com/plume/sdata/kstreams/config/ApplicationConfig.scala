package com.plume.sdata.kstreams.config

trait ApplicationConfig {
  val deployment: String
  val kafkaConfig: KafkaConfig
  val s3Config: S3SinkConfig = null
}
