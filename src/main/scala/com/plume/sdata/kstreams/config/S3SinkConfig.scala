package com.plume.sdata.kstreams.config

import com.typesafe.config.Config


case class S3SinkConfig(basePath: String, buffer: Int, bufferTimeMs: Long)

object S3SinkConfig {
  def apply(cfg: Config): S3SinkConfig = {
    S3SinkConfig(cfg.getString("basePath"),
      cfg.getInt("buffer"),
      cfg.getLong("bufferTimeMs"))
  }
}
