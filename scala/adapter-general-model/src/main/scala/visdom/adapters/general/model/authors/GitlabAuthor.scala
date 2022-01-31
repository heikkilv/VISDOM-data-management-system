package visdom.adapters.general.model.authors

import visdom.adapters.general.model.base.ActiveAuthorState
import visdom.adapters.general.model.base.Author
import visdom.adapters.general.model.base.AuthorState
import visdom.adapters.general.model.base.ItemLink
import visdom.adapters.general.model.origins.GitlabOrigin
import visdom.utils.GeneralUtils


class GitlabAuthor(
    authorName: String,
    authorEmail: String,
    hostName: String,
    authorDescription: Option[String],
    userId: Option[Int]
)
extends Author {
    val name: String = authorName
    val description: String = authorDescription match {
        case Some(authorText: String) => authorText
        case None => Author.DefaultDescription
    }
    val state: AuthorState = ActiveAuthorState  // NOTE: use active author state for everyone
    val origin: ItemLink = GitlabOrigin.getGitlabOriginFromHost(hostName).link
    val data: GitlabAuthorData = GitlabAuthorData(
        id = userId,
        email = authorEmail
    )

    val id: String = GeneralUtils.getUuid(
        origin.id,
        userId match {
            case Some(userIdInt: Int) => userIdInt.toString()
            case None => authorEmail
        }
    )
}
