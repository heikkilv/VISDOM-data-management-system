package visdom.adapters.utils

import visdom.adapters.general.model.events.CommitEvent
import visdom.adapters.general.schemas.GitlabEventPushDataSchema
import visdom.adapters.general.schemas.GitlabEventSchema


object ModelHelperUtils {
    def getCommitAncestors(commitId: String, limit: Int, parentCommitMap: Map[String, Seq[String]]): Set[String] = {
        def getAncestors(currentCommits: Seq[String], ancestors: Set[String]): Set[String] = {
            ancestors.size < limit match {
                case true => currentCommits.headOption match {
                    case Some(currentCommit: String) => {
                        val newParents: Seq[String] = parentCommitMap.get(currentCommit) match {
                            case Some(parentCommits: Seq[String]) => parentCommits
                            case None => Seq.empty
                        }
                        getAncestors(currentCommits.drop(1) ++ newParents, ancestors + currentCommit)
                    }
                    case None => ancestors
                }
                case false => ancestors
            }
        }

        getAncestors(Seq(commitId), Set.empty)
    }

    def getCommitList(
        commitCount: Int,
        lastCommitId: String,
        targetCommitId: Option[String],
        parentCommitMap: Map[String, Seq[String]]
    ): Set[String] = {
        val targetAncestors: Set[String] = targetCommitId match {
            case Some(targetCommit: String) => getCommitAncestors(targetCommit, commitCount, parentCommitMap)
            case None => Set.empty
        }

        def getParentCommits(
            currentCommits: Seq[String],
            targetCommit: String,
            previousCommits: Set[String]
        ): Set[String] = {
            previousCommits.size < commitCount match {
                case true => currentCommits.headOption match {
                    case Some(currentCommit: String) => {
                        currentCommit != targetCommit &&
                        !previousCommits.contains(currentCommit) &&
                        !targetAncestors.contains(currentCommit) match {
                            case true => {
                                val newParents: Seq[String] = parentCommitMap.get(currentCommit) match {
                                    case Some(parents: Seq[String]) => parents
                                        .filter(parent => parent != targetCommit && !previousCommits.contains(parent))
                                    case None => Seq.empty
                                }
                                getParentCommits(
                                    currentCommits.drop(1) ++ newParents,
                                    targetCommit,
                                    previousCommits + currentCommit
                                )
                            }
                            case false => getParentCommits(currentCommits.drop(1), targetCommit, previousCommits)
                        }
                    }
                    case None => previousCommits
                }
                case false => previousCommits
            }
        }

        commitCount match {
            case 0 => Set.empty
            case 1 => Set(lastCommitId)
            case n: Int if n >= 2 => targetCommitId match {
                case Some(targetCommit: String) => getParentCommits(Seq(lastCommitId), targetCommit, Set.empty)
                case None => Set(lastCommitId)
            }
            case _ => Set.empty
        }
    }

    def getEventCommits(
        projectNames: Map[Int, String],
        parentCommits: Map[String, Seq[String]],
        event: GitlabEventSchema
    ): Seq[String] = {
        event.push_data match {
            case Some(pushDataSchema: GitlabEventPushDataSchema) => projectNames.get(event.project_id) match {
                case Some(projectName: String) => pushDataSchema.commit_to match {
                    case Some(lastCommitId: String) =>
                            getCommitList(
                                pushDataSchema.commit_count,
                                lastCommitId,
                                pushDataSchema.commit_from,
                                parentCommits
                            ).map(commitId => CommitEvent.getId(event.host_name, projectName, commitId))
                            .toSeq
                    case None => Seq.empty
                }
                case None => Seq.empty
            }
            case None => Seq.empty
        }
    }
}
