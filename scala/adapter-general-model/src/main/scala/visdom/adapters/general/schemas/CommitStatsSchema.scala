package visdom.adapters.general.schemas

import visdom.adapters.schemas.BaseSchema
import visdom.adapters.schemas.BaseSchemaTrait
import visdom.spark.FieldDataType
import visdom.utils.GeneralUtils.toIntOption
import visdom.utils.SnakeCaseConstants
import visdom.utils.TupleUtils.toOption
import visdom.utils.TupleUtils.EnrichedWithToTuple
import visdom.utils.WartRemoverConstants


final case class CommitStatsSchema(
    additions: Int,
    deletions: Int,
    total: Int
)
extends BaseSchema

object CommitStatsSchema extends BaseSchemaTrait[CommitStatsSchema] {
    def fields: Seq[FieldDataType] = Seq(
        FieldDataType(SnakeCaseConstants.Additions, false),
        FieldDataType(SnakeCaseConstants.Deletions, false),
        FieldDataType(SnakeCaseConstants.Total, false)
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def transformValues(valueOptions: Seq[Option[Any]]): Option[CommitStatsSchema] = {
        toOption(
            valueOptions.toTuple3,
            (
                (value: Any) => toIntOption(value),
                (value: Any) => toIntOption(value),
                (value: Any) => toIntOption(value)
            )
        ) match {
            case Some((
                additions: Int,
                deletions: Int,
                total: Int
            )) => Some(
                CommitStatsSchema(
                    additions,
                    deletions,
                    total
                )
            )
            case None => None
        }
    }
}
