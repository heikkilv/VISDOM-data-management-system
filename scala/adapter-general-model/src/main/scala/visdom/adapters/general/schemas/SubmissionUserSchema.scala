package visdom.adapters.general.schemas

import visdom.adapters.schemas.BaseSchema
import visdom.adapters.schemas.BaseSchemaTrait2
import visdom.spark.FieldDataModel
import visdom.utils.GeneralUtils.toBooleanOption
import visdom.utils.GeneralUtils.toIntOption
import visdom.utils.GeneralUtils.toStringOption
import visdom.utils.SnakeCaseConstants
import visdom.utils.TupleUtils
import visdom.utils.WartRemoverConstants


final case class SubmissionUserSchema(
    id: Int,
    url: String,
    username: String,
    student_id: String,
    email: String,
    full_name: String,
    is_external: Boolean
)
extends BaseSchema

object SubmissionUserSchema extends BaseSchemaTrait2[SubmissionUserSchema] {
    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def fields: Seq[FieldDataModel] = Seq(
        FieldDataModel(SnakeCaseConstants.Id, false, toIntOption),
        FieldDataModel(SnakeCaseConstants.Url, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.Username, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.StudentId, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.Email, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.FullName, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.IsExternal, false, toBooleanOption)
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def createInstance(values: Seq[Any]): Option[SubmissionUserSchema] = {
        TupleUtils.toTuple[Int, String, String, String, String, String, Boolean](values) match {
            case Some(inputValues) => Some(
                (SubmissionUserSchema.apply _).tupled(inputValues)
            )
            case None => None
        }
    }
}
