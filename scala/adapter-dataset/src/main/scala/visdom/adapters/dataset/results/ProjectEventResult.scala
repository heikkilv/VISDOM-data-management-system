package visdom.adapters.dataset.results

import visdom.adapters.dataset.model.events.ProjectCommitEvent
import visdom.adapters.dataset.model.events.data.ProjectCommitData
import visdom.adapters.dataset.schemas.CommitChangeSchema
import visdom.adapters.dataset.schemas.CommitSchema
import visdom.adapters.general.model.results.EventResult


object ProjectEventResult {
    type ProjectCommitEventResult = EventResult[ProjectCommitData]

    def fromCommitSchema(
        commitSchema: CommitSchema,
        commitChangeSchemas: Seq[CommitChangeSchema],
        datasetName: String
    ): ProjectCommitEventResult = {
        val commitEvent: ProjectCommitEvent = new ProjectCommitEvent(commitSchema, commitChangeSchemas, datasetName)
        EventResult.fromEvent(commitEvent, commitEvent.data)
    }
}
