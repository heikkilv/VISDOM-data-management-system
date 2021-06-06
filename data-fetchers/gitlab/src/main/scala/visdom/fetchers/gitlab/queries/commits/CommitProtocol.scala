package visdom.fetchers.gitlab.queries.commits

import spray.json.RootJsonFormat
import visdom.fetchers.gitlab.queries.GitlabProtocol
import visdom.fetchers.gitlab.queries.GitlabResponseAccepted


trait CommitProtocol extends GitlabProtocol {
    type CommitResponseAccepted = GitlabResponseAccepted[CommitQueryOptions]

    implicit val commitOptionsFormat: RootJsonFormat[CommitQueryOptions] =
        jsonFormat8(CommitQueryOptions)
    implicit val commitResponseAcceptedFormat: RootJsonFormat[CommitResponseAccepted] =
        jsonFormat3(GitlabResponseAccepted[CommitQueryOptions])
}
