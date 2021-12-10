/*
 * Copyright 2000-2021 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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