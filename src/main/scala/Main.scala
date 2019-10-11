import scopt.OParser

case class CliOptions(
    moves: String
) {
  override def toString = s"CliOptions[$moves]"
}

object Main extends App {
  val builder = OParser.builder[CliOptions]
  val optsparser = {
    import builder._
    OParser.sequence(
      programName("scopt"),
      head("scopt", "4.x"),
      builder
        .opt[String]('p', "pgn")
        .action((x, c) => c.copy(moves = x))
        .text("Moves to base suggestion from")
    )
  }
  OParser.parse(optsparser, args, CliOptions("e4 e5")) match {
    case Some(CliOptions(moves)) =>
      println(moves)
    case _ => ;
  }
}
