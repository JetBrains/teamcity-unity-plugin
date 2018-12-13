/*
 * Copyright 2000-2018 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.unity

import jetbrains.buildServer.serverSide.discovery.BreadthFirstRunnerDiscoveryExtension
import jetbrains.buildServer.serverSide.discovery.DiscoveredObject
import jetbrains.buildServer.util.browser.Element
import java.util.*

/**
 * Performs unity build steps discovery.
 */
class UnityRunnerDiscoveryExtension : BreadthFirstRunnerDiscoveryExtension(1) {
    override fun discoverRunnersInDirectory(dir: Element, filesAndDirs: MutableList<Element>): MutableList<DiscoveredObject> {
        val result = ArrayList<DiscoveredObject>()
        return result
    }
}
