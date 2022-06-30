// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.adapters.options

import visdom.adapters.general.model.base.Artifact
import visdom.adapters.general.model.base.Author
import visdom.adapters.general.model.base.Event
import visdom.adapters.general.model.base.Metadata
import visdom.adapters.general.model.base.Origin
import visdom.utils.CommonConstants


trait ObjectTypesTrait {
    final val TargetTypeAll: String = "All"

    val TargetTypeOrigin: String = Origin.OriginType
    val TargetTypeEvent: String = Event.EventType
    val TargetTypeAuthor: String = Author.AuthorType
    val TargetTypeArtifact: String = Artifact.ArtifactType
    val TargetTypeMetadata: String = Metadata.MetadataType

    val OriginTypes: Set[String]
    val EventTypes: Set[String]
    val AuthorTypes: Set[String]
    val ArtifactTypes: Set[String]
    val MetadataTypes: Set[String]

    val objectTypes: Map[String, Set[String]]

    def getTargetType(objectType: String): Option[String] = {
        objectTypes
            .find({case (_, types) => types.contains(objectType)})
            .map({case (targetType, _) => targetType})
    }

    final val BooleanType: String = "boolean"
    final val DoubleType: String = "double"
    final val IntType: String = "int"
    final val StringType: String = "string"
    final val DefaultAttributeType: String = StringType

    def toName(attributeNames: String*): String = {
        attributeNames.mkString(CommonConstants.Dot)
    }

    // The default attribute type is String => only non-string attributes should be listed here
    val attributeTypes: Map[String, Map[String, String]]

    def getAttributeType(objectType: String, attributeName: String): String = {
        attributeTypes.get(objectType) match {
            case Some(attributeMap: Map[String, String]) => attributeMap.get(attributeName) match {
                case Some(attributeType: String) => attributeType
                case None => DefaultAttributeType
            }
            case None => DefaultAttributeType
        }
    }
}
