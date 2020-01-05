package com.plume.sdata.kstreams.utils

import java.util.Properties

import org.apache.kafka.clients.admin.{AdminClient, NewTopic}
import org.apache.kafka.streams.processor.Punctuator

import scala.collection.JavaConverters._

object KStreams {

  private def getPartitionCountsForTopic(topic: String, properties: Properties): Int = {
    AdminClient.create(properties).describeTopics(List(topic).asJava)
      .values()
      .get(topic)
      .get
      .partitions()
      .size()
  }

  def createTopicForCoPartitioning(topic: String, sourceTopic: String,
                                   properties:Properties, extraSuffix:String = ""): String = {
    try {
      val partitionCount = getPartitionCountsForTopic(sourceTopic, properties)
      val targetPartitionCount = getPartitionCountsForTopic(topic, properties)
      if (partitionCount != targetPartitionCount) {
        val newTopic = new NewTopic(topic + ".copartitioned" + extraSuffix, partitionCount, 1)
        AdminClient.create(properties).createTopics(List(newTopic).asJavaCollection)
        newTopic.name()
      } else {
        topic
      }
    } catch {
      case _: Exception => topic
    }
  }

  implicit class PunctuatorWrapper(fn: Long => Unit) extends Punctuator {
    override def punctuate(timestamp: Long): Unit = fn(timestamp)
  }
}




