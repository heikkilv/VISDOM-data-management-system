package visdom.adapters.general.schemas


final case class ModuleNumbersSchema(
    point_count: PointsDifficultySchema,
    exercise_count: Int,
    submission_count: Int,
    commit_count: Int
) {
    def add(other: ModuleNumbersSchema): ModuleNumbersSchema = {
        ModuleNumbersSchema(
            point_count = point_count.add(other.point_count),
            exercise_count = exercise_count + other.exercise_count,
            submission_count = submission_count + other.submission_count,
            commit_count = commit_count + other.commit_count
        )
    }
}

object ModuleNumbersSchema {
    def getEmpty(): ModuleNumbersSchema = {
        ModuleNumbersSchema(
            point_count = PointsDifficultySchema.getEmpty(),
            exercise_count = 0,
            submission_count = 0,
            commit_count = 0
        )
    }
}
