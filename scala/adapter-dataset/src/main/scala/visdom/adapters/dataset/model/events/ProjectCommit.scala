package visdom.adapters.dataset.model.events

import java.time.ZonedDateTime
import visdom.adapters.dataset.model.events.data.ProjectCommitData
import visdom.adapters.dataset.schemas.CommitSchema
import visdom.adapters.dataset.model.authors.UserAuthor
import visdom.adapters.general.model.base.Event
import visdom.adapters.general.model.base.ItemLink
import visdom.adapters.dataset.model.origins.ProjectOrigin
import visdom.utils.GeneralUtils
import visdom.utils.TimeUtils


class ProjectCommitEvent(
    commitSchema: CommitSchema,
    numberOfFiles: Int,
    additions: Int,
    deletions: Int,
    datasetName: String,
    relatedConstructs: Seq[ItemLink]
)
extends Event {
    def getType: String = ProjectCommitEvent.ProjectCommitEventType
    val duration: Double = 0.0

    val origin: ItemLink = ItemLink(
        ProjectOrigin.getId(datasetName, commitSchema.project_id),
        ProjectOrigin.ProjectOriginType
    )

    val author: ItemLink = ItemLink(
        UserAuthor.getId(ProjectOrigin.getId(datasetName), commitSchema.committer),
        UserAuthor.UserAuthorType
    )

    val data: ProjectCommitData = ProjectCommitData.fromCommitSchema(commitSchema, numberOfFiles, additions, deletions)

    val message: String = commitSchema.commit_message
    val time: ZonedDateTime = TimeUtils.toZonedDateTimeWithDefault(commitSchema.committer_date)

    val id: String = ProjectCommitEvent.getId(origin.id, data.commit_id)

    // author as related construct
    addRelatedConstruct(author)

    // if commit author is not the same as committer, add author as related construct
    if (data.committer_name != data.author_name) {
        addRelatedConstruct(
            ItemLink(
                UserAuthor.getId(ProjectOrigin.getId(datasetName), data.author_name),
                UserAuthor.UserAuthorType
            )
        )
    }

    // add parent commits as related events
    addRelatedEvents(
        data.parent_ids.map(
            commitId => ItemLink(
                id = ProjectCommitEvent.getId(origin.id, commitId),
                `type` = getType
            )
        )
    )

    addRelatedConstructs(relatedConstructs)
}

object ProjectCommitEvent {
    final val ProjectCommitEventType: String = "git_commit"

    def getId(originId: String, commitId: String): String = {
        GeneralUtils.getUuid(originId, ProjectCommitEventType, commitId)
    }

    def getId(datasetName: String, projectId: String, commitId: String): String = {
        getId(
            originId = ProjectOrigin.getId(datasetName, projectId),
            commitId = commitId
        )
    }
}
