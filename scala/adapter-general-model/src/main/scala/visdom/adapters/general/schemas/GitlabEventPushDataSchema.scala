package visdom.adapters.general.schemas

import visdom.adapters.schemas.BaseSchema
import visdom.adapters.schemas.BaseSchemaTrait2
import visdom.spark.FieldDataModel
import visdom.utils.GeneralUtils.toIntOption
import visdom.utils.GeneralUtils.toStringOption
import visdom.utils.SnakeCaseConstants
import visdom.utils.WartRemoverConstants
import visdom.utils.TupleUtils


final case class GitlabEventPushDataSchema(
    action: String,
    ref: String,
    commit_count: Int,
    commit_from: Option[String],
    commit_to: Option[String],
    commit_title: Option[String]
)
extends BaseSchema

object GitlabEventPushDataSchema extends BaseSchemaTrait2[GitlabEventPushDataSchema] {
    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def fields: Seq[FieldDataModel] = Seq(
        FieldDataModel(SnakeCaseConstants.Action, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.Ref, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.CommitCount, false, toIntOption),
        FieldDataModel(SnakeCaseConstants.CommitFrom, true, toStringOption),
        FieldDataModel(SnakeCaseConstants.CommitTo, true, toStringOption),
        FieldDataModel(SnakeCaseConstants.CommitTitle, true, toStringOption)
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def createInstance(values: Seq[Any]): Option[GitlabEventPushDataSchema] = {
        TupleUtils.toTuple[String, String, Int, Option[String], Option[String], Option[String]](values) match {
            case Some(inputValues) => Some(
                (GitlabEventPushDataSchema.apply _).tupled(inputValues)
            )
            case None => None
        }
    }
}
