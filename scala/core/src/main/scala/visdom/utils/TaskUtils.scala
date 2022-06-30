// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.utils

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success
import visdom.http.HttpConstants


object TaskUtils {
    private def taskLoop[T](taskList: TaskList[T]): Unit = {
        val _: Unit = Thread.sleep(HttpConstants.FutureTaskDelayMs)
        executeNextTask(taskList)
    }

    private def continueTaskLoop[T](taskList: TaskList[T], printTaskNumbers: Boolean): Unit = {
        if (printTaskNumbers) {
            println(s"${taskList.getNumberOfTasks()} tasks remaining in the task list")
        }
        val _: Future[Unit] = Future(taskLoop(taskList))
    }

    private def executeNextTask[T](taskList: TaskList[T]): Unit = {
        taskList.popTask() match {
            case Some((nextTask, taskParameters)) => {
                Future(nextTask(taskParameters))
                    .onComplete {
                        case Success(_) => continueTaskLoop(taskList, true)
                        case Failure(error: Throwable) => {
                            println(s"Task error: ${error}")
                            continueTaskLoop(taskList, true)
                        }
                    }
            }
            case None => continueTaskLoop(taskList, false)
        }
    }

    def startTaskLoop[T](taskList: TaskList[T]): Unit = {
        executeNextTask(taskList)
    }
}
