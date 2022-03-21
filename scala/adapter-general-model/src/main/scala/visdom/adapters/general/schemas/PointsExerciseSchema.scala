package visdom.adapters.general.schemas

import visdom.adapters.schemas.BaseSchema
import visdom.adapters.schemas.BaseSchemaTrait2
import visdom.spark.FieldDataModel
import visdom.utils.GeneralUtils.toBooleanOption
import visdom.utils.GeneralUtils.toIntOption
import visdom.utils.GeneralUtils.toSeqOption
import visdom.utils.GeneralUtils.toStringOption
import visdom.utils.GeneralUtils.toStringSeqOption
import visdom.utils.SnakeCaseConstants
import visdom.utils.TupleUtils
import visdom.utils.WartRemoverConstants


final case class PointsExerciseSchema(
    id: Int,
    url: String,
    name: ModuleNameSchema,
    difficulty: String,
    max_points: Int,
    points_to_pass: Int,
    submission_count: Int,
    points: Int,
    best_submission: Option[String],
    submissions: Seq[String],
    submissions_with_points: Seq[SubmissionPointsSchema],
    passed: Boolean,
    official: Boolean
)
extends BaseSchema

object PointsExerciseSchema extends BaseSchemaTrait2[PointsExerciseSchema] {
    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def fields: Seq[FieldDataModel] = Seq(
        FieldDataModel(SnakeCaseConstants.Id, false, toIntOption),
        FieldDataModel(SnakeCaseConstants.Url, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.Name, false, ModuleNameSchema.fromAny),
        FieldDataModel(SnakeCaseConstants.Difficulty, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.MaxPoints, false, toIntOption),
        FieldDataModel(SnakeCaseConstants.PointsToPass, false, toIntOption),
        FieldDataModel(SnakeCaseConstants.SubmissionCount, false, toIntOption),
        FieldDataModel(SnakeCaseConstants.Points, false, toIntOption),
        FieldDataModel(SnakeCaseConstants.BestSubmission, true, toStringOption),
        FieldDataModel(SnakeCaseConstants.Submissions, false, toStringSeqOption),
        FieldDataModel(SnakeCaseConstants.SubmissionsWithPoints, false, (value: Any) => toSeqOption(value, SubmissionPointsSchema.fromAny)),
        FieldDataModel(SnakeCaseConstants.Passed, false, toBooleanOption),
        FieldDataModel(SnakeCaseConstants.Official, false, toBooleanOption)
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def createInstance(values: Seq[Any]): Option[PointsExerciseSchema] = {
        TupleUtils.toTuple[Int, String, ModuleNameSchema, String, Int, Int, Int, Int, Option[String],
                           Seq[String], Seq[SubmissionPointsSchema], Boolean, Boolean](values) match {
            case Some(inputValues) => Some(
                (PointsExerciseSchema.apply _).tupled(inputValues)
            )
            case None => None
        }
    }
}
