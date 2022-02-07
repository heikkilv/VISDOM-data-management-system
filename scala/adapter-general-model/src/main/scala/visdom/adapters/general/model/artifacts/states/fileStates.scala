package visdom.adapters.general.model.artifacts.states

import visdom.adapters.general.model.base.State
import org.mongodb.scala.bson.BsonString


abstract class FileState extends State

// TODO: consider the states more carefully for the file objects

final case object FileExists extends FileState {
    val stateString: String = "exists"

}

final case object FileRemoved extends FileState {
    val stateString: String = "removed"
}
