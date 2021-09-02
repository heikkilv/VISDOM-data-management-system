package visdom.fetchers

import org.mongodb.scala.bson.BsonDocument
import visdom.json.JsonUtils.EnrichedBsonDocument
import visdom.utils.AttributeConstants
import visdom.utils.GeneralUtils.EnrichedWithToTuple


final case class FetcherType(
    componentName: String,
    fetcherType: String,
    version: String,
    apiAddress: String,
    information: BsonDocument
) {
    def usesMatchingSourceServer(sourceServer: String): Boolean = {
        information.getStringOption(AttributeConstants.AttributeSourceServer) match {
            case Some(fetcherSourceServer: String) => fetcherSourceServer == sourceServer
            case None => false
        }
    }

    def isMatchingFetcher(targetFetcherType: String, sourceServer: String): Boolean = {
        fetcherType == targetFetcherType && usesMatchingSourceServer(sourceServer)
    }
}

object FetcherType {
    def fromBsonDocument(document: BsonDocument): Option[FetcherType] = {
        val requiredAttributes: Option[(String, String, String, String)] =
            document.getManyStringOption(
                AttributeConstants.AttributeComponentName,
                AttributeConstants.AttributeFetcherType,
                AttributeConstants.AttributeVersion,
                AttributeConstants.AttributeApiAddress
            ).map(valueSequence => valueSequence.toTuple4)
        val informationAttribute: Option[BsonDocument] =
            document.getDocumentOption(AttributeConstants.AttributeInformation)

        requiredAttributes match {
            case Some((componentName: String, fetcherType: String, version: String, apiAddress: String)) =>
                Some(
                    FetcherType(
                        componentName = componentName,
                        fetcherType = fetcherType,
                        version = version,
                        apiAddress = apiAddress,
                        information = informationAttribute match {
                            case Some(information: BsonDocument) => information
                            case None => BsonDocument()
                        }
                    )
                )
            case None => None
        }
    }
}
