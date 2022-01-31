package visdom.adapters.general.schemas

import visdom.adapters.schemas.BaseSchema
import visdom.adapters.schemas.BaseSchemaTrait
import visdom.spark.FieldDataType
import visdom.utils.GeneralUtils.toStringOption
import visdom.utils.SnakeCaseConstants
import visdom.utils.TupleUtils.toOption
import visdom.utils.TupleUtils.EnrichedWithToTuple
import visdom.utils.WartRemoverConstants


final case class CommitRefLinkSchema(
    `type`: String,
    name: String
)
extends BaseSchema

object CommitRefLinkSchema extends BaseSchemaTrait[CommitRefLinkSchema] {
    def fields: Seq[FieldDataType] = Seq(
        FieldDataType(SnakeCaseConstants.Type, false),
        FieldDataType(SnakeCaseConstants.Name, false)
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def transformValues(valueOptions: Seq[Option[Any]]): Option[CommitRefLinkSchema] = {
        toOption(
            valueOptions.toTuple2,
            (
                (value: Any) => toStringOption(value),
                (value: Any) => toStringOption(value)
            )
        ) match {
            case Some((
                refType: String,
                name: String
            )) => Some(
                CommitRefLinkSchema(
                    refType,
                    name
                )
            )
            case None => None
        }
    }
}
