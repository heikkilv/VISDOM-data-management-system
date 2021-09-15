package visdom.adapters.course.schemas

import visdom.spark.FieldDataType
import visdom.utils.GeneralUtils.EnrichedWithToTuple
import visdom.utils.GeneralUtils.toIntOption
import visdom.utils.GeneralUtils.toOption
import visdom.utils.GeneralUtils.toSeqOption
import visdom.utils.GeneralUtils.toStringOption
import visdom.utils.SnakeCaseConstants
import visdom.utils.WartRemoverConstants


final case class PointSchema(
    id: Int,
    full_name: String,
    course_id: Int,
    modules: Seq[ModuleSchema]
)
extends BaseSchema

object PointSchema extends BaseSchemaTrait[PointSchema] {
    def fields: Seq[FieldDataType] = Seq(
        FieldDataType(SnakeCaseConstants.Id, false),
        FieldDataType(SnakeCaseConstants.FullName, false),
        FieldDataType(SnakeCaseConstants.CourseId, false),
        FieldDataType(SnakeCaseConstants.Modules, false)
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def transformValues(valueOptions: Seq[Option[Any]]): Option[PointSchema] = {
        toOption(
            valueOptions.toTuple4,
            (
                (value: Any) => toIntOption(value),
                (value: Any) => toStringOption(value),
                (value: Any) => toIntOption(value),
                (value: Any) => toSeqOption(value, ModuleSchema.fromAny)
            )
        ) match {
            case Some((id: Int, full_name: String, course_id: Int, modules: Seq[ModuleSchema])) =>
                Some(
                    PointSchema(
                        id,
                        full_name,
                        course_id,
                        modules
                    )
                )
            case _ => None
        }
    }
}
