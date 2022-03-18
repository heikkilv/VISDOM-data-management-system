package visdom.adapters.general.model.artifacts.states


object PointsState {
    // TODO: consider the states more carefully for the points objects
    final val NotStarted: String = "not_started"
    final val Active: String = "active"
    final val Passed: String = "passed"
    final val Finished: String = "finished"
    final val Unknown: String = "unknown"
}
