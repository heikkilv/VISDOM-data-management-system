// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.adapter.gitlab.schemas


final case class CommitTimestampSchema(
    project_name: String,
    id: String,
    committed_date: String
)

final case class CommitTimestampSchemaKey(
    project_name: String,
    id: String
)
