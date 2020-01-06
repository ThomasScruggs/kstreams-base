package com.plume.sdata.kstreams.config

trait ApplicationConfig {
  /**
    * The deployment that this streams service is running for
    */
  val deployment: String
  /**
    * The kafka config for the source
    */
  val kafkaConfig: KafkaConfig
  /**
    * Optional if the service writes to S3
    */
  val s3Config: S3SinkConfig = null
}
