package visdom.fetchers.gitlab.queries.commits

abstract class CommitResponse {
    def status: String
    def description: String
}

final case class CommitResponseAccepted(
    status: String,
    description: String,
    options: CommitQueryOptions
) extends CommitResponse

final case class CommitResponseInvalid(
    status: String,
    description: String
)
extends CommitResponse

final case class CommitResponseUnauthorized(
    status: String,
    description: String
)
extends CommitResponse

final case class CommitResponseNotFound(
    status: String,
    description: String
)
extends CommitResponse

final case class CommitResponseError(
    status: String,
    description: String
)
extends CommitResponse
