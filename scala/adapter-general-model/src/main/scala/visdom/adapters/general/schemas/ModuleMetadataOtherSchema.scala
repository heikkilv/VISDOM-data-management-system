package visdom.adapters.general.schemas

import visdom.adapters.schemas.BaseSchema
import visdom.adapters.schemas.BaseSchemaTrait2
import visdom.spark.FieldDataModel
import visdom.utils.GeneralUtils.toStringOption
import visdom.utils.SnakeCaseConstants
import visdom.utils.TupleUtils
import visdom.utils.WartRemoverConstants


final case class ModuleMetadataOtherSchema(
    start_date: String,
    end_data: String,
    late_submission_date: Option[String]
)
extends BaseSchema

object ModuleMetadataOtherSchema extends BaseSchemaTrait2[ModuleMetadataOtherSchema] {
    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def fields: Seq[FieldDataModel] = Seq(
        FieldDataModel(SnakeCaseConstants.StartDate, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.EndDate, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.LateSubmissionDate, true, toStringOption)
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def createInstance(values: Seq[Any]): Option[ModuleMetadataOtherSchema] = {
        TupleUtils.toTuple[String, String, Option[String]](values) match {
            case Some(inputValues) => Some(
                (ModuleMetadataOtherSchema.apply _).tupled(inputValues)
            )
            case None => None
        }
    }
}
