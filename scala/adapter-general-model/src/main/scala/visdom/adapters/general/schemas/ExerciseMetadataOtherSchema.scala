package visdom.adapters.general.schemas

import visdom.adapters.schemas.BaseSchema
import visdom.adapters.schemas.BaseSchemaTrait2
import visdom.spark.FieldDataModel
import visdom.utils.GeneralUtils.toBooleanOption
import visdom.utils.GeneralUtils.toStringOption
import visdom.utils.SnakeCaseConstants
import visdom.utils.TupleUtils
import visdom.utils.WartRemoverConstants


final case class ExerciseMetadataOtherSchema(
    path: String,
    is_folder: Boolean
)
extends BaseSchema

object ExerciseMetadataOtherSchema extends BaseSchemaTrait2[ExerciseMetadataOtherSchema] {
    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def fields: Seq[FieldDataModel] = Seq(
        FieldDataModel(SnakeCaseConstants.Path, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.IsFolder, false, toBooleanOption)
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def createInstance(values: Seq[Any]): Option[ExerciseMetadataOtherSchema] = {
        TupleUtils.toTuple[String, Boolean](values) match {
            case Some(inputValues) => Some(
                (ExerciseMetadataOtherSchema.apply _).tupled(inputValues)
            )
            case None => None
        }
    }
}
