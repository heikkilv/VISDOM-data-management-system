package visdom.fetchers.gitlab

import scalaj.http.HttpRequest
import scalaj.http.HttpResponse


object Main extends App
{
    private val endSleep: Int = 5000

    Routes.storeMetadata()

    val commits: Int = Routes.fetchCommits()
    val files: Int = Routes.fetchFiles
    println(s"Found ${commits} commits.")
    println(s"Found ${files} files.")

    println(s"Waiting for ${endSleep/1000} seconds.")
    Thread.sleep(endSleep)
    println("The end.")
}
