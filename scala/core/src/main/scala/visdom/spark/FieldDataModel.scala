package visdom.spark

import visdom.utils.WartRemoverConstants


@SuppressWarnings(Array(WartRemoverConstants.WartsAny))
final case class FieldDataModel(
    name: String,
    nullable: Boolean,
    transformation: Any => Option[Any]
)

@SuppressWarnings(Array(WartRemoverConstants.WartsAny))
object FieldDataModel
