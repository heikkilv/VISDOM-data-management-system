// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.utils

import java.io.IOException
import scala.io.BufferedSource
import scala.io.Source


object FileUtils {
    def getTextFileSource(filename: String): Option[BufferedSource] = {
        try {
            Some(Source.fromFile(filename))
        }
        catch {
            case error: IOException => {
                println(s"File opening error: ${error}")
                None
            }
        }
    }

    def readTextFile(filename: String): Option[String] = {
        val fileSourceOption: Option[BufferedSource] = getTextFileSource(filename)
        try {
            fileSourceOption match {
                case Some(fileSource: BufferedSource) => Some(fileSource.mkString)
                case None => None
            }
        }
        catch {
            case error: IOException => {
                println(s"File reading error: ${error}")
                None
            }
        }
        finally {
            fileSourceOption match {
                case Some(fileSource: BufferedSource) => fileSource.close()
                case None =>
            }
        }
    }
}
