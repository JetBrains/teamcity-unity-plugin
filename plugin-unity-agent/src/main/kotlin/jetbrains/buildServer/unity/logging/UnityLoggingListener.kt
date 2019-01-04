/*
 * Copyright 2000-2019 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.unity.logging

import jetbrains.buildServer.agent.BuildProgressLogger
import jetbrains.buildServer.agent.runner.ProcessListenerAdapter
import java.util.*

class UnityLoggingListener(private val logger: BuildProgressLogger) : ProcessListenerAdapter() {

    private var blocks = Stack<LogBlock>()
    private val currentBlock: LogBlock
        get() = if (blocks.isEmpty()) defaultBlock else blocks.peek()

    override fun onStandardOutput(text: String) {
        currentBlock.apply {
            if (isBlockEnd(text)) {
                when (logLastLine) {
                    LogType.Outside -> {
                        logBlockClosed(name)
                        blocks.pop()
                        logMessage(text)
                    }
                    LogType.Inside -> {
                        logMessage(text)
                        logBlockClosed(name)
                        blocks.pop()
                    }
                    else -> {
                        logBlockClosed(name)
                        blocks.pop()
                    }
                }
                return
            }
        }

        val foundBlock = loggers.firstOrNull {
            it.isBlockStart(text)
        }
        if (foundBlock != null && foundBlock != currentBlock) {
            if (currentBlock != defaultBlock) {
                logBlockClosed(currentBlock.name)
                blocks.pop()
            }
            foundBlock.apply {
                when (logFirstLine) {
                    LogType.Outside -> {
                        logMessage(text)
                        logBlockOpened(name)
                        blocks.push(this)
                    }
                    LogType.Inside -> {
                        logBlockOpened(name)
                        blocks.push(this)
                        logMessage(text)
                    }
                    else -> {
                        logBlockOpened(name)
                        blocks.push(this)
                    }
                }
            }
        } else {
            logMessage(text)
        }
    }

    private fun logMessage(text: String) {
        logger.message(currentBlock.getText(text))
    }

    private fun logBlockOpened(name: String) {
        logger.message("##teamcity[blockOpened name='$name']")
    }

    private fun logBlockClosed(name: String) {
        logger.message("##teamcity[blockClosed name='$name']")
    }

    companion object {
        private val defaultBlock = DefaultBlock()
        private val loggers = listOf(
                BuildReportBlock(),
                CommandLineBlock(),
                CompileBlock(),
                ExtensionsBlock(),
                LightmapBlock(),
                MonoBlock(),
                PackageManagerBlock(),
                PerformanceBlock(),
                PlayerStatisticsBlock(),
                PrepareBlock(),
                RefreshBlock(),
                ScriptCompilationBlock(),
                UpdateBlock()
        )
    }
}