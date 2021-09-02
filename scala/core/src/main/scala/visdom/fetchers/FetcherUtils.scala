package visdom.fetchers

import org.mongodb.scala.bson.Document
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success
import visdom.json.JsonUtils.EnrichedBsonDocument
import visdom.utils.AttributeConstants


object FetcherUtils {
    implicit val ec: ExecutionContext = ExecutionContext.global

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

    def handleDataFetchingSequence[Parameters](
        dataFetchers: Seq[Parameters => Unit],
        fetchParameters: Parameters
    ): Unit = {
        val (fetchers, parameters) = dataFetchers.map(fetcher => (fetcher, fetchParameters)).unzip
        handleDataFetchingSequence(fetchers, parameters)
    }

    def handleDataFetchingSequence[Parameters](
        dataFetchers: Seq[Parameters => Unit],
        fetchParameters: Seq[Parameters]
    ): Unit = {
        def handleDataFetchingInternal(fetchersWithParameters: Seq[(Parameters => Unit, Parameters)]): Unit = {
            fetchersWithParameters.headOption match {
                case Some((fetcher, parameters)) => {
                    println(s"Starting fetching process with parameters: ${parameters}")
                    val fetcherFuture: Future[Unit] = Future(fetcher(parameters))
                    fetcherFuture.onComplete({
                        case Success(_) => handleDataFetchingInternal(fetchersWithParameters.drop(1))
                        case Failure(error: Throwable) => println(s"Error: ${error.getMessage()}")
                    })
                }
                case None => println(
                    s"Data fetching sequence was completed successfully."
                )
            }
        }

        val dataFetchersWithParameters: Seq[(Parameters => Unit, Parameters)] = dataFetchers.zip(fetchParameters)
        println(s"Started data fetching sequence with ${dataFetchersWithParameters.size} fetchers.")
        handleDataFetchingInternal(dataFetchersWithParameters)
    }
}
