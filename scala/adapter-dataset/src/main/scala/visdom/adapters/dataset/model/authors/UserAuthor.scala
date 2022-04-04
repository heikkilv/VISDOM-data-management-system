package visdom.adapters.dataset.model.authors

import visdom.adapters.dataset.model.authors.data.UserData
import visdom.adapters.dataset.model.origins.ProjectOrigin
import visdom.adapters.general.model.authors.states.AuthorState
import visdom.adapters.general.model.base.Author
import visdom.adapters.general.model.base.ItemLink
import visdom.utils.GeneralUtils


class UserAuthor(
    username: String,
    datasetName: String,
    relatedConstructs: Seq[ItemLink],
    relatedEvents: Seq[ItemLink]
)
extends Author {
    override def getType: String = UserAuthor.UserAuthorType

    val name: String = username
    val description: String = Author.DefaultDescription
    val state: String = AuthorState.ActiveAuthorStateString  // NOTE: use active author state for everyone
    val origin: ItemLink = ProjectOrigin.getProjectOriginFromDataset(datasetName).link
    val data: UserData = UserData(
        commits = relatedEvents.size,
        issues = relatedConstructs.size
    )

    val id: String = UserAuthor.getId(origin.id, name)

    addRelatedConstructs(relatedConstructs)
    addRelatedEvents(relatedEvents)
}

object UserAuthor {
    final val UserAuthorType: String = "user"

    def getId(originId: String, username: String): String = {
        GeneralUtils.getUuid(originId, UserAuthorType, username)
    }
}
