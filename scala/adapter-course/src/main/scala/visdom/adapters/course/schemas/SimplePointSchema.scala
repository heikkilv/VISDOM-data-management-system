package visdom.adapters.course.schemas

import visdom.spark.FieldDataType
import visdom.utils.GeneralUtils.toIntOption
import visdom.utils.GeneralUtils.toStringOption
import visdom.utils.SnakeCaseConstants
import visdom.utils.TupleUtils.EnrichedWithToTuple
import visdom.utils.TupleUtils.toOption
import visdom.utils.WartRemoverConstants


final case class SimplePointSchema(
    username: String,
    course_id: Int
)
extends BaseSchema

object SimplePointSchema extends BaseSchemaTrait[SimplePointSchema] {
    def fields: Seq[FieldDataType] = Seq(
        FieldDataType(SnakeCaseConstants.Username, false),
        FieldDataType(SnakeCaseConstants.CourseId, false)
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def transformValues(valueOptions: Seq[Option[Any]]): Option[SimplePointSchema] = {
        toOption(
            valueOptions.toTuple2,
            (
                (value: Any) => toStringOption(value),
                (value: Any) => toIntOption(value)
            )
        ) match {
            case Some((username: String, courseId: Int)) => Some(SimplePointSchema(username, courseId))
            case _ => None
        }
    }
}
