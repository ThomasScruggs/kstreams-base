package com.plume.sdata.kstreams.engine

import java.time.Duration

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.services.s3.{AmazonS3, AmazonS3ClientBuilder}
import com.plume.sdata.kstreams.config.{EngineConfig, KafkaConfig, S3SinkConfig}
import nl.grons.metrics4.scala.MetricBuilder
import org.apache.kafka.streams.{KafkaStreams, Topology}
import org.slf4j.Logger

/**
  * An engine processes realtime streams and produces/serializes alerts from kafka sources
  * Drivers create instances of engines and follow the lifecycle
  * - init
  * - create a pipeline(topology) that will process the stream
  * - run the pipeline that will produce alerts and serialize them
  * - finally if we stop the pipeline to cleanup any state appropriately
  */
trait Engine  {
  val name: String
  val kafkaConfig: KafkaConfig
  val engineConfig: EngineConfig
  val s3Config: S3SinkConfig
  val metrics: MetricBuilder

  /**
    * The definition of the pipeline
    * @param s3Client - The client used to serialize to S3
    * @param topics - The topics that this pipeline will process
    * @return the pipeline to rum
    */
  def topology(s3Client: AmazonS3, topics: String*): Topology

  /**
    * Where all processing happens
    * @param topology - the pipeline to run
    * @return the running stream so that it can be cleaned up on exit
    */
  def run(topology: Topology): KafkaStreams = {
    logger.info(name + "\n" + topology.describe())
    val streams = new KafkaStreams(topology, kafkaConfig.toProperties(engineConfig.applicationId))
    //streams.cleanUp() //required when we do changes to objects that contain state
    streams.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler {
      override def uncaughtException(thread: Thread, throwable: Throwable): Unit = {
        logger.error("Streams failed " + thread.getName + " " + throwable.toString)
        System.exit(1)
      }
    })
    streams.start()
    streams
  }

  /**
    * Collect all garbage and close the stream
    * @param stream - The running pipeline
    */
  def cleanup(stream: KafkaStreams): Unit = {
    stream.close(Duration.ofSeconds(30))
  }

  /**
    * List of topics used by this engine
    * @return Each engine specifies the list of topics it processes building it appropriately from config
    */
  def topics: List[String]

  /**
    * The logger used to log
    */
  val logger: Logger

  /**
    * S3Client for writing alerts
    * @return The default s3 client that can be used by the engine to serialize data
    */
  def s3Client: AmazonS3 = {
    val s3ClientBuilder = AmazonS3ClientBuilder.standard().withCredentials(new DefaultAWSCredentialsProviderChain())
    s3ClientBuilder.build
  }
}
