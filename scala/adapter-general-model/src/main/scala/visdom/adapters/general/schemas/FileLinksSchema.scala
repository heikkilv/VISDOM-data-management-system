package visdom.adapters.general.schemas

import visdom.adapters.schemas.BaseSchema
import visdom.adapters.schemas.BaseSchemaTrait2
import visdom.spark.FieldDataModel
import visdom.utils.GeneralUtils.toStringSeqOption
import visdom.utils.SnakeCaseConstants
import visdom.utils.WartRemoverConstants
import visdom.utils.TupleUtils


final case class FileLinksSchema(
    commits: Option[Seq[String]]
)
extends BaseSchema

object FileLinksSchema extends BaseSchemaTrait2[FileLinksSchema] {
    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def fields: Seq[FieldDataModel] = Seq(
        FieldDataModel(SnakeCaseConstants.Commits, true, toStringSeqOption)
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def createInstance(values: Seq[Any]): Option[FileLinksSchema] = {
        TupleUtils.toTuple[Option[Seq[String]]](values) match {
            case Some(inputValues) => Some(FileLinksSchema(inputValues._1))
            case None => None
        }
    }
}
