package visdom.adapters.general.schemas

import visdom.adapters.schemas.BaseSchema
import visdom.adapters.schemas.BaseSchemaTrait2
import visdom.spark.FieldDataModel
import visdom.utils.GeneralUtils.toDoubleOption
import visdom.utils.GeneralUtils.toIntOption
import visdom.utils.GeneralUtils.toSeqOption
import visdom.utils.GeneralUtils.toStringOption
import visdom.utils.SnakeCaseConstants
import visdom.utils.TupleUtils
import visdom.utils.WartRemoverConstants


final case class SubmissionSchema(
    id: Int,
    url: String,
    html_url: String,
    exercise: IntIdSchema,
    submission_time: String,
    submitters: Seq[SubmissionUserSchema],
    submission_data: Option[SubmissionDataSchema],
    status: String,
    grade: Int,
    late_penalty_applied: Option[Double],
    grading_time: Option[String],
    grader: Option[SubmissionUserSchema],
    feedback: String,
    assistant_feedback: Option[String],
    host_name: String,
    _links: Option[SubmissionLinksSchema]
)
extends BaseSchema

object SubmissionSchema extends BaseSchemaTrait2[SubmissionSchema] {
    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def fields: Seq[FieldDataModel] = Seq(
        FieldDataModel(SnakeCaseConstants.Id, false, toIntOption),
        FieldDataModel(SnakeCaseConstants.Url, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.HtmlUrl, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.Exercise, false, IntIdSchema.fromAny),
        FieldDataModel(SnakeCaseConstants.SubmissionTime, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.Submitters, false, (value: Any) => toSeqOption(value, SubmissionUserSchema.fromAny)),
        FieldDataModel(SnakeCaseConstants.SubmissionData, true, SubmissionDataSchema.fromAny),
        FieldDataModel(SnakeCaseConstants.Status, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.Grade, false, toIntOption),
        FieldDataModel(SnakeCaseConstants.LatePenaltyApplied, true, toDoubleOption),
        FieldDataModel(SnakeCaseConstants.GradingTime, true, toStringOption),
        FieldDataModel(SnakeCaseConstants.Grader, true, SubmissionUserSchema.fromAny),
        FieldDataModel(SnakeCaseConstants.Feedback, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.AssistantFeedback, true, toStringOption),
        FieldDataModel(SnakeCaseConstants.HostName, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.Links, true, SubmissionLinksSchema.fromAny)
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def createInstance(values: Seq[Any]): Option[SubmissionSchema] = {
        TupleUtils.toTuple[Int, String, String, IntIdSchema, String, Seq[SubmissionUserSchema],
                           Option[SubmissionDataSchema], String, Int, Option[Double], Option[String],
                           Option[SubmissionUserSchema], String, Option[String], String,
                           Option[SubmissionLinksSchema]](values) match {
            case Some(inputValues) => Some(
                (SubmissionSchema.apply _).tupled(inputValues)
            )
            case None => None
        }
    }
}
