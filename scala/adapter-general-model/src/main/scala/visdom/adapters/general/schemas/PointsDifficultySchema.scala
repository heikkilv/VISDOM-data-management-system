package visdom.adapters.general.schemas

import visdom.adapters.schemas.BaseSchema
import visdom.adapters.schemas.BaseSchemaTrait2
import visdom.spark.FieldDataModel
import visdom.utils.CommonConstants
import visdom.utils.GeneralUtils
import visdom.utils.GeneralUtils.toIntOption
import visdom.utils.PascalCaseConstants
import visdom.utils.SnakeCaseConstants
import visdom.utils.TupleUtils
import visdom.utils.WartRemoverConstants


final case class PointsDifficultySchema(
    category: Option[Int],
    categoryP: Option[Int],
    categoryG: Option[Int]
)
extends BaseSchema {
    def add(otherPoints: PointsDifficultySchema): PointsDifficultySchema = {
        PointsDifficultySchema(
            category = GeneralUtils.sumValues(category, otherPoints.category),
            categoryP = GeneralUtils.sumValues(categoryP, otherPoints.categoryP),
            categoryG = GeneralUtils.sumValues(categoryG, otherPoints.categoryG)
        )
    }

    def total(): Int = {
        category.getOrElse(0) + categoryP.getOrElse(0) + categoryG.getOrElse(0)
    }
}

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

    def getEmpty(): PointsDifficultySchema = {
        PointsDifficultySchema(
            category = None,
            categoryP = None,
            categoryG = None
        )
    }

    def getSingle(difficulty: String, points: Int): PointsDifficultySchema = {
        PointsDifficultySchema(
            category = difficulty match {
                case CommonConstants.EmptyString => Some(points)
                case _ => None
            },
            categoryP = difficulty match {
                case PascalCaseConstants.P => Some(points)
                case _ => None
            },
             categoryG = difficulty match {
                case PascalCaseConstants.G => Some(points)
                case _ => None
            }
        )
    }
}
