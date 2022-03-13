package visdom.adapters.general.schemas


final case class ExerciseAdditionalSchema(
    difficulty: Option[String],
    points_to_pass: Option[Int],
    start_date: Option[String],
    end_date: Option[String]
)
