package visdom.adapter.gitlab.queries


abstract class GitlabResponse {
    def status: String
    def description: String
}

final case class GitlabResponseAccepted[GitlabQueryOptions](
    status: String,
    description: String,
    options: GitlabQueryOptions
) extends GitlabResponse

final case class GitlabResponseProblem(
    status: String,
    description: String
)
extends GitlabResponse
