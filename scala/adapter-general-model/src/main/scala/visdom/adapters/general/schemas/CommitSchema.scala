package visdom.adapters.general.schemas

import visdom.adapters.schemas.BaseSchema
import visdom.adapters.schemas.BaseSchemaTrait
import visdom.spark.FieldDataType
import visdom.utils.GeneralUtils.toStringOption
import visdom.utils.SnakeCaseConstants
import visdom.utils.TupleUtils.toOption
import visdom.utils.TupleUtils.EnrichedWithToTuple
import visdom.utils.WartRemoverConstants


final case class CommitSchema(
    id: String,
    project_name: String,
    group_name: String,
    host_name: String,
    message: String,
    committed_date: String,
    committer_name: String,
    committer_email: String
)
extends BaseSchema

object CommitSchema extends BaseSchemaTrait[CommitSchema] {
    def fields: Seq[FieldDataType] = Seq(
        FieldDataType(SnakeCaseConstants.Id, false),
        FieldDataType(SnakeCaseConstants.ProjectName, false),
        FieldDataType(SnakeCaseConstants.GroupName, false),
        FieldDataType(SnakeCaseConstants.HostName, false),
        FieldDataType(SnakeCaseConstants.Message, false),
        FieldDataType(SnakeCaseConstants.CommittedDate, false),
        FieldDataType(SnakeCaseConstants.CommitterName, false),
        FieldDataType(SnakeCaseConstants.CommitterEmail, false)
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def transformValues(valueOptions: Seq[Option[Any]]): Option[CommitSchema] = {
        toOption(
            valueOptions.toTuple8,
            (
                (value: Any) => toStringOption(value),
                (value: Any) => toStringOption(value),
                (value: Any) => toStringOption(value),
                (value: Any) => toStringOption(value),
                (value: Any) => toStringOption(value),
                (value: Any) => toStringOption(value),
                (value: Any) => toStringOption(value),
                (value: Any) => toStringOption(value)
            )
        ) match {
            case Some((
                id: String,
                project_name: String,
                group_name: String,
                host_name: String,
                message: String,
                committed_date: String,
                committer_name: String,
                committer_email: String
            )) => Some(
                CommitSchema(
                    id,
                    project_name,
                    group_name,
                    host_name,
                    message,
                    committed_date,
                    committer_name,
                    committer_email
                )
            )
            case None => None
        }
    }
}
