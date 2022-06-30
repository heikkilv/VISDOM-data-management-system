// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.utils

import org.apache.spark.sql.SparkSession
import visdom.spark.Session


object SparkSessionUtils {
    @SuppressWarnings(Array(WartRemoverConstants.WartsVar))
    private var sessionCounter: Int = 0
    @SuppressWarnings(Array(WartRemoverConstants.WartsVar))
    private var sparkSession: Option[SparkSession] = None

    def getSparkSession(): SparkSession = {
        sparkSession match {
            case Some(session: SparkSession) => {
                sessionCounter += 1
                session
            }
            case None => {
                val session: SparkSession = Session.getSparkSession()
                sessionCounter = 1
                sparkSession = Some(session)
                session
            }
        }
    }

    def releaseSparkSession(): Unit = {
        sessionCounter match {
            case n: Int if n > 1 => sessionCounter -= 1
            case 1 => {
                sparkSession match {
                    case Some(session: SparkSession) => session.stop()
                    case None =>
                }

                sessionCounter = 0
                sparkSession = None
            }
            case _ =>
        }
    }
}
