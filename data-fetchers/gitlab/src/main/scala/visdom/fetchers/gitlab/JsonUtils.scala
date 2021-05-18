package visdom.fetchers.gitlab

import io.circe.Json
import io.circe.JsonObject


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
}
