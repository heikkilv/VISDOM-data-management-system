package visdom.fetchers

import org.mongodb.scala.bson.Document
import visdom.json.JsonUtils.EnrichedBsonDocument
import visdom.utils.AttributeConstants


object FetcherUtils {
    def getFetcherResultIds(dataHandler: DataHandler): Seq[Int] = {
        dataHandler.process() match {
            case Some(resultDocuments: Array[Document]) =>
                resultDocuments.map(
                    resultDocument =>
                        resultDocument
                            .toBsonDocument
                            .getIntOption(AttributeConstants.AttributeId)
                ).flatten
            case None => Seq.empty  // did not get any results
        }
    }
}
