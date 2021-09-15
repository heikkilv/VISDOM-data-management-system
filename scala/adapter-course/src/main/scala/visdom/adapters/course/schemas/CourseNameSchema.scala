package visdom.adapters.course.schemas

import visdom.spark.FieldDataType
import visdom.utils.GeneralUtils.toOption
import visdom.utils.GeneralUtils.toStringOption
import visdom.utils.GeneralUtils.EnrichedWithToTuple
import visdom.utils.SnakeCaseConstants
import visdom.utils.WartRemoverConstants


final case class CourseNameSchema(
    fi: Option[CodeNameSchema],
    en: Option[CodeNameSchema],
    raw: String
)
extends BaseSchema

object CourseNameSchema extends BaseSchemaTrait[CourseNameSchema] {
    def fields: Seq[FieldDataType] = Seq(
        FieldDataType(SnakeCaseConstants.Fi, true),
        FieldDataType(SnakeCaseConstants.En, true),
        FieldDataType(SnakeCaseConstants.Raw, false)
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def transformValues(valueOptions: Seq[Option[Any]]): Option[CourseNameSchema] = {
        val (fiOption, enOption, rawOption) = valueOptions.toTuple3
        toStringOption(rawOption) match {
            case Some(raw: String) =>
                Some(
                    CourseNameSchema(
                        CodeNameSchema.fromAny(fiOption),
                        CodeNameSchema.fromAny(enOption),
                        raw
                    )
                )
            case None => None
        }
    }
}
