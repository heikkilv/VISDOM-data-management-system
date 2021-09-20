package visdom.adapters.course.schemas

import visdom.spark.FieldDataType
import visdom.utils.GeneralUtils.toBooleanOption
import visdom.utils.GeneralUtils.toIntOption
import visdom.utils.GeneralUtils.toSeqOption
import visdom.utils.GeneralUtils.toStringOption
import visdom.utils.GeneralUtils.toStringSeqOption
import visdom.utils.SnakeCaseConstants
import visdom.utils.TupleUtils.EnrichedWithToTuple
import visdom.utils.TupleUtils.toOption
import visdom.utils.WartRemoverConstants


final case class ExercisePointsSchema(
    id: Int,
    name: NameSchema,
    url: String,
    best_submission: Option[String],
    submissions: Seq[String],
    submissions_with_points: Seq[SubmissionPointsSchema],
    difficulty: String,
    max_points: Int,
    points_to_pass: Int,
    submission_count: Int,
    points: Int,
    passed: Boolean,
    official: Boolean
)
extends BaseSchema

object ExercisePointsSchema extends BaseSchemaTrait[ExercisePointsSchema] {
    def fields: Seq[FieldDataType] = Seq(
        FieldDataType(SnakeCaseConstants.Id, false),
        FieldDataType(SnakeCaseConstants.Name, false),
        FieldDataType(SnakeCaseConstants.Url, false),
        FieldDataType(SnakeCaseConstants.BestSubmission, true),
        FieldDataType(SnakeCaseConstants.Submissions, false),
        FieldDataType(SnakeCaseConstants.SubmissionsWithPoints, false),
        FieldDataType(SnakeCaseConstants.Difficulty, false),
        FieldDataType(SnakeCaseConstants.MaxPoints, false),
        FieldDataType(SnakeCaseConstants.PointsToPass, false),
        FieldDataType(SnakeCaseConstants.SubmissionCount, false),
        FieldDataType(SnakeCaseConstants.Points, false),
        FieldDataType(SnakeCaseConstants.Passed, false),
        FieldDataType(SnakeCaseConstants.Official, false)
    )

    // scalastyle:off method.length
    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def transformValues(valueOptions: Seq[Option[Any]]): Option[ExercisePointsSchema] = {
        val (
            idOption,
            nameOption,
            urlOption,
            bestSubmissionOption,
            submissionOption,
            submissionsWithPointsOption,
            difficultyOption,
            maxPointsOption,
            pointsToPassOption,
            submissionCountOption,
            pointsOption,
            passedOption,
            officialOption
        ) = valueOptions.toTuple13
        toOption(
            (
                idOption,
                nameOption,
                urlOption,
                submissionOption,
                submissionsWithPointsOption,
                difficultyOption,
                maxPointsOption,
                pointsToPassOption,
                submissionCountOption,
                pointsOption,
                passedOption,
                officialOption
            ),
            (
                (value: Any) => toIntOption(value),
                (value: Any) => NameSchema.fromAny(value),
                (value: Any) => toStringOption(value),
                (value: Any) => toStringSeqOption(value),
                (value: Any) => toSeqOption(value, SubmissionPointsSchema.fromAny),
                (value: Any) => toStringOption(value),
                (value: Any) => toIntOption(value),
                (value: Any) => toIntOption(value),
                (value: Any) => toIntOption(value),
                (value: Any) => toIntOption(value),
                (value: Any) => toBooleanOption(value),
                (value: Any) => toBooleanOption(value)
                )
        ) match {
            case Some(
                (
                    id: Int,
                    name: NameSchema,
                    url: String,
                    submissions: Seq[String],
                    submissionsWithPoints: Seq[SubmissionPointsSchema],
                    difficulty: String,
                    maxPoints: Int,
                    pointsToPass: Int,
                    submissionCount: Int,
                    points: Int,
                    passed: Boolean,
                    official: Boolean
                )
            ) =>
                Some(
                    ExercisePointsSchema(
                        id,
                        name,
                        url,
                        toStringOption(bestSubmissionOption),
                        submissions,
                        submissionsWithPoints,
                        difficulty,
                        maxPoints,
                        pointsToPass,
                        submissionCount,
                        points,
                        passed,
                        official
                    )
                )
            case _ => None
        }
    }
    // scalastyle:on method.length
}
