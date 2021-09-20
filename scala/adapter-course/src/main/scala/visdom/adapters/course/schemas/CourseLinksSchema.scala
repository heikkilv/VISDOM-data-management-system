package visdom.adapters.course.schemas

import visdom.spark.FieldDataType
import visdom.utils.GeneralUtils.toIntOption
import visdom.utils.GeneralUtils.toSeqOption
import visdom.utils.SnakeCaseConstants
import visdom.utils.TupleUtils.toOption
import visdom.utils.TupleUtils.EnrichedWithToTuple
import visdom.utils.WartRemoverConstants


final case class CourseLinksSchema(
    modules: Option[Seq[Int]],
    points: Option[Seq[Int]]
)
extends BaseSchema

object CourseLinksSchema extends BaseSchemaTrait[CourseLinksSchema] {
    def fields: Seq[FieldDataType] = Seq(
        FieldDataType(SnakeCaseConstants.Modules, true),
        FieldDataType(SnakeCaseConstants.Points, true)
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def transformValues(valueOptions: Seq[Option[Any]]): Option[CourseLinksSchema] = {
        val (modulesOption, pointsOption) =
            valueOptions
                .map(valueOption => toSeqOption(valueOption, toIntOption))
                .toTuple2
        Some(CourseLinksSchema(modulesOption, pointsOption))
    }
}
