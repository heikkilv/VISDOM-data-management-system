package visdom.adapters.general.schemas

import visdom.adapters.schemas.BaseSchema
import visdom.adapters.schemas.BaseSchemaTrait
import visdom.spark.FieldDataType
import visdom.utils.GeneralUtils.toBooleanOption
import visdom.utils.GeneralUtils.toStringOption
import visdom.utils.SnakeCaseConstants
import visdom.utils.TupleUtils.toOption
import visdom.utils.TupleUtils.EnrichedWithToTuple
import visdom.utils.WartRemoverConstants


final case class CommitFileLinkSchema(
    old_path: String,
    new_path: String,
    a_mode: String,
    b_mode: String,
    new_file: Boolean,
    renamed_file: Boolean,
    deleted_file: Boolean
)
extends BaseSchema

object CommitFileLinkSchema extends BaseSchemaTrait[CommitFileLinkSchema] {
    def fields: Seq[FieldDataType] = Seq(
        FieldDataType(SnakeCaseConstants.OldPath, false),
        FieldDataType(SnakeCaseConstants.NewPath, false),
        FieldDataType(SnakeCaseConstants.AMode, false),
        FieldDataType(SnakeCaseConstants.BMode, false),
        FieldDataType(SnakeCaseConstants.NewFile, false),
        FieldDataType(SnakeCaseConstants.RenamedFile, false),
        FieldDataType(SnakeCaseConstants.DeletedFile, false)
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def transformValues(valueOptions: Seq[Option[Any]]): Option[CommitFileLinkSchema] = {
        toOption(
            valueOptions.toTuple7,
            (
                (value: Any) => toStringOption(value),
                (value: Any) => toStringOption(value),
                (value: Any) => toStringOption(value),
                (value: Any) => toStringOption(value),
                (value: Any) => toBooleanOption(value),
                (value: Any) => toBooleanOption(value),
                (value: Any) => toBooleanOption(value)
            )
        ) match {
            case Some((
                old_path: String,
                new_path: String,
                a_mode: String,
                b_mode: String,
                new_file: Boolean,
                renamed_file: Boolean,
                deleted_file: Boolean
            )) => Some(
                CommitFileLinkSchema(
                    old_path,
                    new_path,
                    a_mode,
                    b_mode,
                    new_file,
                    renamed_file,
                    deleted_file
                )
            )
            case None => None
        }
    }
}
