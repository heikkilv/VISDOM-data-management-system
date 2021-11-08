package visdom.adapters

import org.mongodb.scala.bson.BsonDocument
import visdom.http.server.response.InfoResponse
import visdom.http.server.response.ComponentInfoResponse
import visdom.utils.MetadataConstants


trait Metadata extends visdom.utils.Metadata {
    val adapterValues: AdapterValues = AdapterApp.adapterApp.adapterValues

    def getMetadataDocument(): BsonDocument = {
        BsonDocument(
            MetadataConstants.AttributeComponentName -> adapterValues.componentName,
            MetadataConstants.AttributeComponentType -> adapterValues.componentType,
            MetadataConstants.AttributeAdapterType -> adapterValues.AdapterType,
            MetadataConstants.AttributeVersion -> adapterValues.Version,
            MetadataConstants.AttributeApiAddress -> adapterValues.fullApiAddress,
            MetadataConstants.AttributeSwaggerDefinition -> adapterValues.swaggerDefinition,
            MetadataConstants.AttributeStartTime -> adapterValues.startTime
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
        InfoResponse(
            componentType = adapterValues.componentName,
            componentName = adapterValues.componentType,
            adapterType = adapterValues.AdapterType,
            version = adapterValues.Version,
            apiAddress = adapterValues.fullApiAddress,
            swaggerDefinition = adapterValues.swaggerDefinition,
            startTime = adapterValues.startTime
        )
    }
}

object DefaultMetadata extends Metadata
