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

object ModuleAverageSchema {
    def getEmpty(moduleNumber: Int, grade: Int): ModuleAverageSchema = {
        ModuleAverageSchema(
            module_number = moduleNumber,
            grade = grade,
            total = 0,
            avg_points = 0.0,
            avg_exercises = 0.0,
            avg_submissions = 0.0,
            avg_commits = 0.0
        )
    }
}
