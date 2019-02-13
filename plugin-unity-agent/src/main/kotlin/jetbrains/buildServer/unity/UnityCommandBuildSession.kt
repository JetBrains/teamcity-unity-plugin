/*
 * Copyright 2000-2018 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.unity

import jetbrains.buildServer.agent.BuildFinishedStatus
import jetbrains.buildServer.agent.BuildRunnerContext
import jetbrains.buildServer.agent.runner.CommandExecution
import jetbrains.buildServer.agent.runner.CommandLineBuildService
import jetbrains.buildServer.agent.runner.MultiCommandBuildSession

/**
 * Unity runner service.
 */
class UnityCommandBuildSession(private val runnerContext: BuildRunnerContext,
                               private val unityToolProvider: UnityToolProvider) : MultiCommandBuildSession {

    private var buildSteps: Iterator<CommandExecution>? = null
    private var lastCommands = arrayListOf<CommandExecutionAdapter>()

    override fun sessionStarted() {
        buildSteps = getSteps()
    }

    override fun getNextCommand(): CommandExecution? {
        buildSteps?.let {
            if (it.hasNext()) {
                return it.next()
            }
        }

        return null
    }

    override fun sessionFinished(): BuildFinishedStatus? {
        return lastCommands.last().result
    }

    private fun getSteps() = iterator<CommandExecution> {
        yield(addCommand(UnityRunnerBuildService(unityToolProvider)))
    }

    private fun addCommand(buildService: CommandLineBuildService) = CommandExecutionAdapter(buildService.apply {
            this.initialize(runnerContext.build, runnerContext)
        }).apply {
            lastCommands.add(this)
        }
}
