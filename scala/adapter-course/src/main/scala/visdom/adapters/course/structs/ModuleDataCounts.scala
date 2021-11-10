package visdom.adapters.course.structs


final case class ModuleDataCounts[T : Numeric](
    points: T,
    exercises: T,
    submissions: T,
    commits: T
) {
    def add(otherCounts: ModuleDataCounts[T]): ModuleDataCounts[T] = {
        ModuleDataCounts(
            points = implicitly[Numeric[T]].plus(points, otherCounts.points),
            exercises = implicitly[Numeric[T]].plus(exercises, otherCounts.exercises),
            submissions = implicitly[Numeric[T]].plus(submissions, otherCounts.submissions),
            commits = implicitly[Numeric[T]].plus(commits, otherCounts.commits)
        )
    }
}

object ModuleDataCounts {
    def getEmpty[T : Numeric](): ModuleDataCounts[T] = {
        ModuleDataCounts(
            implicitly[Numeric[T]].zero,
            implicitly[Numeric[T]].zero,
            implicitly[Numeric[T]].zero,
            implicitly[Numeric[T]].zero
        )
    }

    def getAverages(dataCounts: Seq[ModuleDataCounts[Int]]): ModuleDataCounts[Float] = {
        val size: Int = dataCounts.size
        size match {
            case 0 => getEmpty[Float]
            case _ => ModuleDataCounts[Float](
                points = dataCounts.map(counts => counts.points).sum.toFloat / size,
                exercises = dataCounts.map(counts => counts.exercises).sum.toFloat / size,
                submissions = dataCounts.map(counts => counts.submissions).sum.toFloat / size,
                commits = dataCounts.map(counts => counts.commits).sum.toFloat / size
            )
        }
    }
}
