package visdom.fetchers.aplus

import org.mongodb.scala.bson.BsonDocument
import visdom.http.server.response.APlusFetcherInfoResponse
import visdom.http.server.response.ComponentInfoResponse
import visdom.utils.MetadataConstants


object Metadata extends visdom.utils.Metadata {
    def getMetadataDocument(): BsonDocument = {
        BsonDocument(
            MetadataConstants.AttributeComponentName -> FetcherValues.componentName,
            MetadataConstants.AttributeComponentType -> FetcherValues.componentType,
            MetadataConstants.AttributeFetcherType -> FetcherValues.FetcherType,
            MetadataConstants.AttributeVersion -> FetcherValues.FetcherVersion,
            MetadataConstants.AttributeSourceServer -> FetcherValues.sourceServer,
            MetadataConstants.AttributeDatabase -> FetcherValues.targetDatabaseName,
            MetadataConstants.AttributeApiAddress -> FetcherValues.fullApiAddress,
            MetadataConstants.AttributeSwaggerDefinition -> FetcherValues.swaggerDefinition,
            MetadataConstants.AttributeStartTime -> FetcherValues.startTime
        )
    }

    def getIdentifyingAttributes(): Array[String] = {
        Array(
            MetadataConstants.AttributeComponentName,
            MetadataConstants.AttributeComponentType,
            MetadataConstants.AttributeVersion
        )
    }

    def getInfoResponse(): ComponentInfoResponse = {
        APlusFetcherInfoResponse(
            componentType = FetcherValues.componentName,
            componentName = FetcherValues.componentType,
            fetcherType = FetcherValues.FetcherType,
            version = FetcherValues.FetcherVersion,
            sourceServer = FetcherValues.sourceServer,
            mongoDatabase = FetcherValues.targetDatabaseName,
            apiAddress = FetcherValues.fullApiAddress,
            swaggerDefinition = FetcherValues.swaggerDefinition,
            startTime = FetcherValues.startTime
        )
    }
}
