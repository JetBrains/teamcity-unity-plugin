

package jetbrains.buildServer.unity

import jetbrains.buildServer.tools.*
import jetbrains.buildServer.util.ArchiveUtil
import java.io.File
import java.io.FileFilter

class UnityServerToolProvider : ServerToolProviderAdapter() {

    override fun getType(): ToolType {
        return unityToolType
    }

    override fun tryGetPackageVersion(toolPackage: File): GetPackageVersionResult {
        if(!createPackageFilter().accept(toolPackage)) {
            return GetPackageVersionResult.error("Package file is invalid")
        }

        val prefix = "${type.type}-"
        if (!toolPackage.nameWithoutExtension.startsWith(prefix)) {
            return GetPackageVersionResult.error("Could not determine ${type.type} version based on its package file name ${toolPackage.name}.")
        }

        val version = toolPackage.nameWithoutExtension.substring(prefix.length)
        if(version.isEmpty()) {
            return GetPackageVersionResult.error("Could not determine ${type.type} version based on its package file name ${toolPackage.name}.")
        }

        return GetPackageVersionResult.version(SimpleToolVersion(type, version, "${type.shortDisplayName}-${version}",type.displayName))
    }

    override fun unpackToolPackage(toolPackage: File, targetDirectory: File) {
        ArchiveUtil.unpackZip(toolPackage, "", targetDirectory)
    }

    private fun createPackageFilter() =
            FileFilter {
                it.isFile
                        && it.nameWithoutExtension.startsWith(type.type, true)
                        && UnityConstants.UNITY_TOOL_EXTENSION.equals(it.extension, true)
            }

    companion object {
        private val unityToolType = UnityToolType()
    }
}