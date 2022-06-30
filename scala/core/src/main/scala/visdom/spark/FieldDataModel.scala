// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

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
