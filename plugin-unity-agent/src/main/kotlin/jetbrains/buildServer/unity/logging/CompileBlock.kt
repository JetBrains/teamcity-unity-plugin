/*
 * Copyright 2000-2019 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.unity.logging

class CompileBlock : LogBlock {

    override val name = "Compile"

    override val logFirstLine = LogType.None

    override val logLastLine = LogType.None

    override fun isBlockStart(text: String) = text.contains("-----Compiler Commandline Arguments:")

    override fun isBlockEnd(text: String) = text.contains("-----EndCompilerOutput---------------")

    override fun getText(text: String) = text
}