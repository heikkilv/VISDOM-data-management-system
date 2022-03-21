package visdom.adapters.general.schemas

import visdom.adapters.schemas.BaseSchema
import visdom.adapters.schemas.BaseSchemaTrait2
import visdom.spark.FieldDataModel
import visdom.utils.GeneralUtils.toDoubleOption
import visdom.utils.GeneralUtils.toStringOption
import visdom.utils.SnakeCaseConstants
import visdom.utils.TupleUtils
import visdom.utils.WartRemoverConstants


final case class CourseMetadataOtherSchema(
    late_submission_coefficient: Option[Double],
    git_branch: Option[String]
)
extends BaseSchema

object CourseMetadataOtherSchema extends BaseSchemaTrait2[CourseMetadataOtherSchema] {
    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def fields: Seq[FieldDataModel] = Seq(
        FieldDataModel(SnakeCaseConstants.LateSubmissionCoefficient, true, toDoubleOption),
        FieldDataModel(SnakeCaseConstants.GitBranch, true, toStringOption)
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def createInstance(values: Seq[Any]): Option[CourseMetadataOtherSchema] = {
        TupleUtils.toTuple[Option[Double], Option[String]](values) match {
            case Some(inputValues) => Some(
                (CourseMetadataOtherSchema.apply _).tupled(inputValues)
            )
            case None => None
        }
    }
}
