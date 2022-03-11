package visdom.adapters.general.schemas

import visdom.adapters.schemas.BaseSchema
import visdom.adapters.schemas.BaseSchemaTrait2
import visdom.spark.FieldDataModel
import visdom.utils.SnakeCaseConstants
import visdom.utils.TupleUtils
import visdom.utils.WartRemoverConstants


final case class CourseMetadataSchema(
    other: Option[CourseMetadataOtherSchema]
)
extends BaseSchema

object CourseMetadataSchema extends BaseSchemaTrait2[CourseMetadataSchema] {
    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def fields: Seq[FieldDataModel] = Seq(
        FieldDataModel(SnakeCaseConstants.Other, true, CourseMetadataOtherSchema.fromAny)
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def createInstance(values: Seq[Any]): Option[CourseMetadataSchema] = {
        TupleUtils.toTuple[Option[CourseMetadataOtherSchema]](values) match {
            case Some(inputValues) => Some(CourseMetadataSchema(inputValues._1))
            case None => None
        }
    }
}
