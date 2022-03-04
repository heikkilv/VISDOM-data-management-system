package visdom.adapters.general.schemas

final case class CommitAuthorProcessedSchema(
    committerName: String,
    committerEmail: String,
    hostName: String,
    commitEventIds: Seq[String]
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
                firstAuthorSchema.commitEventIds ++ secondAuthorSchema.commitEventIds
        )
    }
}
