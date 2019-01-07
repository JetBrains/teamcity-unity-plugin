/*
 * Copyright 2000-2019 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.unity.logging

import org.testng.Assert
import org.testng.annotations.Test
import java.io.File

class LineStatusProviderTest {

    @Test
    fun testCustomFile() {
        val customSettingsFile = File("src/test/resources/logger/customLogging.xml")
        val provider = LineStatusProvider(customSettingsFile)

        Assert.assertEquals(provider.getLineStatus("text"), LineStatus.Normal)
        Assert.assertEquals(provider.getLineStatus("error message"), LineStatus.Normal)
        Assert.assertEquals(provider.getLineStatus("warning message"), LineStatus.Normal)
        Assert.assertEquals(provider.getLineStatus("customWarning: message"), LineStatus.Warning)
        Assert.assertEquals(provider.getLineStatus("customError: message"), LineStatus.Error)
    }
}