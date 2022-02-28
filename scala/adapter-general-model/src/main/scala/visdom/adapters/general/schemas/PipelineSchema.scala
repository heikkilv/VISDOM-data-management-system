package visdom.adapters.general.schemas

import visdom.adapters.schemas.BaseSchema
import visdom.adapters.schemas.BaseSchemaTrait2
import visdom.spark.FieldDataModel
import visdom.utils.GeneralUtils.toBooleanOption
import visdom.utils.GeneralUtils.toDoubleOption
import visdom.utils.GeneralUtils.toIntOption
import visdom.utils.GeneralUtils.toStringOption
import visdom.utils.SnakeCaseConstants
import visdom.utils.TupleUtils
import visdom.utils.WartRemoverConstants


final case class PipelineSchema(
    id: Int,
    project_id: Int,
    sha: String,
    ref: String,
    status: String,
    source: String,
    created_at: String,
    updated_at: String,
    started_at: String,
    finished_at: String,
    tag: Boolean,
    user: PipelineUserSchema,
    duration: Double,  // TODO: check type
    queued_duration: Double,  // TODO: check type
    detailed_status: PipelineDetailedStatusSchema,
    web_url: String,
    project_name: String,
    group_name: String,
    host_name: String,
    _links: Option[PipelineLinksSchema]
)
extends BaseSchema
with GitlabProjectInformationSchemaTrait

object PipelineSchema extends BaseSchemaTrait2[PipelineSchema] {
    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def fields: Seq[FieldDataModel] = Seq(
        FieldDataModel(SnakeCaseConstants.Id, false, toIntOption),
        FieldDataModel(SnakeCaseConstants.ProjectId, false, toIntOption),
        FieldDataModel(SnakeCaseConstants.Sha, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.Ref, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.Status, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.Source, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.CreatedAt, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.UpdatedAt, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.StartedAt, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.FinishedAt, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.Tag, false, toBooleanOption),
        FieldDataModel(SnakeCaseConstants.User, false, PipelineUserSchema.fromAny),
        FieldDataModel(SnakeCaseConstants.Duration, false, toDoubleOption),
        FieldDataModel(SnakeCaseConstants.QueuedDuration, false, toDoubleOption),
        FieldDataModel(SnakeCaseConstants.DetailedStatus, false, PipelineDetailedStatusSchema.fromAny),
        FieldDataModel(SnakeCaseConstants.WebUrl, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.ProjectName, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.GroupName, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.HostName, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.Links, true, PipelineLinksSchema.fromAny)
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def createInstance(values: Seq[Any]): Option[PipelineSchema] = {
        TupleUtils.toTuple[Int, Int, String, String, String, String, String, String, String, String, Boolean,
                           PipelineUserSchema, Double, Double, PipelineDetailedStatusSchema, String, String,
                           String, String, Option[PipelineLinksSchema]](values) match {
            case Some(inputValues) => Some(
                (PipelineSchema.apply _).tupled(inputValues)
            )
            case None => None
        }
    }
}
