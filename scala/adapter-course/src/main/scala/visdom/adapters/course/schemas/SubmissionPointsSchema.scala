package visdom.adapters.course.schemas

import visdom.spark.FieldDataType
import visdom.utils.GeneralUtils.EnrichedWithToTuple
import visdom.utils.GeneralUtils.toIntOption
import visdom.utils.GeneralUtils.toOption
import visdom.utils.GeneralUtils.toStringOption
import visdom.utils.SnakeCaseConstants
import visdom.utils.WartRemoverConstants


final case class SubmissionPointsSchema(
    id: Int,
    submission_time: String,
    grade: Int
)
extends BaseSchema

object SubmissionPointsSchema extends BaseSchemaTrait[SubmissionPointsSchema] {
    def fields: Seq[FieldDataType] = Seq(
        FieldDataType(SnakeCaseConstants.Id, false),
        FieldDataType(SnakeCaseConstants.SubmissionTime, false),
        FieldDataType(SnakeCaseConstants.Grade, false)
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def transformValues(valueOptions: Seq[Option[Any]]): Option[SubmissionPointsSchema] = {
        toOption(
            valueOptions.toTuple3,
            (
                (value: Any) => toIntOption(value),
                (value: Any) => toStringOption(value),
                (value: Any) => toIntOption(value)
            )
        ) match {
            case Some((id: Int, submission_time: String, grade: Int)) =>
                Some(SubmissionPointsSchema(id, submission_time, grade))
            case None => None
        }
    }
}
