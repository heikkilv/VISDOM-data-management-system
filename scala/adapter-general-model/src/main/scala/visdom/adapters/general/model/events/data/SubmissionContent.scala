package visdom.adapters.general.model.events.data

import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.BsonValue
import spray.json.JsValue
import visdom.adapters.general.schemas.SubmissionDataSchema
import visdom.adapters.results.BaseResultValue
import visdom.json.JsonUtils
import visdom.json.JsonUtils.EnrichedBsonDocument
import visdom.utils.SnakeCaseConstants


final case class SubmissionContent(
    git: Option[SubmissionGitContent],
    field_0: Option[String],
    field_1: Option[String],
    field_2: Option[String],
    field_3: Option[String],
    field_4: Option[String],
    field_5: Option[String],
    field_6: Option[String],
    field_7: Option[String],
    field_8: Option[String],
    field_9: Option[String]
)
extends BaseResultValue {
    def toBsonValue(): BsonValue = {
        BsonDocument()
            .appendOption(SnakeCaseConstants.Git, git.map(gitContent => gitContent.toBsonValue()))
            .appendOption(SnakeCaseConstants.Field0, field_0.map(value => JsonUtils.toBsonValue(value)))
            .appendOption(SnakeCaseConstants.Field1, field_1.map(value => JsonUtils.toBsonValue(value)))
            .appendOption(SnakeCaseConstants.Field2, field_2.map(value => JsonUtils.toBsonValue(value)))
            .appendOption(SnakeCaseConstants.Field3, field_3.map(value => JsonUtils.toBsonValue(value)))
            .appendOption(SnakeCaseConstants.Field4, field_4.map(value => JsonUtils.toBsonValue(value)))
            .appendOption(SnakeCaseConstants.Field5, field_5.map(value => JsonUtils.toBsonValue(value)))
            .appendOption(SnakeCaseConstants.Field6, field_6.map(value => JsonUtils.toBsonValue(value)))
            .appendOption(SnakeCaseConstants.Field7, field_7.map(value => JsonUtils.toBsonValue(value)))
            .appendOption(SnakeCaseConstants.Field8, field_8.map(value => JsonUtils.toBsonValue(value)))
            .appendOption(SnakeCaseConstants.Field9, field_9.map(value => JsonUtils.toBsonValue(value)))
    }

    def toJsValue(): JsValue = {
        JsonUtils.toJsonValue(toBsonValue())
    }
}

object SubmissionContent {
    def fromSubmissionData(data: SubmissionDataSchema): SubmissionContent = {
        SubmissionContent(
            git = data.git.map(gitData => SubmissionGitContent.fromSubmissionGitData(gitData)),
            field_0 = data.field_0,
            field_1 = data.field_1,
            field_2 = data.field_2,
            field_3 = data.field_3,
            field_4 = data.field_4,
            field_5 = data.field_5,
            field_6 = data.field_6,
            field_7 = data.field_7,
            field_8 = data.field_8,
            field_9 = data.field_9
        )
    }
}
