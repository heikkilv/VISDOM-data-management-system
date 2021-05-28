package visdom.fetchers.gitlab.utils

import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.Document
import org.mongodb.scala.bson.BsonValue
import org.mongodb.scala.bson.BsonNull
import visdom.fetchers.gitlab.GitlabConstants


object JsonUtils {
    implicit class EnrichedBsonDocument(document: BsonDocument) {
        def getStringOption(key: Any): Option[String] = {
            document.containsKey(key) match {
                case true => document.get(key).isString() match {
                    case true => Some(document.getString(key).getValue())
                    case false => None
                }
                case false => None
            }
        }
    }

    private val TempKey: String = "Temp"
    private val TempJsonStart: String = s"{'${TempKey}':"
    private val TempJsonEnd: String = "}"

    private def toTempJsonDocument(jsonString: String): String = {
        TempJsonStart + jsonString + TempJsonEnd
    }

    def parseJson(jsonString: String): BsonValue = {
        Document.apply(toTempJsonDocument(jsonString)).getOrElse(TempKey, new BsonNull)
    }

    def removeAttribute(document: BsonDocument, attributeName: String): BsonDocument = {
        val a = Document()
        document.containsKey(attributeName) match {
            case true => {
                // remove method removes the key from the document and returns the removed value
                val removedValue: BsonValue = document.remove(attributeName)
                document
            }
            case false => document
        }
    }
}
