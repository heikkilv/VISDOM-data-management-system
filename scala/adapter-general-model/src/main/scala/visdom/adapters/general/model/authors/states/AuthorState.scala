package visdom.adapters.general.model.authors.states

import visdom.adapters.general.model.base.State


final case class AuthorState(
    stateString: String
)
extends State

object AuthorState {
    val ActiveAuthorStateString: String = "active"

    val ActiveAuthorState: AuthorState = AuthorState(ActiveAuthorStateString)
}
