import scopt.OParser

case class CliOptions(
    moves: String = ""
) {
  override def toString = s"CliOptions[$moves]"
}

object Main extends App {
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
      println(moves)
    case _ => ;
  }
}
