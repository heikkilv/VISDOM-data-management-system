package visdom.adapters.general.model.artifacts

import visdom.adapters.general.model.artifacts.data.TestSuiteData
import visdom.adapters.general.model.artifacts.states.TestSuiteState
import visdom.adapters.general.model.base.Artifact
import visdom.adapters.general.model.base.ItemLink
import visdom.adapters.general.model.events.PipelineEvent
import visdom.adapters.general.model.events.PipelineJobEvent
import visdom.adapters.general.model.origins.GitlabOrigin
import visdom.adapters.general.schemas.TestSuiteSchema
import visdom.utils.CommonConstants
import visdom.utils.GeneralUtils


class TestSuiteArtifact(
    testSuiteSchema: TestSuiteSchema,
    hostName: String,
    projectName: String,
    pipelineId: Int,
    pipelineJobId: Option[Int]
)
extends Artifact {
    def getType: String = TestSuiteArtifact.TestSuiteArtifactType

    val origin: ItemLink = ItemLink(
        GitlabOrigin.getId(hostName, projectName),
        GitlabOrigin.GitlabOriginType
    )

    val name: String = testSuiteSchema.name
    val description: String = s"Test suite ${testSuiteSchema.name} for pipeline ${pipelineId}"
    // NOTE: all test suites use the same state for now
    val state: String = TestSuiteState.SuiteComplete
    val data: TestSuiteData = TestSuiteData.fromTestSuiteSchema(testSuiteSchema)

    val id: String = TestSuiteArtifact.getId(origin.id, pipelineId, testSuiteSchema.name)

    // add pipeline and pipeline job events to related events
    addRelatedEvent(
        ItemLink(
            PipelineEvent.getId(origin.id, pipelineId),
            PipelineEvent.PipelineEventType
        )
    )
    pipelineJobId match {
        case Some(jobId: Int) => addRelatedEvent(
            ItemLink(
                PipelineJobEvent.getId(origin.id, jobId),
                PipelineJobEvent.PipelineJobEventType
            )
        )
        case None =>
    }

    // add pipeline report artifact to related constructs
    addRelatedConstruct(
        ItemLink(
            PipelineReportArtifact.getId(origin.id, pipelineId),
            PipelineReportArtifact.PipelineReportArtifactType
        )
    )
    // add test case artifacts to related constructs
    addRelatedConstructs(
        testSuiteSchema.test_cases.map(
            testCase => ItemLink(
                TestCaseArtifact.getId(origin.id, pipelineId, name, testCase.name),
                TestCaseArtifact.TestCaseArtifactType
            )
        )
    )
}

object TestSuiteArtifact {
    final val TestSuiteArtifactType: String = "test_suite"

    def getId(originId: String, pipelineId: Int, testSuiteName: String): String = {
        GeneralUtils.getUuid(originId, TestSuiteArtifactType, pipelineId.toString(), testSuiteName)
    }
}
