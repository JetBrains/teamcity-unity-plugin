

package jetbrains.buildServer.unity.messages

import jetbrains.buildServer.messages.serviceMessages.MessageWithAttributes

class ImportData(type: String, path: String) : MessageWithAttributes("importData", mapOf(
        "type" to type,
        "path" to path,
        "nUnitV3TestNameHandlingMode" to "fullname",
        "nUnitV3SuiteLoggingMode" to "root-only"
))