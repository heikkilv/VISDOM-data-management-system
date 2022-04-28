package visdom.adapters.general.model.artifacts

import visdom.adapters.general.model.artifacts.data.TestCaseData
import visdom.adapters.general.model.base.Artifact
import visdom.adapters.general.model.base.ItemLink
import visdom.adapters.general.model.events.PipelineEvent
import visdom.adapters.general.model.events.PipelineJobEvent
import visdom.adapters.general.model.origins.GitlabOrigin
import visdom.adapters.general.schemas.TestCaseSchema
import visdom.utils.CommonConstants
import visdom.utils.GeneralUtils


class TestCaseArtifact(
    testCaseSchema: TestCaseSchema,
    hostName: String,
    projectName: String,
    pipelineId: Int,
    testSuiteName: String
)
extends Artifact {
    def getType: String = TestCaseArtifact.TestCaseArtifactType

    val origin: ItemLink = ItemLink(
        GitlabOrigin.getId(hostName, projectName),
        GitlabOrigin.GitlabOriginType
    )

    val name: String = testCaseSchema.name
    val description: String = s"Test case ${testCaseSchema.name} for pipeline ${pipelineId} and suite ${testSuiteName}"

    val state: String = testCaseSchema.status
    val data: TestCaseData = TestCaseData.fromTestCaseSchema(testCaseSchema, pipelineId, testSuiteName)

    val id: String = TestSuiteArtifact.getId(origin.id, pipelineId, testCaseSchema.name)

    // add pipeline event to related events
    addRelatedEvent(
        ItemLink(
            PipelineEvent.getId(origin.id, pipelineId),
            PipelineEvent.PipelineEventType
        )
    )

    // add pipeline report and test suite artifacts to related constructs
    addRelatedConstructs(
        Seq(
            ItemLink(
                PipelineReportArtifact.getId(origin.id, pipelineId),
                PipelineReportArtifact.PipelineReportArtifactType
            ),
            ItemLink(
                TestSuiteArtifact.getId(origin.id, pipelineId, testSuiteName),
                TestSuiteArtifact.TestSuiteArtifactType
            )
        )
    )
}

object TestCaseArtifact {
    final val TestCaseArtifactType: String = "test_case"

    def getId(originId: String, pipelineId: Int, testSuiteName: String, testCaseName: String): String = {
        GeneralUtils.getUuid(originId, TestCaseArtifactType, pipelineId.toString(), testSuiteName, testCaseName)
    }
}
