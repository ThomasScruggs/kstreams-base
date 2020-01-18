# kstreams-base
This library provides the base for all KStreams services, which helps you get jump started when you are trying to 
make one of your own

## General flow 
- Import the kstreams-base jar from artifactory and include it in your program's build.sbt (TODO publish the latest build here if possible)
- Implement a main app that inherits from [Driver](src/main/scala/com/plume/sdata/kstreams/driver/Driver.scala)
- Implement methods in the driver 
    - that parses a typesafe config for the application (parseAppConfig) including engine(s) specific config
    - that accepts this parsed application config and intializes and returns a list of engines. Each engine is meant to be
    passed its own specific [EngineConfig](src/main/scala/com/plume/sdata/kstreams/config/EngineConfig.scala) that the user can get from the ApplicationConfig.
- Implement the various [Engines](src/main/scala/com/plume/sdata/kstreams/engine/Engine.scala) which are your 
kstreams services(A service processes one or more input kafka streams, all the real work is done here). What you need to 
do is create your topology(s) that process the kafka streams. Make sure you add metrics as you process your streams
(number of messages processed etc)
- In main you would call the following driver methods in order to run your application
    - parseAppConfig
    - getEngines
    - run

## Other utilities and services
### Config parsers
- For source kafka - If you pass a section that contains the [Properties](https://github.com/plume-design-inc/kstreams-base/blob/master/src/main/scala/com/plume/sdata/kstreams/config/KafkaConfig.scala#L47)
a you can get back a object that gives you the [KafkaConfig](/src/main/scala/com/plume/sdata/kstreams/config/KafkaConfig.scala)
- For s3 writers - Similarly for services that will write output to s3 you have[S3SinkConfig](src/main/scala/com/plume/sdata/kstreams/config/S3SinkConfig.scala) 
 
### Processors
- [S3Writer](src/main/scala/com/plume/sdata/kstreams/processor/S3Writer.scala) - Use this processor to write out text 
formats to S3 (CSV or JSON)
- [StreamLogger](src/main/scala/com/plume/sdata/kstreams/processor/StreamLogger.scala) - Use this processor to log the 
stream messages for debug. By default these are enabled at the debug log level but can be modified for every stream 

### Serde
- If you need to serialize/desrialialize to and from json to read from / produce into Kafka use [JSONSerde](src/main/scala/com/plume/sdata/kstreams/serde/JSONSerde.scala) 
This is also useful when you are looking for a serde to serialize/desrialize data when you use KTable.

## Kafka Streams resources
[ARB Presentation](https://docs.google.com/presentation/d/1nsAJ6zqS0ZOE0JqJig66y5cghFWNCUGSDwLTV8wE6JY/edit?usp=sharing)

[Where I started](https://www.confluent.io/blog/introducing-kafka-streams-stream-processing-made-simple/)

[Confluent's home](https://kafka.apache.org/documentation/streams/)

