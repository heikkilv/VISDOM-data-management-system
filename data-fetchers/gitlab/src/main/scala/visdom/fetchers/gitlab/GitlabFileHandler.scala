package visdom.fetchers.gitlab

import java.time.Instant
import scalaj.http.Http
import scalaj.http.HttpConstants.utf8
import scalaj.http.HttpConstants.urlEncode
import scalaj.http.HttpRequest
import scala.collection.JavaConverters.seqAsJavaListConverter
import org.mongodb.scala.MongoCollection
import org.mongodb.scala.MongoDatabase
import org.mongodb.scala.bson.BsonArray
import org.mongodb.scala.bson.BsonBoolean
import org.mongodb.scala.bson.BsonDateTime
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.BsonElement
import org.mongodb.scala.bson.BsonInt32
import org.mongodb.scala.bson.BsonNull
import org.mongodb.scala.bson.BsonString
import org.mongodb.scala.bson.BsonValue
import org.mongodb.scala.bson.Document
import visdom.database.mongodb.MongoConstants
import visdom.fetchers.gitlab.utils.JsonUtils.EnrichedBsonDocument


class GitlabFileHandler(options: GitlabFileOptions)
    extends GitlabDataHandler {

    def getRequest(): HttpRequest = {
        // https://docs.gitlab.com/ee/api/repositories.html#list-repository-tree
        val uri: String = List(
            options.hostServer.baseAddress,
            GitlabConstants.PathProjects,
            urlEncode(options.projectName, utf8),
            GitlabConstants.PathRepository,
            GitlabConstants.PathTree
        ).mkString("/")

        val commitRequest: HttpRequest = processOptionalParameters(
            Http(uri).param(GitlabConstants.ParamRef, options.reference)
        )
        options.hostServer.modifyRequest(commitRequest)
    }

    override def getCollection(): Option[MongoCollection[Document]] = {
        options.mongoDatabase match {
            case Some(database: MongoDatabase) => Some(
                database.getCollection(MongoConstants.CollectionFiles)
            )
            case None => None
        }
    }

    override def getIdentifierAttributes(): Array[String] = {
        Array(
            GitlabConstants.ParamPath,
            GitlabConstants.AttributeProjectName,
            GitlabConstants.AttributeHostName
        )
    }

    override def processDocument(document: BsonDocument): BsonDocument = {
        val filePathOption: Option[String] = document.getStringOption(GitlabConstants.AttributePath)
        val linkDocumentOption: Option[BsonDocument] = filePathOption match {
            case Some(filePath: String) => collectData(Seq(
                (GitlabConstants.AttributeCommits, options.includeCommitLinks match {
                    case Some(includeCommitLinks: Boolean) if includeCommitLinks => {
                        fetchLinkData(filePath) match {
                            case Some(value) => Some(
                                // only include the commit id from the commit data
                                value.map(
                                    linkDocument => simplifyCommitLink(linkDocument)
                                ).flatten
                            )
                            case None => None
                        }
                    }
                    case _ => None
                })
            ))
            case None => None
        }

        val documentWithMetadata: BsonDocument = addIdentifierAttributes(document).append(
            GitlabConstants.AttributeMetadata, getMetadata()
        )
        linkDocumentOption match {
            case Some(linkDocument: BsonDocument) => documentWithMetadata.append(
                GitlabConstants.AttributeLinks, linkDocument
            )
            case None => documentWithMetadata
        }
    }

    private def addIdentifierAttributes(document: BsonDocument): BsonDocument = {
        document
            .append(GitlabConstants.AttributeProjectName, new BsonString(options.projectName))
            .append(GitlabConstants.AttributeHostName, new BsonString(options.hostServer.hostName))
    }

    private def getMetadata(): BsonDocument = {
        new BsonDocument(
            List(
                new BsonElement(
                    GitlabConstants.AttributeLastModified,
                    new BsonDateTime(Instant.now().toEpochMilli())
                ),
                new BsonElement(
                    GitlabConstants.AttributeApiVersion,
                    new BsonInt32(GitlabConstants.GitlabApiVersion)
                ),
                new BsonElement(
                    GitlabConstants.AttributeIncludeLinksCommits,
                    new BsonBoolean(options.includeCommitLinks.getOrElse(false))
                )
            ).asJava
        )
    }

    private def simplifyCommitLink(document: Document): Option[BsonString] = {
        document.containsKey(GitlabConstants.AttributeId) match {
            case true => document.get(GitlabConstants.AttributeId) match {
                case Some(idAttribute: BsonValue) => idAttribute.isString() match {
                    case true => Some(idAttribute.asString())
                    case false => None
                }
                case None => None
            }
            case false => None
        }
    }

    def collectData(documentData: Seq[(String, Option[Array[BsonString]])]): Option[BsonDocument] = {
        def collectDataInternal(
            documentInternal: Option[BsonDocument],
            dataInternal: Seq[(String, Option[BsonArray])]
        ): Option[BsonDocument] = {
            dataInternal.headOption match {
                case Some(dataElement: (String, Option[BsonArray])) => collectDataInternal(
                    (dataElement._2 match {
                        case Some(actualData: BsonArray) => documentInternal match {
                            case Some(internalDocument: BsonDocument) =>
                                Some(internalDocument.append(dataElement._1, actualData))
                            case None =>
                                Some(new BsonDocument(dataElement._1, actualData))
                        }
                        case None => documentInternal
                    }),
                    dataInternal.drop(1)
                )
                case None => documentInternal
            }
        }

        collectDataInternal(
            None,
            documentData.map(
                documentElement => (
                    documentElement._1,
                    documentElement._2 match {
                        case Some(stringArray: Array[BsonString]) => Some(
                            new BsonArray(stringArray.toList.asJava)
                        )
                        case None => None
                    }
                )
            )
        )
    }

    private def fetchLinkData(filePath: String): Option[Array[Document]] = {
        val commitOptions: GitlabCommitOptions = GitlabCommitOptions(
            hostServer = options.hostServer,
            mongoDatabase = None,
            projectName = options.projectName,
            reference = options.reference,
            startDate = None,
            endDate = None,
            filePath = Some(filePath),
            includeStatistics = None,
            includeFileLinks = None,
            includeReferenceLinks = None
        )
        val commitFetcher: GitlabCommitHandler = new GitlabCommitHandler(commitOptions)
        commitFetcher.process()
    }

    private def processOptionalParameters(request: HttpRequest): HttpRequest = {
        @SuppressWarnings(Array("org.wartremover.warts.Var"))
        var paramMap: Seq[(String, String)] = Seq.empty

        options.filePath match {
            case Some(filePath: String) => {
                paramMap = paramMap ++ Seq((
                    GitlabConstants.ParamPath, filePath
                ))
            }
            case None =>
        }

        options.useRecursiveSearch match {
            case Some(useRecursiveSearch: Boolean) => {
                paramMap = paramMap ++ Seq((
                    GitlabConstants.ParamRecursive,
                    useRecursiveSearch.toString()
                ))
            }
            case None =>
        }

        request.params(paramMap)
    }
}
