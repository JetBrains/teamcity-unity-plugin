/*
 * Copyright 2000-2019 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.unity.logging

interface LogBlock {
    val name: String
    val logFirstLine: LogType
    val logLastLine: LogType
    fun isBlockStart(text: String): Boolean
    fun isBlockEnd(text: String): Boolean
    fun getText(text: String): String
}

enum class LogType {
    None,
    Inside,
    Outside
}