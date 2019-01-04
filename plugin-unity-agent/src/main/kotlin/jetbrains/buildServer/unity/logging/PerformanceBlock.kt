/*
 * Copyright 2000-2019 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.unity.logging

class PerformanceBlock :LogBlock {

    override val name = blockName

    override val logFirstLine = LogType.Inside

    override val logLastLine = LogType.Outside

    override fun isBlockStart(text: String) = text.contains(prefix)

    override fun isBlockEnd(text: String) = !isBlockStart(text)

    override fun getText(text: String) = text.removePrefix(prefix)

    companion object {
        private const val blockName = "Performance"
        private const val prefix = "[$blockName] "
    }
}