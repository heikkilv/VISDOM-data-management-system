package visdom.adapters.general.schemas

import visdom.adapters.schemas.BaseSchema
import visdom.adapters.schemas.BaseSchemaTrait2
import visdom.spark.FieldDataModel
import visdom.utils.GeneralUtils.toIntOption
import visdom.utils.SnakeCaseConstants
import visdom.utils.TupleUtils
import visdom.utils.WartRemoverConstants


final case class PointsDifficultySchema(
    category: Option[Int],
    categoryP: Option[Int],
    categoryG: Option[Int]
)
extends BaseSchema

object PointsDifficultySchema extends BaseSchemaTrait2[PointsDifficultySchema] {
    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def fields: Seq[FieldDataModel] = Seq(
        FieldDataModel(SnakeCaseConstants.Category, true, toIntOption),
        FieldDataModel(SnakeCaseConstants.CategoryP, true, toIntOption),
        FieldDataModel(SnakeCaseConstants.CategoryG, true, toIntOption)
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def createInstance(values: Seq[Any]): Option[PointsDifficultySchema] = {
        TupleUtils.toTuple[Option[Int], Option[Int], Option[Int]](values) match {
            case Some(inputValues) => Some(
                (PointsDifficultySchema.apply _).tupled(inputValues)
            )
            case None => None
        }
    }
}
