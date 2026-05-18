package jetbrains.buildServer.unity.fetchers

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jetbrains.buildServer.util.browser.Browser
import jetbrains.buildServer.util.browser.Element
import kotlin.test.Test

class UnityBuildProfileFetcherTest {

    private val projectPath = "/project"
    private val fetcher = UnityBuildProfileFetcher()

    @Test
    fun `conventional path with one asset file returns it in results`() {
        val browser = mockk<Browser>()
        val profileElement = mockAssetElement("Android.asset")
        val conventionalDirElement = mockDirElement(listOf(profileElement))

        every { browser.getElement(any()) } returns null
        every { browser.getElement("$projectPath/Assets/Settings/Build Profiles") } returns conventionalDirElement

        val result = fetcher.retrieveData(browser, projectPath)

        result.map { it.value } shouldContainExactlyInAnyOrder listOf("Assets/Settings/Build Profiles/Android.asset")
    }

    @Test
    fun `conventional path empty, fallback assets scan finds profile`() {
        val browser = mockk<Browser>()
        val profileElement = mockAssetElement("PC.asset")
        val profilesDirElement = mockDirElement(listOf(profileElement), dirName = "Build Profiles")
        val settingsDirElement = mockDirElement(listOf(profilesDirElement), dirName = "Settings")
        val assetsDirElement = mockDirElement(listOf(settingsDirElement), dirName = "Assets")

        every { browser.getElement(any()) } returns null
        every { browser.getElement("$projectPath/Assets") } returns assetsDirElement

        val result = fetcher.retrieveData(browser, projectPath)

        result.map { it.value } shouldContainExactlyInAnyOrder listOf("Assets/Settings/Build Profiles/PC.asset")
    }

    @Test
    fun `no asset files anywhere returns empty list`() {
        val browser = mockk<Browser>()
        every { browser.getElement(any()) } returns null

        val result = fetcher.retrieveData(browser, projectPath)

        result.shouldBeEmpty()
    }

    @Test
    fun `conventional path has profiles, fallback scan is not performed`() {
        val browser = mockk<Browser>()
        val profileElement = mockAssetElement("iOS.asset")
        val conventionalDirElement = mockDirElement(listOf(profileElement))

        every { browser.getElement(any()) } returns null
        every { browser.getElement("$projectPath/Assets/Settings/Build Profiles") } returns conventionalDirElement

        fetcher.retrieveData(browser, projectPath)

        verify(exactly = 0) { browser.getElement("$projectPath/Assets") }
    }

    private fun mockAssetElement(assetName: String) = mockk<Element> {
        every { name } returns assetName
        every { isLeaf } returns true
        every { children } returns emptyList()
    }

    private fun mockDirElement(childList: List<Element>, dirName: String = "Build Profiles") = mockk<Element> {
        every { name } returns dirName
        every { isLeaf } returns false
        every { children } returns childList
    }
}
