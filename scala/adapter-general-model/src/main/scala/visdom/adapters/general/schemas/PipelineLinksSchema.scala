package visdom.adapters.general.schemas

import visdom.adapters.schemas.BaseSchema
import visdom.adapters.schemas.BaseSchemaTrait2
import visdom.spark.FieldDataModel
import visdom.utils.GeneralUtils.toIntOption
import visdom.utils.GeneralUtils.toSeqOption
import visdom.utils.SnakeCaseConstants
import visdom.utils.WartRemoverConstants
import visdom.utils.TupleUtils


final case class PipelineLinksSchema(
    jobs: Option[Seq[Int]]
)
extends BaseSchema

object PipelineLinksSchema extends BaseSchemaTrait2[PipelineLinksSchema] {
    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def fields: Seq[FieldDataModel] = Seq(
        FieldDataModel(SnakeCaseConstants.Jobs, true, (value: Any) => toSeqOption(value, toIntOption))
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def createInstance(values: Seq[Any]): Option[PipelineLinksSchema] = {
        TupleUtils.toTuple[Option[Seq[Int]]](values) match {
            case Some(inputValues) => Some(PipelineLinksSchema(inputValues._1))
            case None => None
        }
    }
}
