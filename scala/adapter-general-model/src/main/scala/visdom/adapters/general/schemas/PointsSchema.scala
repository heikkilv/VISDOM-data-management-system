package visdom.adapters.general.schemas

import visdom.adapters.schemas.BaseSchema
import visdom.adapters.schemas.BaseSchemaTrait2
import visdom.spark.FieldDataModel
import visdom.utils.GeneralUtils.toBooleanOption
import visdom.utils.GeneralUtils.toIntOption
import visdom.utils.GeneralUtils.toSeqOption
import visdom.utils.GeneralUtils.toStringOption
import visdom.utils.SnakeCaseConstants
import visdom.utils.TupleUtils
import visdom.utils.WartRemoverConstants


final case class PointsSchema(
    id: Int,
    url: String,
    username: String,
    student_id: String,
    email: String,
    full_name: String,
    is_external: Boolean,
    submission_count: Int,
    points: Int,
    points_by_difficulty: PointsDifficultySchema,
    course_id: Int,
    host_name: String,
    modules: Seq[PointsModuleSchema]
)
extends BaseSchema

object PointsSchema extends BaseSchemaTrait2[PointsSchema] {
    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def fields: Seq[FieldDataModel] = Seq(
        FieldDataModel(SnakeCaseConstants.Id, false, toIntOption),
        FieldDataModel(SnakeCaseConstants.Url, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.Username, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.StudentId, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.Email, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.FullName, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.IsExternal, false, toBooleanOption),
        FieldDataModel(SnakeCaseConstants.SubmissionCount, false, toIntOption),
        FieldDataModel(SnakeCaseConstants.Points, false, toIntOption),
        FieldDataModel(SnakeCaseConstants.PointsByDifficulty, false, PointsDifficultySchema.fromAny),
        FieldDataModel(SnakeCaseConstants.CourseId, false, toIntOption),
        FieldDataModel(SnakeCaseConstants.HostName, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.Modules, false, (value: Any) => toSeqOption(value, PointsModuleSchema.fromAny))
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def createInstance(values: Seq[Any]): Option[PointsSchema] = {
        TupleUtils.toTuple[Int, String, String, String, String, String, Boolean, Int, Int,
                           PointsDifficultySchema, Int, String, Seq[PointsModuleSchema]](values) match {
            case Some(inputValues) => Some(
                (PointsSchema.apply _).tupled(inputValues)
            )
            case None => None
        }
    }
}
