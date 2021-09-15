package visdom.adapters.course.schemas

import visdom.spark.FieldDataType
import visdom.utils.GeneralUtils.EnrichedWithToTuple
import visdom.utils.GeneralUtils.toBooleanOption
import visdom.utils.GeneralUtils.toOption
import visdom.utils.GeneralUtils.toStringOption
import visdom.utils.SnakeCaseConstants
import visdom.utils.WartRemoverConstants


final case class FolderLocationSchema(
    path: String,
    is_folder: Boolean
)
extends BaseSchema

object FolderLocationSchema extends BaseSchemaTrait[FolderLocationSchema] {
    def fields: Seq[FieldDataType] = Seq(
        FieldDataType(SnakeCaseConstants.Path, false),
        FieldDataType(SnakeCaseConstants.IsFolder, false)
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def transformValues(valueOptions: Seq[Option[Any]]): Option[FolderLocationSchema] = {
        toOption(
            valueOptions.toTuple2,
            (
                (value: Any) => toStringOption(value),
                (value: Any) => toBooleanOption(value)
            )
        ) match {
            case Some((path: String, is_folder: Boolean)) => Some(FolderLocationSchema(path, is_folder))
            case None => None
        }
    }
}
