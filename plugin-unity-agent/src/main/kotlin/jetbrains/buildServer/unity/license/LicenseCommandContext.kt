package jetbrains.buildServer.unity.license

import jetbrains.buildServer.agent.AgentRunningBuild
import jetbrains.buildServer.unity.util.FileSystemService

interface LicenseCommandContext {
    val build: AgentRunningBuild
    val fileSystemService: FileSystemService
    val environmentVariables: Map<String, String>
    val workingDirectory: String
    fun resolvePath(path: String): String
}