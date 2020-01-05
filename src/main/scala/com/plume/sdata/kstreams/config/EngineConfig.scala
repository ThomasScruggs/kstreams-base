package com.plume.sdata.kstreams.config

trait EngineConfig {
  val name: String
  val topicsSuffix: List[String]
  val applicationId: String
  val stateStore: String
  val stateStoreRetentionSize: Long
  val stateStoreRetentionMinutes: Int
}
