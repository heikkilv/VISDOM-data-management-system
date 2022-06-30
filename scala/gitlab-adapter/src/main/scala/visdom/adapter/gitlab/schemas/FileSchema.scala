// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.adapter.gitlab.schemas


final case class FileLinksSchema(
    commits: Array[String]
)

final case class FileSchema(
    project_name: String,
    path: String,
    _links: Option[FileLinksSchema]
)

final case class FileCommitSchema(
    project_name: String,
    path: String,
    commits: Array[String]
)

final case class FileDistinctCommitSchema(
    project_name: String,
    commit: String
)
