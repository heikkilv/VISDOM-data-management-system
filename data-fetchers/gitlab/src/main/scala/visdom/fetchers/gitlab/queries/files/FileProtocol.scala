package visdom.fetchers.gitlab.queries.files

import spray.json.RootJsonFormat
import visdom.fetchers.gitlab.queries.GitlabProtocol
import visdom.fetchers.gitlab.queries.GitlabResponseAccepted


trait FileProtocol extends GitlabProtocol {
    type FileResponseAccepted = GitlabResponseAccepted[FileQueryOptions]

    implicit val commitOptionsFormat: RootJsonFormat[FileQueryOptions] =
        jsonFormat5(FileQueryOptions)
    implicit val commitResponseAcceptedFormat: RootJsonFormat[FileResponseAccepted] =
        jsonFormat3(GitlabResponseAccepted[FileQueryOptions])
}
