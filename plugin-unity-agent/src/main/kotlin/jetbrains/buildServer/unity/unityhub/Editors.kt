/*
 * Copyright 2000-2019 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.unity.unityhub

import kotlinx.serialization.Serializable

@Serializable
data class Editor(val version: String, val location: List<String>?, val manual: Boolean)