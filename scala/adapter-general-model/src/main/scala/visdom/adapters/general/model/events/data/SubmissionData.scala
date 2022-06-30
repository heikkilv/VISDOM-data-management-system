// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.adapters.general.model.events.data

import java.time.ZonedDateTime
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.BsonValue
import spray.json.JsObject
import spray.json.JsValue
import visdom.adapters.general.model.base.Data
import visdom.adapters.general.schemas.SubmissionDataSchema
import visdom.adapters.general.schemas.SubmissionSchema
import visdom.json.JsonUtils
import visdom.utils.SnakeCaseConstants
import visdom.utils.TimeUtils


final case class SubmissionData(
    submission_id: Int,
    exercise_id: Int,
    submitters: Seq[Int],
    grade: Int,
    status: String,
    late_penalty_applied: Option[Double],
    grading_time: Option[String],
    grader: Option[Int],
    // feedback: String,
    // assistant_feedback: Option[String],
    submission_data: SubmissionContent
)
extends Data {
    def toBsonValue(): BsonValue = {
        BsonDocument(
            Map(
                SnakeCaseConstants.SubmissionId -> JsonUtils.toBsonValue(submission_id),
                SnakeCaseConstants.ExerciseId -> JsonUtils.toBsonValue(exercise_id),
                SnakeCaseConstants.Submitters -> JsonUtils.toBsonValue(submitters),
                SnakeCaseConstants.Grade -> JsonUtils.toBsonValue(grade),
                SnakeCaseConstants.Status -> JsonUtils.toBsonValue(status),
                SnakeCaseConstants.LatePenaltyApplied -> JsonUtils.toBsonValue(late_penalty_applied),
                SnakeCaseConstants.GradingTime -> JsonUtils.toBsonValue(grading_time),
                SnakeCaseConstants.Grader -> JsonUtils.toBsonValue(grader),
                // SnakeCaseConstants.Feedback -> JsonUtils.toBsonValue(feedback),
                // SnakeCaseConstants.AssistantFeedback -> JsonUtils.toBsonValue(assistant_feedback),
                SnakeCaseConstants.SubmissionData -> submission_data.toBsonValue()
            )
        )
    }

    def toJsValue(): JsValue = {
        JsObject(
            Map(
                SnakeCaseConstants.SubmissionId -> JsonUtils.toJsonValue(submission_id),
                SnakeCaseConstants.ExerciseId -> JsonUtils.toJsonValue(exercise_id),
                SnakeCaseConstants.Submitters -> JsonUtils.toJsonValue(submitters),
                SnakeCaseConstants.Grade -> JsonUtils.toJsonValue(grade),
                SnakeCaseConstants.Status -> JsonUtils.toJsonValue(status),
                SnakeCaseConstants.LatePenaltyApplied -> JsonUtils.toJsonValue(late_penalty_applied),
                SnakeCaseConstants.GradingTime -> JsonUtils.toJsonValue(grading_time),
                SnakeCaseConstants.Grader -> JsonUtils.toJsonValue(grader),
                // SnakeCaseConstants.Feedback -> JsonUtils.toJsonValue(feedback),
                // SnakeCaseConstants.AssistantFeedback -> JsonUtils.toJsonValue(assistant_feedback),
                SnakeCaseConstants.SubmissionData -> submission_data.toJsValue()
            )
        )
    }
}

object SubmissionData {
    def fromSubmissionSchema(submissionSchema: SubmissionSchema): SubmissionData = {
        SubmissionData(
            submission_id = submissionSchema.id,
            exercise_id = submissionSchema.exercise.id,
            submitters = submissionSchema.submitters.map(submitter => submitter.id),
            grade = submissionSchema.grade,
            status = submissionSchema.status,
            late_penalty_applied = submissionSchema.late_penalty_applied,
            grading_time = submissionSchema.grading_time,
            grader = submissionSchema.grader.map(grader => grader.id),
            // feedback = submissionSchema.feedback,
            // assistant_feedback = submissionSchema.assistant_feedback,
            submission_data = submissionSchema.submission_data match {
                case Some(submissionData: SubmissionDataSchema) => SubmissionContent.fromSubmissionData(submissionData)
                case None => SubmissionContent.getEmpty()
            }
        )
    }
}
