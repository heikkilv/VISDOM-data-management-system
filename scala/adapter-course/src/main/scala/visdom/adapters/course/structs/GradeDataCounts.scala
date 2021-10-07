package visdom.adapters.course.structs


final case class GradeDataCounts(
    students: Int,
    weeks: Map[String, ModuleDataCountsWithCumulative[Float]]
)
