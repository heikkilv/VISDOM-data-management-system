package visdom.adapters.course.structs


final case class ModuleDataCountsWithCumulative[NumberType](
    points: NumberType,
    exercises: NumberType,
    submissions: NumberType,
    commits: NumberType,
    cumulativePoints: NumberType,
    cumulativeExercises: NumberType,
    cumulativeSubmissions: NumberType,
    cumulativeCommits: NumberType
)

object ModuleDataCountsWithCumulative {
    def getEmptyFloats(
        previous: Option[ModuleDataCountsWithCumulative[Float]]
    ): ModuleDataCountsWithCumulative[Float] = {
        previous match {
            case Some(previousCounts: ModuleDataCountsWithCumulative[Float]) => ModuleDataCountsWithCumulative(
                0.0f,
                0.0f,
                0.0f,
                0.0f,
                previousCounts.cumulativePoints,
                previousCounts.cumulativeExercises,
                previousCounts.cumulativeSubmissions,
                previousCounts.cumulativeCommits
            )
            case None => ModuleDataCountsWithCumulative(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f)
        }
    }

    def getAverages(dataCounts: Seq[ModuleDataCountsWithCumulative[Int]]): ModuleDataCountsWithCumulative[Float] = {
        val size: Int = dataCounts.size
        size match {
            case 0 => getEmptyFloats(None)
            case _ => ModuleDataCountsWithCumulative(
                points = dataCounts.map(counts => counts.points).sum.toFloat / size,
                exercises = dataCounts.map(counts => counts.exercises).sum.toFloat / size,
                submissions = dataCounts.map(counts => counts.submissions).sum.toFloat / size,
                commits = dataCounts.map(counts => counts.commits).sum.toFloat / size,
                cumulativePoints = dataCounts.map(counts => counts.cumulativePoints).sum.toFloat / size,
                cumulativeExercises = dataCounts.map(counts => counts.cumulativeExercises).sum.toFloat / size,
                cumulativeSubmissions = dataCounts.map(counts => counts.cumulativeSubmissions).sum.toFloat / size,
                cumulativeCommits = dataCounts.map(counts => counts.cumulativeCommits).sum.toFloat / size
            )
        }
    }
}
