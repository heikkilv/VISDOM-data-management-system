package visdom.adapters.general.schemas

import visdom.adapters.schemas.BaseSchema
import visdom.adapters.schemas.BaseSchemaTrait2
import visdom.spark.FieldDataModel
import visdom.utils.GeneralUtils.toBooleanOption
import visdom.utils.GeneralUtils.toStringOption
import visdom.utils.SnakeCaseConstants
import visdom.utils.TupleUtils
import visdom.utils.WartRemoverConstants


final case class PipelineDetailedStatusSchema(
    icon: String,
    text: String,
    label: String,
    group: String,
    has_details: Boolean,
    detailed_path: String
)
extends BaseSchema

object PipelineDetailedStatusSchema extends BaseSchemaTrait2[PipelineDetailedStatusSchema] {
    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def fields: Seq[FieldDataModel] = Seq(
        FieldDataModel(SnakeCaseConstants.Icon, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.Text, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.Label, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.Group, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.HasDetails, false, toBooleanOption),
        FieldDataModel(SnakeCaseConstants.DetailedPath, false, toStringOption)
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def createInstance(values: Seq[Any]): Option[PipelineDetailedStatusSchema] = {
        TupleUtils.toTuple[String, String, String, String, Boolean, String](values) match {
            case Some(inputValues) => Some(
                (PipelineDetailedStatusSchema.apply _).tupled(inputValues)
            )
            case None => None
        }
    }
}
