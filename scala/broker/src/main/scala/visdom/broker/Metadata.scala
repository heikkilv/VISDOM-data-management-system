// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.broker

import org.mongodb.scala.bson.BsonDocument
import visdom.http.server.response.BrokerInfoResponse
import visdom.http.server.response.ComponentInfoResponse
import visdom.http.server.swagger.SwaggerConstants
import visdom.utils.MetadataConstants


object Metadata extends visdom.utils.Metadata {
    def getMetadataDocument(): BsonDocument = {
        BsonDocument(
            MetadataConstants.AttributeComponentName -> BrokerValues.componentName,
            MetadataConstants.AttributeComponentType -> BrokerValues.componentType,
            MetadataConstants.AttributeVersion -> BrokerValues.brokerVersion,
            MetadataConstants.AttributeApiAddress -> BrokerValues.apiAddress,
            MetadataConstants.AttributeSwaggerDefinition -> BrokerValues.swaggerDefinition,
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

    def getInfoResponse(): ComponentInfoResponse = {
        BrokerInfoResponse(
            componentType = BrokerValues.componentName,
            componentName = BrokerValues.componentType,
            version = BrokerValues.brokerVersion,
            apiAddress = BrokerValues.apiAddress,
            swaggerDefinition = BrokerValues.swaggerDefinition,
            startTime = BrokerValues.startTime
        )
    }
}
