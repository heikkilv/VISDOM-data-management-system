package visdom.adapters.general.model.base


abstract class Artifact
extends Construct {
    val state: String
    // val stateHistory
}

object Artifact {
    final val ArtifactType: String = "artifact"
}
