// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.utils

import java.util.concurrent.locks.ReentrantReadWriteLock


class TaskList[T] {
    @SuppressWarnings(Array(WartRemoverConstants.WartsVar))
    private var waitingTasks: Seq[((T) => Unit, T)] = Seq.empty

    private val lock: ReentrantReadWriteLock = new ReentrantReadWriteLock()

    def getNumberOfTasks(): Int = {
        lock.readLock().lock()
        val numberOfTasks: Int = waitingTasks.size
        lock.readLock().unlock()

        numberOfTasks
    }

    def addTask(task: ((T) => Unit, T)): Unit = {
        lock.writeLock().lock()
        waitingTasks = waitingTasks ++ Seq(task)
        lock.writeLock().unlock()
    }

    def addTasks(tasks: Seq[((T) => Unit, T)]): Unit = {
        lock.writeLock().lock()
        waitingTasks = waitingTasks ++ tasks
        lock.writeLock().unlock()
    }

    def popTask(): Option[((T) => Unit, T)] = {
        lock.writeLock().lock()
        val topTask: Option[((T) => Unit, T)] = waitingTasks.headOption
        topTask match {
            case Some(_) => waitingTasks = waitingTasks.drop(1)
            case None =>
        }
        lock.writeLock().unlock()

        topTask
    }
}
