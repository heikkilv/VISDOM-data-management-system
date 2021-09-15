package visdom.adapters.course.schemas

import visdom.spark.FieldDataType
import visdom.utils.GeneralUtils
import visdom.utils.SnakeCaseConstants
import visdom.utils.WartRemoverConstants


final case class CommitIdListSchema(
    commits: Option[Seq[String]]
)
extends BaseSchema

object CommitIdListSchema extends BaseSchemaTrait[CommitIdListSchema] {
    def fields: Seq[FieldDataType] = Seq(
        FieldDataType(SnakeCaseConstants.Commits, true)
        )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def transformValues(valueOptions: Seq[Option[Any]]): Option[CommitIdListSchema] = {
        Some(
            CommitIdListSchema(
                valueOptions.headOption match {
                    case Some(valueOption) => GeneralUtils.toStringSeqOption(valueOption)
                    case None => None
                }
            )
        )
    }
}
