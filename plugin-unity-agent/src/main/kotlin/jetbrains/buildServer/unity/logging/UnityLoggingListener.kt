/*
 * Copyright 2000-2021 JetBrains s.r.o.
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

package jetbrains.buildServer.unity.logging

import jetbrains.buildServer.agent.BuildProgressLogger
import jetbrains.buildServer.agent.runner.ProcessListenerAdapter
import jetbrains.buildServer.messages.Status
import jetbrains.buildServer.messages.serviceMessages.BlockClosed
import jetbrains.buildServer.messages.serviceMessages.BlockOpened
import jetbrains.buildServer.messages.serviceMessages.Message
import jetbrains.buildServer.unity.messages.BuildProblem
import java.util.*

class UnityLoggingListener(private val logger: BuildProgressLogger,
                           private val problemsProvider: LineStatusProvider) : ProcessListenerAdapter() {

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
        val message = currentBlock.getText(text)
        val status = problemsProvider.getLineStatus(message)
        val serviceMessage = when (status) {
            LineStatus.Warning -> Message(message, Status.WARNING.text, null).asString()
            LineStatus.Error -> BuildProblem(message).asString()
            else -> message
        }
        logger.message(serviceMessage)
    }

    private fun logBlockOpened(name: String) {
        logger.message(BlockOpened(name, null).asString())
    }

    private fun logBlockClosed(name: String) {
        logger.message(BlockClosed(name).asString())
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