package visdom.fetchers.gitlab.utils

import io.circe.Json
import io.circe.JsonObject
import visdom.fetchers.gitlab.GitlabConstants


object JsonUtils {
    def onlyJsonObjects(sourceVector: Vector[Json]): Vector[JsonObject] = {
        def onlyObjectsInternal(
            sourceVectorInternal: Vector[Json],
            targetVector: Vector[JsonObject]
        ): Vector[JsonObject] = sourceVectorInternal.headOption match {
            case None => targetVector
            case Some(sourceHead: Json) => sourceHead.asObject match {
                case None => onlyObjectsInternal(sourceVector.drop(1), targetVector)
                case Some(sourceObject: JsonObject) => {
                    onlyObjectsInternal(sourceVectorInternal.drop(1), targetVector ++ Vector(sourceObject))
                }
            }
        }

        onlyObjectsInternal(sourceVector, Vector())
    }

    def addSubAttribute(
        jsonObject: JsonObject,
        rootAttribute: String,
        subAttribute: String,
        value: Json
    ): JsonObject = {
        jsonObject.apply(rootAttribute) match {
            case Some(element: Json) => element.asObject match {
                case Some(elementObject: JsonObject) => {
                    // combining the new value with previous values in the JSON object
                    val newElementObject: JsonObject = elementObject.add(subAttribute, value)
                    jsonObject.add(rootAttribute, Json.fromJsonObject(newElementObject))
                }
                case None => {
                    // the element was not a JSON object, overwriting it with a new value
                    jsonObject.add(
                        rootAttribute,
                        Json.fromFields(Vector((subAttribute, value)))
                    )
                }
            }
            case None => jsonObject.add(
                rootAttribute,
                Json.fromFields(Vector((subAttribute, value)))
            )
        }
    }

    def modifyJsonResult[T](
        results: Either[String, Vector[JsonObject]],
        modifier: (JsonObject, T) => JsonObject,
        modifierParameters: T
    ): Either[String, Vector[JsonObject]] = {
        results match {
            case Right(jsonObjectVector: Vector[JsonObject]) => {
                val modifiedJsonObjectVector: Vector[JsonObject] = jsonObjectVector.map(
                    jsonObject => modifier(jsonObject, modifierParameters)
                )
                Right(modifiedJsonObjectVector)
            }
            case Left(errorMessage: String) => Left(errorMessage)
        }
    }

    def addProjectName(jsonObject: JsonObject, projectName: String): JsonObject = {
        jsonObject.add(GitlabConstants.AttributeProjectName, Json.fromString(projectName))
    }

    def removeAttribute(jsonObject: JsonObject, attributeName: String): JsonObject = {
        jsonObject.remove(attributeName)
    }
}
