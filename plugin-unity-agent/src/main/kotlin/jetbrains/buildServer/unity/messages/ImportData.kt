/*
 * Copyright 2000-2019 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.unity.messages

import jetbrains.buildServer.messages.serviceMessages.MessageWithAttributes

class ImportData(type: String, path: String) : MessageWithAttributes("importData", mapOf(
        "type" to type,
        "path" to path
))