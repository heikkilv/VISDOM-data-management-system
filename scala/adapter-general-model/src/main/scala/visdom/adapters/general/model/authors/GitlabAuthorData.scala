package visdom.adapters.general.model.authors

import visdom.adapters.general.model.base.Data


final case class GitlabAuthorData(
    id: Option[Int],
    email: String
)
extends Data
