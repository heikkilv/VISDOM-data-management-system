package visdom.adapters.general.schemas

import visdom.adapters.schemas.BaseSchema
import visdom.adapters.schemas.BaseSchemaTrait2
import visdom.spark.FieldDataModel
import visdom.utils.GeneralUtils.toIntOption
import visdom.utils.SnakeCaseConstants
import visdom.utils.TupleUtils
import visdom.utils.WartRemoverConstants


final case class SubmissionLinksSchema(
    courses: Option[Int],
    exercises: Option[Int]
)
extends BaseSchema

object SubmissionLinksSchema extends BaseSchemaTrait2[SubmissionLinksSchema] {
    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def fields: Seq[FieldDataModel] = Seq(
        FieldDataModel(SnakeCaseConstants.Courses, true, toIntOption),
        FieldDataModel(SnakeCaseConstants.Exercises, true, toIntOption)
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def createInstance(values: Seq[Any]): Option[SubmissionLinksSchema] = {
        TupleUtils.toTuple[Option[Int], Option[Int]](values) match {
            case Some(inputValues) => Some(
                (SubmissionLinksSchema.apply _).tupled(inputValues)
            )
            case None => None
        }
    }
}
