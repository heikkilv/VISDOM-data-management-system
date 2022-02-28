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


final case class PipelineJobSchema(
    id: Int,
    status: String,
    stage: String,
    name: String,
    ref: String,
    tag: Boolean,
    created_at: String,
    started_at: Option[String],
    finished_at: Option[String],
    duration: Option[Double],
    queued_duration: Option[Double],
    user: PipelineUserSchema,
    commit: StringIdSchema,
    pipeline: IntIdSchema,
    web_url: String,
    host_name: String
)
extends BaseSchema

object PipelineJobSchema extends BaseSchemaTrait2[PipelineJobSchema] {
    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def fields: Seq[FieldDataModel] = Seq(
        FieldDataModel(SnakeCaseConstants.Id, false, toIntOption),
        FieldDataModel(SnakeCaseConstants.Status, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.Stage, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.Name, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.Ref, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.Tag, false, toBooleanOption),
        FieldDataModel(SnakeCaseConstants.CreatedAt, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.StartedAt, true, toStringOption),
        FieldDataModel(SnakeCaseConstants.FinishedAt, true, toStringOption),
        FieldDataModel(SnakeCaseConstants.Duration, true, toDoubleOption),
        FieldDataModel(SnakeCaseConstants.QueuedDuration, true, toDoubleOption),
        FieldDataModel(SnakeCaseConstants.User, false, PipelineUserSchema.fromAny),
        FieldDataModel(SnakeCaseConstants.Commit, false, StringIdSchema.fromAny),
        FieldDataModel(SnakeCaseConstants.Pipeline, false, IntIdSchema.fromAny),
        FieldDataModel(SnakeCaseConstants.WebUrl, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.HostName, false, toStringOption)
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def createInstance(values: Seq[Any]): Option[PipelineJobSchema] = {
        TupleUtils.toTuple[Int, String, String, String, String, Boolean, String, Option[String], Option[String],
                           Option[Double], Option[Double], PipelineUserSchema, StringIdSchema, IntIdSchema,
                           String, String](values) match {
            case Some(inputValues) => Some(
                (PipelineJobSchema.apply _).tupled(inputValues)
            )
            case None => None
        }
    }
}
