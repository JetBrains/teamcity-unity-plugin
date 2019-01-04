/*
 * Copyright 2000-2019 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.unity.logging

class RefreshBlock : LogBlock {

    override val name = blockName

    override val logFirstLine = LogType.Inside

    override val logLastLine = LogType.Inside

    override fun isBlockStart(text: String) = text.contains(prefix)

    override fun isBlockEnd(text: String) = text.isEmpty() || blockEnd.containsMatchIn(text)

    override fun getText(text: String) = text.removePrefix(prefix).capitalize()

    companion object {
        private const val blockName = "Refresh"
        private const val prefix = "$blockName: "
        private val blockEnd = Regex("Refresh completed in [^\\s]+ seconds")
    }
}