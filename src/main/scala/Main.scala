package com.github.lsund.chessmovedb_gamesuggester

import org.apache.kafka.clients.consumer._
import scopt.OParser
import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._
import org.apache.kafka.clients.producer._
import java.util.Properties
import java.util
import org.apache.kafka.common.errors.WakeupException
import scala.collection.JavaConverters._

case class Turn(number: Int, white: String, black: String) {}

case class CliOptions(
    moves: String = ""
) {
  override def toString = s"CliOptions[$moves]"
}

object SuggestionConsumer extends Runnable {

  def make(): KafkaConsumer[String, String] = {
    val props = new Properties()
    props.put("bootstrap.servers", "localhost:9092")
    props.put(
      "key.deserializer",
      "org.apache.kafka.common.serialization.StringDeserializer"
    )
    props.put(
      "value.deserializer",
      "org.apache.kafka.common.serialization.StringDeserializer"
    )
    props.put("group.id", "consumer-group")
    return new KafkaConsumer[String, String](props)
  }

  val consumer = make()

  override def run {
    try {
      consumer.subscribe(util.Arrays.asList("suggestion"))
      var running = true
      while (running) {
        val record = consumer.poll(1000).asScala
        for (data <- record.iterator) {
          val message = data.value()
          println("Got message" + message)
          running = false
        }
      }
    } catch {
      case e: WakeupException =>
      // Ignore
    } finally {
      consumer.close()
    }
  }
  def shutdown() {
    consumer.wakeup()
  }
}

object Main extends App {

  def makeKafkaProducer(): KafkaProducer[String, String] = {
    val props = new Properties()
    props.put("bootstrap.servers", "localhost:9092")
    props.put(
      "key.serializer",
      "org.apache.kafka.common.serialization.StringSerializer"
    )
    props.put(
      "value.serializer",
      "org.apache.kafka.common.serialization.StringSerializer"
    )
    return new KafkaProducer[String, String](props)
  }

  def produceMessage(
      producer: KafkaProducer[String, String],
      topic: String,
      message: String
  ): Unit = {
    try {
      producer.send(
        new ProducerRecord[String, String](topic, message)
      )
    } catch {
      case e: Exception => {
        e.printStackTrace()
      }
    }
    producer.close()
  }

  val builder = OParser.builder[CliOptions]
  val optsparser = {
    import builder._
    OParser.sequence(
      programName("chessmovedb"),
      head("chessmovedb", "1.0.0"),
      builder
        .opt[String]('m', "moves")
        .action((x, c) => c.copy(moves = x))
        .text("Moves to base suggestion from")
    )
  }

  def moveListToTurns(moves: Array[String]): Array[Turn] = {
    moves
      .grouped(2)
      .toArray
      .zip(Stream from 1)
      .map({
        case (x: Array[String], y: Int) => Turn(y, x(0), x(1))
      })
      .toArray
  }

  OParser.parse(optsparser, args, CliOptions()) match {
    case Some(CliOptions(moves)) =>
      val producer = makeKafkaProducer()
      produceMessage(
        producer,
        "query",
        moveListToTurns(moves.split(" ")).asJson.noSpaces
      )
    case _ => ;
  }

  val suggestionConsumer = SuggestionConsumer
  val mainThread = Thread.currentThread

  new Thread(suggestionConsumer).start
  Runtime.getRuntime
    .addShutdownHook(new Thread() {
      override def run {
        suggestionConsumer.shutdown
        try {
          mainThread.join
        } catch {
          case e: InterruptedException =>
            println("Thread interrupted")
        }
      }
    });
}
