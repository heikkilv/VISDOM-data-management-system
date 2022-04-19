package visdom.adapters.general.schemas


final case class ModuleNumbersSchema(
    point_count: PointsDifficultySchema,
    exercise_count: Int,
    submission_count: Int,
    commit_count: Int
)
