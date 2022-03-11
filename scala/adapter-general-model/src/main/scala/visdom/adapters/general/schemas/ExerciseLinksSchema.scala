package visdom.adapters.general.schemas

import visdom.adapters.schemas.BaseSchema
import visdom.adapters.schemas.BaseSchemaTrait2
import visdom.spark.FieldDataModel
import visdom.utils.GeneralUtils.toIntOption
import visdom.utils.GeneralUtils.toSeqOption
import visdom.utils.SnakeCaseConstants
import visdom.utils.TupleUtils
import visdom.utils.WartRemoverConstants


final case class ExerciseLinksSchema(
    courses: Option[Int],
    modules: Option[Int],
    submission: Option[Seq[Int]]
)
extends BaseSchema

object ExerciseLinksSchema extends BaseSchemaTrait2[ExerciseLinksSchema] {
    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def fields: Seq[FieldDataModel] = Seq(
        FieldDataModel(SnakeCaseConstants.Courses, true, toIntOption),
        FieldDataModel(SnakeCaseConstants.Modules, true, toIntOption),
        FieldDataModel(SnakeCaseConstants.Submissions, true, (value: Any) => toSeqOption(value, toIntOption))
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def createInstance(values: Seq[Any]): Option[ExerciseLinksSchema] = {
        TupleUtils.toTuple[Option[Int], Option[Int], Option[Seq[Int]]](values) match {
            case Some(inputValues) => Some(
                (ExerciseLinksSchema.apply _).tupled(inputValues)
            )
            case None => None
        }
    }
}
