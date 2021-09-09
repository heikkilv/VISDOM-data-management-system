package visdom.utils

import visdom.utils.metadata.ExerciseGitLocation


final case class GitlabFetcherQueryOptions(
    val projectNames: Seq[String],
    val gitLocation: ExerciseGitLocation
)
