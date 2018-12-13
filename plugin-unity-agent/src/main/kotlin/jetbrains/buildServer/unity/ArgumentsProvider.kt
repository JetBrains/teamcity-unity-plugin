/*
 * Copyright 2000-2018 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.unity

import jetbrains.buildServer.agent.BuildRunnerContext

/**
 * Provides arguments to the utility.
 */
interface ArgumentsProvider {
    fun getArguments(runnerContext: BuildRunnerContext): List<String>
}
