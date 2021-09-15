package visdom.adapters.course.schemas

import visdom.spark.FieldDataType
import visdom.utils.GeneralUtils.toOption
import visdom.utils.GeneralUtils.toStringOption
import visdom.utils.GeneralUtils.EnrichedWithToTuple
import visdom.utils.SnakeCaseConstants
import visdom.utils.WartRemoverConstants


final case class CodeNameSchema(
    code: String,
    name: String
)
extends BaseSchema

object CodeNameSchema extends BaseSchemaTrait[CodeNameSchema] {
    def fields: Seq[FieldDataType] = Seq(
        FieldDataType(SnakeCaseConstants.Code, false),
        FieldDataType(SnakeCaseConstants.Name, false)
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def transformValues(valueOptions: Seq[Option[Any]]): Option[CodeNameSchema] = {
        toOption(
            valueOptions.toTuple2,
            (
                (value: Any) => toStringOption(value),
                (value: Any) => toStringOption(value)
            )
        ) match {
            case Some((code: String, name: String)) => Some(CodeNameSchema(code, name))
            case None => None
        }
    }
}
