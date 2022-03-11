package visdom.adapters.general.schemas

import visdom.adapters.schemas.BaseSchema
import visdom.adapters.schemas.BaseSchemaTrait2
import visdom.spark.FieldDataModel
import visdom.utils.GeneralUtils.toStringOption
import visdom.utils.SnakeCaseConstants
import visdom.utils.TupleUtils
import visdom.utils.WartRemoverConstants


final case class NameSchema(
    code: String,
    name: String
)
extends BaseSchema

object NameSchema extends BaseSchemaTrait2[NameSchema] {
    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def fields: Seq[FieldDataModel] = Seq(
        FieldDataModel(SnakeCaseConstants.Code, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.Name, false, toStringOption)
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def createInstance(values: Seq[Any]): Option[NameSchema] = {
        TupleUtils.toTuple[String, String](values) match {
            case Some(inputValues) => Some(
                (NameSchema.apply _).tupled(inputValues)
            )
            case None => None
        }
    }
}
