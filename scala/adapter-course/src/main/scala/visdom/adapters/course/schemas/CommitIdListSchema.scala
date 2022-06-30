// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

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
