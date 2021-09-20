package visdom.adapters.course.schemas

import visdom.spark.FieldDataType
import visdom.utils.SnakeCaseConstants
import visdom.utils.WartRemoverConstants


final case class MetadataSchema(
    other: Option[MetadataOtherSchema]
)
extends BaseSchema

object MetadataSchema extends BaseSchemaTrait[MetadataSchema] {
    def fields: Seq[FieldDataType] = Seq(
        FieldDataType(SnakeCaseConstants.Other, true)
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def transformValues(valueOptions: Seq[Option[Any]]): Option[MetadataSchema] = {
        Some(
            MetadataSchema(
                valueOptions.headOption match {
                    case Some(otherOption) => otherOption match {
                        case Some(other) => MetadataOtherSchema.fromAny(other)
                        case None => None
                    }
                    case None => None
                }
            )
        )
    }
}
