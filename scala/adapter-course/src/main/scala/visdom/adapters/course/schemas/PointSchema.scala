package visdom.adapters.course.schemas

import visdom.spark.FieldDataType
import visdom.utils.GeneralUtils.toBooleanOption
import visdom.utils.GeneralUtils.toIntOption
import visdom.utils.GeneralUtils.toSeqOption
import visdom.utils.GeneralUtils.toStringOption
import visdom.utils.SnakeCaseConstants
import visdom.utils.TupleUtils.EnrichedWithToTuple
import visdom.utils.TupleUtils.toOption
import visdom.utils.WartRemoverConstants


final case class PointSchema(
    id: Int,
    url: String,
    username: String,
    student_id: String,
    email: String,
    full_name: String,
    is_external: Boolean,
    submission_count: Int,
    points: Int,
    points_by_difficulty: ModulePointDifficultySchema,
    course_id: Int,
    modules: Seq[ModulePointSchema]
)
extends BaseSchema

object PointSchema extends BaseSchemaTrait[PointSchema] {
    def fields: Seq[FieldDataType] = Seq(
        FieldDataType(SnakeCaseConstants.Id, false),
        FieldDataType(SnakeCaseConstants.Url, false),
        FieldDataType(SnakeCaseConstants.Username, false),
        FieldDataType(SnakeCaseConstants.StudentId, false),
        FieldDataType(SnakeCaseConstants.Email, false),
        FieldDataType(SnakeCaseConstants.FullName, false),
        FieldDataType(SnakeCaseConstants.IsExternal, false),
        FieldDataType(SnakeCaseConstants.SubmissionCount, false),
        FieldDataType(SnakeCaseConstants.Points, false),
        FieldDataType(SnakeCaseConstants.PointsByDifficulty, false),
        FieldDataType(SnakeCaseConstants.CourseId, false),
        FieldDataType(SnakeCaseConstants.Modules, false)
    )

    // scalastyle:off method.length
    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def transformValues(valueOptions: Seq[Option[Any]]): Option[PointSchema] = {
        toOption(
            valueOptions.toTuple12,
            (
                (value: Any) => toIntOption(value),
                (value: Any) => toStringOption(value),
                (value: Any) => toStringOption(value),
                (value: Any) => toStringOption(value),
                (value: Any) => toStringOption(value),
                (value: Any) => toStringOption(value),
                (value: Any) => toBooleanOption(value),
                (value: Any) => toIntOption(value),
                (value: Any) => toIntOption(value),
                (value: Any) => ModulePointDifficultySchema.fromAny(value),
                (value: Any) => toIntOption(value),
                (value: Any) => toSeqOption(value, ModulePointSchema.fromAny)
            )
        ) match {
            case Some((
                id: Int,
                url: String,
                username: String,
                studentId: String,
                email: String,
                fullName: String,
                isExternal: Boolean,
                submissionCount: Int,
                points: Int,
                pointsByDifficulty: ModulePointDifficultySchema,
                courseId: Int,
                modules: Seq[ModulePointSchema]
            )) =>
                Some(
                    PointSchema(
                        id,
                        url,
                        username,
                        studentId,
                        email,
                        fullName,
                        isExternal,
                        submissionCount,
                        points,
                        pointsByDifficulty,
                        courseId,
                        modules
                    )
                )
            case _ => None
        }
    }
    // scalastyle:on method.length
}
