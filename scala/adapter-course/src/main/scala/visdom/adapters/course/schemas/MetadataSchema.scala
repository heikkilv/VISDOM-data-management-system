package visdom.adapters.course.schemas

import visdom.spark.FieldDataType
import visdom.utils.SnakeCaseConstants
import visdom.utils.WartRemoverConstants


final case class MetadataSchema(
    other: Option[FolderLocationSchema]
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
                        case Some(other) => FolderLocationSchema.fromAny(other) match {
                            case Some(location: FolderLocationSchema) => Some(location)
                            case _ => None
                        }
                        case None => None
                    }
                    case None => None
                }
            )
        )
    }
}
