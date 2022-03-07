package visdom.fetchers.gitlab

import scalaj.http.Http
import scalaj.http.HttpConstants.utf8
import scalaj.http.HttpConstants.urlEncode
import scalaj.http.HttpRequest
import scalaj.http.HttpResponse
import org.mongodb.scala.bson.BsonDocument
import visdom.database.mongodb.MongoConstants
import visdom.http.HttpUtils
import visdom.json.JsonUtils.toBsonValue
import visdom.utils.CommonConstants


class GitlabProjectHandler(options: GitlabProjectOptions)
    extends GitlabDataHandler(options) {

    def getFetcherType(): String = GitlabConstants.FetcherTypeProject
    def getCollectionName(): String = MongoConstants.CollectionProjects

    override def responseToDocumentArray(response: HttpResponse[String]): Array[BsonDocument] = {
        HttpUtils.responseToDocumentArrayCaseDocument(response)
    }

    override def getOptionsDocument(): BsonDocument = {
        BsonDocument(
            options.projectIdentifier match {
                case Left(projectName: String) => GitlabConstants.AttributeProjectName -> projectName
                case Right(projectId: Int) => GitlabConstants.AttributeProjectId -> projectId
            },
            GitlabConstants.AttributeUseAnonymization -> options.useAnonymization
        )
    }

    def getRequest(): HttpRequest = {
        // https://docs.gitlab.com/ee/api/projects.html#get-single-project
        val uri: String = List(
            options.hostServer.baseAddress,
            GitlabConstants.PathProjects,
            options.projectIdentifier match {
                case Left(projectName: String) => urlEncode(projectName, utf8)
                case Right(projectId: Int) => projectId.toString()
            }

        ).mkString(CommonConstants.Slash)

        options.hostServer.modifyRequest(
            Http(uri)
                .param(GitlabConstants.ParamLicense, GitlabConstants.DefaultLicenseParam)
                .param(GitlabConstants.ParamStatistics, GitlabConstants.DefaultStatisticsParam)
        )
    }

    override def getIdentifierAttributes(): Array[String] = {
        Array(
            GitlabConstants.AttributeId,
            GitlabConstants.AttributeHostName
        )
    }

    override def getHashableAttributes(): Option[Seq[Seq[String]]] = {
        options.useAnonymization match {
            case true => Some(
                Seq(
                    Seq(GitlabConstants.AttributeDescription),
                    Seq(GitlabConstants.AttributeName),
                    Seq(GitlabConstants.AttributeNameWithNamespace),
                    Seq(GitlabConstants.AttributePath),
                    Seq(GitlabConstants.AttributePathWithNamespace),
                    Seq(GitlabConstants.AttributeSshUrlToRepo),
                    Seq(GitlabConstants.AttributeHttpUrlToRepo),
                    Seq(GitlabConstants.AttributeWebUrl),
                    Seq(GitlabConstants.AttributeReadmeUrl),
                    Seq(GitlabConstants.AttributeLicenseUrl),
                    Seq(GitlabConstants.AttributeAvatarUrl),
                    Seq(GitlabConstants.AttributeContainerRegistryImagePrefix),
                    Seq(GitlabConstants.AttributeSharedWithGroups, GitlabConstants.AttributeGroupName),
                    Seq(GitlabConstants.AttributeSharedWithGroups, GitlabConstants.AttributeGroupFullPath)
                )
            )
            case false => None
        }
    }

    override def processDocument(document: BsonDocument): BsonDocument = {
        addIdentifierAttributes(document)
            .append(GitlabConstants.AttributeMetadata, getMetadata())
    }

    private def getMetadata(): BsonDocument = {
        getMetadataBase()
            .append(GitlabConstants.AttributeUseAnonymization, toBsonValue(options.useAnonymization))
    }
}
