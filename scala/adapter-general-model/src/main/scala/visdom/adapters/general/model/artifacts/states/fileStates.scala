package visdom.adapters.general.model.artifacts.states

import visdom.adapters.general.model.base.State


abstract class FileState extends State

// TODO: consider the states more carefully for the file objects

final case object FileExists extends FileState

final case object FileRemoved extends FileState
