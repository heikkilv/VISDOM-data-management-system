package visdom.adapters.course.schemas

import visdom.spark.FieldDataType
import visdom.utils.GeneralUtils.toIntOption
import visdom.utils.GeneralUtils.toSeqOption
import visdom.utils.SnakeCaseConstants
import visdom.utils.WartRemoverConstants


final case class ModuleLinksSchema(
    exercises: Option[Seq[Int]]
)
extends BaseSchema

object ModuleLinksSchema extends BaseSchemaTrait[ModuleLinksSchema] {
    def fields: Seq[FieldDataType] = Seq(
        FieldDataType(SnakeCaseConstants.Exercises, true)
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def transformValues(valueOptions: Seq[Option[Any]]): Option[ModuleLinksSchema] = {
        Some(
            ModuleLinksSchema(
                valueOptions.headOption match {
                    case Some(valueOption) => toSeqOption(valueOption, toIntOption)
                    case None => None
                }
            )
        )
    }
}
