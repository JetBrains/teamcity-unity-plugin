/*
 * Copyright 2000-2023 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.unity

import jetbrains.buildServer.agent.AgentBuildRunnerInfo
import jetbrains.buildServer.agent.BuildAgentConfiguration
import jetbrains.buildServer.agent.BuildRunnerContext
import jetbrains.buildServer.agent.runner.MultiCommandBuildSession
import jetbrains.buildServer.agent.runner.MultiCommandBuildSessionFactory
import jetbrains.buildServer.unity.UnityConstants.RUNNER_TYPE
import jetbrains.buildServer.unity.detectors.DetectVirtualUnityEnvironmentCommand
import jetbrains.buildServer.unity.detectors.UnityToolProvider
import jetbrains.buildServer.unity.license.ActivatePersonalLicenseCommand
import jetbrains.buildServer.unity.license.ActivateProLicenseCommand
import jetbrains.buildServer.unity.license.ReturnProLicenseCommand
import jetbrains.buildServer.unity.license.UnityLicenseManager
import jetbrains.buildServer.unity.util.FileSystemService

class UnityBuildSessionFactory(
    private val unityToolProvider: UnityToolProvider,
    private val fileSystemService: FileSystemService,
) : MultiCommandBuildSessionFactory {

    override fun createSession(runnerContext: BuildRunnerContext): MultiCommandBuildSession =
        UnityCommandBuildSession(
            runnerContext,
            fileSystemService,
            unityEnvironmentProvider(runnerContext),
            unityLicenseManager(runnerContext),
        )

    private fun unityEnvironmentProvider(runnerContext: BuildRunnerContext) =
        UnityEnvironmentProvider(
            unityToolProvider,
            DetectVirtualUnityEnvironmentCommand(
                runnerContext,
            ),
        )

    private fun unityLicenseManager(runnerContext: BuildRunnerContext) = UnityLicenseManager(
        ActivatePersonalLicenseCommand(
            runnerContext,
            fileSystemService,
        ),
        ActivateProLicenseCommand(
            runnerContext,
            fileSystemService,
        ),
        ReturnProLicenseCommand(
            runnerContext,
            fileSystemService,
        ),
    )

    override fun getBuildRunnerInfo(): AgentBuildRunnerInfo {
        return object : AgentBuildRunnerInfo {
            override fun getType(): String {
                return RUNNER_TYPE
            }

            override fun canRun(config: BuildAgentConfiguration): Boolean {
                return true
            }
        }
    }
}
