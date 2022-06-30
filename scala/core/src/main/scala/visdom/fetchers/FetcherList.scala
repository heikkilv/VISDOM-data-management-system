// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.fetchers

import java.util.concurrent.locks.ReentrantReadWriteLock
import org.mongodb.scala.bson.collection.immutable.Document
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success
import visdom.utils.WartRemoverConstants


@SuppressWarnings(Array(WartRemoverConstants.WartsVar))
class FetcherList() {
    private var waitingFetchers: Seq[DataHandler] = Seq.empty
    private var isFetcherRunning: Boolean = false

    private val listLock: ReentrantReadWriteLock = new ReentrantReadWriteLock()
    private val stateLock: ReentrantReadWriteLock = new ReentrantReadWriteLock()

    def getNumberOfFetchers(): Int = {
        listLock.readLock().lock()
        val numberOfTasks: Int = waitingFetchers.size
        listLock.readLock().unlock()

        numberOfTasks
    }

    def addFetcher(fetcher: DataHandler): Unit = {
        listLock.writeLock().lock()
        waitingFetchers = waitingFetchers ++ Seq(fetcher)
        listLock.writeLock().unlock()

        startNextFetcher()
    }

    def addFetchers(fetchers: Seq[DataHandler]): Unit = {
        listLock.writeLock().lock()
        waitingFetchers = waitingFetchers ++ fetchers
        listLock.writeLock().unlock()

        startNextFetcher()
    }

    def getIsFetcherRunning(): Boolean = {
        stateLock.readLock().lock()
        val isFetcherRunningCopy: Boolean = isFetcherRunning
        stateLock.readLock().unlock()

        isFetcherRunningCopy
    }

    private def popFetcher(): Option[DataHandler] = {
        getIsFetcherRunning() match {
            case true => None
            case false => {
                listLock.writeLock().lock()
                val topFetcher: Option[DataHandler] = waitingFetchers.headOption
                topFetcher match {
                    case Some(_) => waitingFetchers = waitingFetchers.drop(1)
                    case None =>
                }
                listLock.writeLock().unlock()

                topFetcher
            }
        }
    }

    private def setIsFetcherRunning(runningState: Boolean): Unit = {
        stateLock.writeLock().lock()
        isFetcherRunning = runningState
        stateLock.writeLock().unlock()
    }

    private def startNextFetcher(): Unit = {
        def processFetcher(fetcher: DataHandler) = {
            setIsFetcherRunning(true)
            println(s"Starting fetcher 1/${getNumberOfFetchers() + 1}")

            val resultCount: Int = fetcher.process() match {
                case Some(documents: Array[Document]) => documents.size
                case None => 0
            }
            println(
                s"Found ${resultCount} results with ${fetcher.getFetcherType()} fetcher " +
                s"using options ${fetcher.getOptions()}"
            )
        }

        def startNextFetcherInternal(errorMessage: Option[String]): Unit = {
            setIsFetcherRunning(false)
            errorMessage match {
                case Some(error: String) => println(error)
                case None =>
            }
            startNextFetcher()
        }

        popFetcher() match {
            case Some(fetcher: DataHandler) => {
                Future(processFetcher(fetcher)).onComplete {
                    case Success(_) => startNextFetcherInternal(None)
                    case Failure(error: Throwable) => startNextFetcherInternal(Some(s"Fetcher error: ${error}"))
                }
            }
            case None =>
        }
    }
}
