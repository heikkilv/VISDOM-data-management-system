package visdom.adapters.general.model.artifacts.states

import visdom.adapters.general.model.base.State
import org.mongodb.scala.bson.BsonString


final case class FileState(
    stateString: String
)
extends State

// TODO: consider the states more carefully for the file objects

object FileState {
    final val FileExistsString: String = "exists"
    final val FileRemovedString: String = "removed"

    final val FileExists: FileState = FileState(FileExistsString)
    final val FileRemoved: FileState = FileState(FileRemovedString)
}
