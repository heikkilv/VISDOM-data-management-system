package visdom.adapters.general.schemas

import visdom.adapters.schemas.BaseSchema
import visdom.adapters.schemas.BaseSchemaTrait
import visdom.spark.FieldDataType
import visdom.utils.GeneralUtils.toSeqOption
import visdom.utils.SnakeCaseConstants
import visdom.utils.TupleUtils.toOption
import visdom.utils.TupleUtils.EnrichedWithToTuple
import visdom.utils.WartRemoverConstants


final case class CommitLinksSchema(
    files: Seq[CommitFileLinkSchema],
    refs: Seq[CommitRefLinkSchema]
)
extends BaseSchema

object CommitLinksSchema extends BaseSchemaTrait[CommitLinksSchema] {
    def fields: Seq[FieldDataType] = Seq(
        FieldDataType(SnakeCaseConstants.Files, false),
        FieldDataType(SnakeCaseConstants.Refs, false)
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def transformValues(valueOptions: Seq[Option[Any]]): Option[CommitLinksSchema] = {
        toOption(
            valueOptions.toTuple2,
            (
                (value: Any) => toSeqOption(value, CommitFileLinkSchema.fromAny),
                (value: Any) => toSeqOption(value, CommitRefLinkSchema.fromAny)
            )
        ) match {
            case Some((
                files: Seq[CommitFileLinkSchema],
                refs: Seq[CommitRefLinkSchema]
            )) => Some(
                CommitLinksSchema(
                    files,
                    refs
                )
            )
            case None => None
        }
    }
}
