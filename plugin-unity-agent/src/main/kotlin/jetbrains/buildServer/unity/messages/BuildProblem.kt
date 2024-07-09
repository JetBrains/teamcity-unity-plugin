

package jetbrains.buildServer.unity.messages

import jetbrains.buildServer.messages.serviceMessages.MessageWithAttributes
import jetbrains.buildServer.messages.serviceMessages.ServiceMessageTypes

class BuildProblem : MessageWithAttributes {
    private constructor(attributes: Map<String, String>) : super(ServiceMessageTypes.BUILD_PORBLEM, attributes)

    constructor(description: String) : this(mapOf("description" to description))
}
