package visdom.broker

import org.mongodb.scala.bson.BsonDocument
import visdom.http.server.swagger.SwaggerConstants
import visdom.utils.MetadataConstants


object Metadata extends visdom.utils.Metadata {
    def getMetadataDocument(): BsonDocument = {
        BsonDocument(
            MetadataConstants.AttributeComponentName -> BrokerValues.componentName,
            MetadataConstants.AttributeComponentType -> BrokerValues.componentType,
            MetadataConstants.AttributeVersion -> BrokerValues.brokerVersion,
            MetadataConstants.AttributeApiAddress -> BrokerValues.apiAddress,
            MetadataConstants.AttributeSwaggerDefinition -> SwaggerConstants.SwaggerLocation,
            MetadataConstants.AttributeStartTime -> BrokerValues.startTime
        )
    }

    def getIdentifyingAttributes(): Array[String] = {
        Array(
            MetadataConstants.AttributeComponentName,
            MetadataConstants.AttributeComponentType,
            MetadataConstants.AttributeVersion
        )
    }
}
