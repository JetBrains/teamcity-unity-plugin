package jetbrains.buildServer.unity.license

import jetbrains.buildServer.agent.AgentRunningBuild
import jetbrains.buildServer.agent.BuildProgressLogger
import jetbrains.buildServer.unity.util.FileSystemService

interface LicenseCommandContext {
    val build: AgentRunningBuild
    val buildLogger: BuildProgressLogger
    val fileSystemService: FileSystemService
    val environmentVariables: Map<String, String>
    val workingDirectory: String
    fun resolvePath(path: String): String
}
