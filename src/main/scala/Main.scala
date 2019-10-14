import scopt.OParser

import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._
import org.apache.kafka.clients.producer._
import java.util.Properties
import java.util

case class Message(key: String, value: String) {}

case class CliOptions(
    moves: String = ""
) {
  override def toString = s"CliOptions[$moves]"
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
      message: Message
  ): Unit = {
    val record =
      new ProducerRecord[String, String](topic, message.key, message.value)
    try {
      producer.send(record)
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
      programName("movesuggester"),
      head("movesuggester", "1.0.0"),
      builder
        .opt[String]('m', "moves")
        .action((x, c) => c.copy(moves = x))
        .text("Moves to base suggestion from")
    )
  }
  OParser.parse(optsparser, args, CliOptions()) match {
    case Some(CliOptions(moves)) =>
      val producer = makeKafkaProducer()
      val msg = Message("foo", moves.split(" ").asJson.noSpaces)
      produceMessage(producer, "query", msg)
    case _ => ;
  }
}
