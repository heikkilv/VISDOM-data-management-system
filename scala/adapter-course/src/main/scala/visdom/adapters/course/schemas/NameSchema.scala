package visdom.adapters.course.schemas

import visdom.spark.FieldDataType
import visdom.utils.GeneralUtils.toStringOption
import visdom.utils.SnakeCaseConstants
import visdom.utils.TupleUtils.EnrichedWithToTuple
import visdom.utils.WartRemoverConstants


final case class NameSchema(
    number: Option[String],
    fi: Option[String],
    en: Option[String],
    raw: String
)
extends BaseSchema

object NameSchema extends BaseSchemaTrait[NameSchema] {
    def fields: Seq[FieldDataType] = Seq(
        FieldDataType(SnakeCaseConstants.Number, true),
        FieldDataType(SnakeCaseConstants.Fi, true),
        FieldDataType(SnakeCaseConstants.En, true),
        FieldDataType(SnakeCaseConstants.Raw, false)
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def transformValues(valueOptions: Seq[Option[Any]]): Option[NameSchema] = {
        val (numberOption, fiOption, enOption, rawOption) = valueOptions.toTuple4
        toStringOption(rawOption) match {
            case Some(raw: String) =>
                Some(
                    NameSchema(
                        toStringOption(numberOption),
                        toStringOption(fiOption),
                        toStringOption(enOption),
                        raw
                    )
                )
            case None => None
        }
    }
}
