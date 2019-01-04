/*
 * Copyright 2000-2019 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.unity.logging

class PrepareBlock : LogBlock {

    override val name = "Prepare Build"

    override val logFirstLine = LogType.None

    override val logLastLine = LogType.None

    override fun isBlockStart(text: String) = text.contains("---- PrepareBuild Start ----")

    override fun isBlockEnd(text: String) = text.contains("---- PrepareBuild End ----")

    override fun getText(text: String) = text
}