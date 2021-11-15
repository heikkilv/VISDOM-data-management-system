package visdom.adapters.course.structs

import visdom.utils.GeneralUtils
import visdom.utils.TupleUtils.EnrichedWithToTuple


final case class ModuleCumulativeDataCounts[T](
    points: T,
    exercises: T,
    submissions: T,
    commits: T,
    cumulativePoints: T,
    cumulativeExercises: T,
    cumulativeSubmissions: T,
    cumulativeCommits: T
)

object ModuleCumulativeDataCounts {
    val attributeCount: Int = GeneralUtils.getAttributeCount[ModuleCumulativeDataCounts[Int]]()

    def getEmpty[T : Numeric](): ModuleCumulativeDataCounts[T] = {
        (ModuleCumulativeDataCounts[T] _).tupled(Seq.fill[T](attributeCount)(implicitly[Numeric[T]].zero).toTuple8)
    }

    def fromModuleDataCounts[T](
        newData: ModuleDataCounts[T],
        previous: Option[ModuleDataCounts[T]]
    ): ModuleCumulativeDataCounts[T] = {
        val cumulativeCounts: ModuleDataCounts[T] = previous match {
            case Some(previousCounts: ModuleDataCounts[_]) => previousCounts.add(newData)
            case None => newData
        }

        ModuleCumulativeDataCounts(
            points = newData.points,
            exercises = newData.exercises,
            submissions = newData.submissions,
            commits = newData.commits,
            cumulativePoints = cumulativeCounts.points,
            cumulativeExercises = cumulativeCounts.exercises,
            cumulativeSubmissions = cumulativeCounts.submissions,
            cumulativeCommits = cumulativeCounts.commits
        )
    }
}
