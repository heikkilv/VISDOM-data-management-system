package visdom.adapters.dataset.results

import visdom.adapters.dataset.model.authors.UserAuthor
import visdom.adapters.dataset.model.authors.data.UserData
import visdom.adapters.general.model.results.ArtifactResult


object ProjectArtifactResult {
    type UserAuthorResult = ArtifactResult[UserData]

    def fromUsername(username: String, datasetName: String): UserAuthorResult = {
        val userAuthor: UserAuthor = new UserAuthor(
            username = username,
            datasetName = datasetName
        )
        ArtifactResult.fromArtifact(userAuthor, userAuthor.data)
    }
}
