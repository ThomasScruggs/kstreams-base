package com.plume.sdata.kstreams.processor

import java.net.URI
import java.time.Duration
import java.util.UUID

import com.amazonaws.services.s3.AmazonS3
import org.apache.kafka.streams.processor.{Processor, ProcessorContext, ProcessorSupplier, PunctuationType}
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.slf4j.LoggerFactory

import scala.collection.mutable

/**
  * This class allows a user to use a generic S3Writer processor that can write out text formats. All a user needs to do
  * is implement a processMessage that takes in the input type and returns a ProcessedMessage
  * @param s3Client - The AWS S3 Client
  * @param targetDir - The target dir(s3://bucket + path)
  * @param size - The number of messages before a flush
  * @param periodInMillis - The minimum time to wait prior to a flush if the number of messages is less than size
  * @tparam K - The kafka key
  * @tparam V - The kafka value
  */
abstract class S3Writer[K, V](s3Client: AmazonS3,
                              targetDir: String,
                              size: Long,
                              periodInMillis: Long) extends ProcessorSupplier[K, V] {

  def processMessage(key: K, message: V): ProcessedMessage

  def initState(): Boolean = true

  private val logger = LoggerFactory.getLogger(this.getClass)
  private val writeableMessages = new mutable.HashMap[String, StringBuilder]
  private var _messages: Long = 0
  private var _processedAt: Long = DateTime.now.getMillis
  private val self = this

  override def get(): Processor[K, V] = new Processor[K, V] {
    private var _context: ProcessorContext = _
    initState()

    override def init(context: ProcessorContext): Unit = {
      _context = context

      import com.plume.sdata.kstreams.utils.KStreams._

      _context.schedule(Duration.ofMillis(periodInMillis), PunctuationType.WALL_CLOCK_TIME, { ts: Long => {
        val now = DateTime.now.getMillis
        if (now - _processedAt > periodInMillis) {
          val messagesToWrite = getMessagesToWrite()
          writeToS3(messagesToWrite)
          _processedAt = now
        }
        _context.commit()
      }
      })
    }

    override def process(key: K, value: V): Unit = {
      val now = DateTime.now.getMillis
      if (_messages > size) {
        val messagesToWrite = getMessagesToWrite()
        writeToS3(messagesToWrite)
        _processedAt = now
      }
      self.synchronized {
        val processedMessage = processMessage(key, value)
        val partition = if (processedMessage.hasPartition) processedMessage.partition else ""
        val alertBuffer = writeableMessages.getOrElse(partition, new StringBuilder)
        writeableMessages.put(processedMessage.partition, alertBuffer)

        if (alertBuffer.isEmpty && processedMessage.hasHeader) {
          alertBuffer ++= processedMessage.header
        }
        alertBuffer ++= "\r\n" + processedMessage.message
        _messages += 1
      }
    }

    override def close(): Unit = {
    }
  }

  def getMessagesToWrite(input: mutable.HashMap[String, StringBuilder] = writeableMessages): Map[String, String] = {
    logger.debug("Getting " + _messages + " to write to s3")
    self.synchronized {
      val messagesToWrite = writeableMessages.foldLeft(Map.empty[String, String]) { case (result, (alertType, alertBuffer)) => {
        val toWrite = alertBuffer.mkString
        writeableMessages(alertType).clear
        result + (alertType -> toWrite)
      }
      }
      _messages = 0
      messagesToWrite
    }
  }

  def writeToS3(messagesToWrite: Map[String, String]): Unit = {
    val uri = new URI(targetDir)
    val bucket = uri.getHost
    var basePath = uri.getPath.substring(1)
    if (basePath.endsWith("/")) {
      basePath = basePath.substring(0, basePath.length - 1)
    }
    val dateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd")
    val date = dateFormatter.print(DateTime.now)
    val filename = UUID.randomUUID().toString

    messagesToWrite.foreach { case (partition, messages) => {
      if (messages.nonEmpty) {
        if (partition.isEmpty)
          s3Client.putObject(bucket, s"$basePath/dt=$date/$filename.csv", messages)
        else
          s3Client.putObject(bucket, s"$basePath/$partition/dt=$date/$filename.csv", messages)
      }
    }
    }
  }
}

case class ProcessedMessage(partition: String, header: String, message: String) {
  val hasPartition: Boolean = partition != null && partition.nonEmpty
  val hasHeader: Boolean = header != null && header.nonEmpty
}
