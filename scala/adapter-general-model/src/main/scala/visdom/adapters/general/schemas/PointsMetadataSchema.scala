package visdom.adapters.general.schemas

import visdom.adapters.schemas.BaseSchema
import visdom.adapters.schemas.BaseSchemaTrait2
import visdom.spark.FieldDataModel
import visdom.utils.GeneralUtils.toStringOption
import visdom.utils.SnakeCaseConstants
import visdom.utils.TupleUtils
import visdom.utils.WartRemoverConstants


final case class PointsMetadataSchema(
    last_modified: String
)
extends BaseSchema

object PointsMetadataSchema extends BaseSchemaTrait2[PointsMetadataSchema] {
    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def fields: Seq[FieldDataModel] = Seq(
        FieldDataModel(SnakeCaseConstants.LastModified, false, toStringOption)
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def createInstance(values: Seq[Any]): Option[PointsMetadataSchema] = {
        TupleUtils.toTuple[String](values) match {
            case Some(inputValues) => Some(PointsMetadataSchema(inputValues._1))
            case None => None
        }
    }
}
