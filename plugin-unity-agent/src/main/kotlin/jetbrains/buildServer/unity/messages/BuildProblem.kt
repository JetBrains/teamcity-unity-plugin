/*
 * Copyright 2000-2019 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.unity.messages

import jetbrains.buildServer.messages.serviceMessages.MessageWithAttributes
import jetbrains.buildServer.messages.serviceMessages.ServiceMessageTypes

class BuildProblem : MessageWithAttributes {
    private constructor(attributes: Map<String, String>) : super(ServiceMessageTypes.BUILD_PORBLEM, attributes)

    constructor(description: String) : this(mapOf("description" to description))
}