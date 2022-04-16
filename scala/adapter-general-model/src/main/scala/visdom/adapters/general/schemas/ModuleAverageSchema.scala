package visdom.adapters.general.schemas


final case class ModuleAverageSchema(
    module_number: Int,
    grade: Int,
    total: Int,
    avg_points: Double,
    avg_exercises: Double,
    avg_submissions: Double,
    avg_commits: Double
)
