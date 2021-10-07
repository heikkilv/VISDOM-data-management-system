package visdom.adapters.course.structs


final case class ModuleDataCounts(
    points: Int,
    exercises: Int,
    submissions: Int,
    commits: Int
) {
    def add(otherCounts: ModuleDataCounts): ModuleDataCounts = {
        ModuleDataCounts(
            points = points + otherCounts.points,
            exercises = exercises + otherCounts.exercises,
            submissions = submissions + otherCounts.submissions,
            commits = commits + otherCounts.commits
        )
    }
}

object ModuleDataCounts {
    def getEmpty(): ModuleDataCounts = {
        ModuleDataCounts(0, 0, 0, 0)
    }
}
