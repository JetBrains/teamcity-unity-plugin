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

package jetbrains.buildServer.unity.fetchers

import org.jetbrains.unity.CSharpParser
import org.jetbrains.unity.CSharpParserBaseListener
import java.lang.StringBuilder
import java.util.*

class UnityStaticMethodNamesListener : CSharpParserBaseListener() {

    val names = linkedMapOf<String, String?>()

    override fun enterMethod_declaration(method: CSharpParser.Method_declarationContext) {
        method.formal_parameter_list()?.let {
            if (!it.isEmpty) return
        }

        val commonMember = method.parent as CSharpParser.Common_member_declarationContext
        if (commonMember.children?.firstOrNull()?.text != "void") return

        val classMember = commonMember.parent as CSharpParser.Class_member_declarationContext
        val modifiers = classMember.all_member_modifiers().all_member_modifier().flatMap { context ->
            context.children.map { it.text }
        }

        if (!modifiers.any { METHOD_REQUIRED.contains(it) } || modifiers.any { METHOD_EXCLUDE.contains(it) }) {
            return
        }

        names += getMethodReference(classMember, method) to getDescription(classMember)
    }

    private fun getMethodReference(classMember: CSharpParser.Class_member_declarationContext,
                                   method: CSharpParser.Method_declarationContext): String {
        val builder = StringBuilder()

        val classDefinition = classMember.parent.parent.parent as CSharpParser.Class_definitionContext
        (classDefinition.parent?.parent?.parent?.parent?.parent as? CSharpParser.Namespace_declarationContext)?.let {
            builder.append(it.qualified_identifier().text).append(".")
        }

        builder.append(classDefinition.identifier().text)
        builder.append(".")
        builder.append(method.method_member_name().text)

        return builder.toString()
    }

    private fun getDescription(classMember: CSharpParser.Class_member_declarationContext): String? {
        classMember.attributes()?.children
                ?.filterIsInstance<CSharpParser.Attribute_sectionContext>()
                ?.forEach {
                    val attribute = it.attribute_list().children
                            .filterIsInstance<CSharpParser.AttributeContext>()
                            .firstOrNull { attribute ->
                                attribute.namespace_or_type_name().text == "MenuItem"
                            } ?: return null
                    return attribute.attribute_argument().first().text.trim('"')
                } ?: return null
        return null
    }

    companion object {
        val METHOD_REQUIRED = TreeSet<String>(String.CASE_INSENSITIVE_ORDER).apply {
            add("static")
        }
        val METHOD_EXCLUDE = TreeSet<String>(String.CASE_INSENSITIVE_ORDER).apply {
            add("private")
            add("internal")
        }
    }
}