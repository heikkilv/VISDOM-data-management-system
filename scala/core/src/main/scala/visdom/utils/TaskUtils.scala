package visdom.utils

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success
import visdom.http.HttpConstants


object TaskUtils {
    implicit val ec: ExecutionContext = ExecutionContext.global

    def startTaskSequence[T](taskSequence: Seq[((T) => Unit, T)]): Unit = {
        taskSequence.headOption match {
            case Some((nextTask, taskParameters)) => {
                val nextFutureTask: Future[Unit] = Future(nextTask(taskParameters))
                nextFutureTask.onComplete {
                    case Success(_) => {
                        val _: Future[Unit] = Future({
                            val _: Unit = Thread.sleep(HttpConstants.FutureTaskDelayMs)
                            startTaskSequence(taskSequence.drop(1))
                        })
                    }
                    case Failure(error: Throwable) => println(s"Task error: ${error.toString()}")
                }
            }
            case None =>
        }
    }
}
