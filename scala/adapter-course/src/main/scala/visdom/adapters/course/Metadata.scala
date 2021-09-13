package visdom.adapters.course

import org.mongodb.scala.bson.BsonDocument
import visdom.http.server.response.CourseAdapterInfoResponse
import visdom.http.server.response.ComponentInfoResponse
import visdom.utils.MetadataConstants


object Metadata extends visdom.utils.Metadata {
    def getMetadataDocument(): BsonDocument = {
        BsonDocument(
            MetadataConstants.AttributeComponentName -> AdapterValues.componentName,
            MetadataConstants.AttributeComponentType -> AdapterValues.componentType,
            MetadataConstants.AttributeAdapterType -> AdapterValues.AdapterType,
            MetadataConstants.AttributeVersion -> AdapterValues.Version,
            MetadataConstants.AttributeApiAddress -> AdapterValues.fullApiAddress,
            MetadataConstants.AttributeSwaggerDefinition -> AdapterValues.swaggerDefinition,
            MetadataConstants.AttributeStartTime -> AdapterValues.startTime
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
        CourseAdapterInfoResponse(
            componentType = AdapterValues.componentName,
            componentName = AdapterValues.componentType,
            adapterType = AdapterValues.AdapterType,
            version = AdapterValues.Version,
            apiAddress = AdapterValues.fullApiAddress,
            swaggerDefinition = AdapterValues.swaggerDefinition,
            startTime = AdapterValues.startTime
        )
    }
}
