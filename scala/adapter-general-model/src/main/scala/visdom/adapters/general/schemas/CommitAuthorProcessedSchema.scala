// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.adapters.general.schemas

final case class CommitAuthorProcessedSchema(
    committerName: String,
    committerEmail: String,
    hostName: String,
    commitEventIds: Seq[String],
    gitlabAuthorIds: Seq[String]
)

object CommitAuthorProcessedSchema {
    def reduceSchemas(
        firstAuthorSchema: CommitAuthorProcessedSchema,
        secondAuthorSchema: CommitAuthorProcessedSchema
    ): CommitAuthorProcessedSchema = {
        // NOTE: assume that both schemas have the same name, email, and host
        CommitAuthorProcessedSchema(
            committerName = firstAuthorSchema.committerName,
            committerEmail = firstAuthorSchema.committerEmail,
            hostName = firstAuthorSchema.hostName,
            commitEventIds =
                firstAuthorSchema.commitEventIds ++ secondAuthorSchema.commitEventIds,
            gitlabAuthorIds =
                firstAuthorSchema.gitlabAuthorIds ++ secondAuthorSchema.gitlabAuthorIds
        )
    }
}
