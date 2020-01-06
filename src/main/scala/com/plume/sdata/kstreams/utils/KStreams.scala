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

  /**
    * If there are joins between topics that dont have the same number of partitions make the smaller one bigger
    * @param topic - The topic for which repartitioning is desired
    * @param sourceTopic - The source topic to match the copartitioning to
    * @param properties - The kafka properties
    * @param extraSuffix - Any extra suffix you want to add to the new topic.
    * @return the new topic as a result of the copartitioning
    */
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

  /**
    * Convert a function that can be used as a punctuator without inconvinincing the user
    * See the s3 writer as an example
    * @param fn - The function to wrap asa punctuator
    */
  implicit class PunctuatorWrapper(fn: Long => Unit) extends Punctuator {
    override def punctuate(timestamp: Long): Unit = fn(timestamp)
  }
}




