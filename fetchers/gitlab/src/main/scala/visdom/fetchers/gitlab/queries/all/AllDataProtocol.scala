package visdom.fetchers.gitlab.queries.all

import spray.json.RootJsonFormat
import visdom.fetchers.gitlab.queries.GitlabProtocol
import visdom.fetchers.gitlab.queries.GitlabResponseAccepted


trait AllDataProtocol extends GitlabProtocol {
    type AllDataResponseAccepted = GitlabResponseAccepted[AllDataQueryOptions]

    implicit val commitOptionsFormat: RootJsonFormat[AllDataQueryOptions] =
        jsonFormat4(AllDataQueryOptions)
    implicit val commitResponseAcceptedFormat: RootJsonFormat[AllDataResponseAccepted] =
        jsonFormat3(GitlabResponseAccepted[AllDataQueryOptions])
}
