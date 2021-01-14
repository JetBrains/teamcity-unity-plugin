<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<%--
  ~ Copyright 2000-2021 JetBrains s.r.o.
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  --%>

<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="params" class="jetbrains.buildServer.unity.UnityParametersProvider"/>
<jsp:useBean id="teamcityPluginResourcesPath" scope="request" type="java.lang.String"/>

<script type="text/javascript">
    var buildPlayerId = BS.Util.escapeId('${params.buildPlayer}');
    var runTestsId = BS.Util.escapeId('${params.runEditorTests}');

    BS.UnityParametersForm = {
        clearInputValues: function (row) {
            $j(row).find(':input').each(function (id, element) {
                var $element = $j(element);
                var name = $element.attr("name");
                if (!name || name.indexOf("prop:") !== 0) {
                    return;
                }
                var changed = false;
                if (element.name === "select") {
                    changed = element.selectedIndex !== 0;
                    element.selectedIndex = 0;
                } else if (element.type === "checkbox") {
                    changed = $element.is(':checked');
                    $element.removeAttr('checked');
                } else {
                    changed = $element.val() !== '';
                    $element.val('');
                }
                if (changed) {
                    $element.change();
                }
            });
        },
        updateElements: function () {
            var buildPlayer = $j(buildPlayerId).val();
            var $buildPlayerPath = $j('label[for="${params.buildPlayerPath}"]').closest('tr');
            if (buildPlayer) {
                $buildPlayerPath.show();
            } else {
                $buildPlayerPath.hide();
                BS.UnityParametersForm.clearInputValues($buildPlayerPath);
            }
            $j(".tests").toggle($j(runTestsId).is(':checked'));
            BS.MultilineProperties.updateVisible();
        }
    };

    $j(document).on('change', buildPlayerId + "," + runTestsId, function () {
        BS.UnityParametersForm.updateElements();
    });
</script>

<l:settingsGroup title="Build Parameters">
<props:workingDirectory/>
<tr>
    <th><label for="${params.projectPath}">Project path:</label></th>
    <td>
        <props:textProperty name="${params.projectPath}" className="longField">
            <jsp:attribute name="afterTextField">
                <bs:vcsTree fieldId="${params.projectPath}" dirsOnly="true"/>
            </jsp:attribute>
        </props:textProperty>
        <span class="error" id="error_${params.projectPath}"></span>
        <span class="smallNote">
            <span id="${params.projectPath}">Specify the path to the target project. If unspecified, will be used the working directory.</span>
        </span>
    </td>
</tr>

<tr>
    <th><label for="${params.executeMethod}">Execute method:</label></th>
    <td>
        <props:textProperty name="${params.executeMethod}" className="longField">
            <jsp:attribute name="afterTextField">
                <bs:projectData type="UnityStaticMethod" sourceFieldId="${params.projectPath}" selectionMode="single"
                                targetFieldId="${params.executeMethod}" popupTitle="Select a method reference"/>
            </jsp:attribute>
        </props:textProperty>
        <span class="error" id="error_${params.executeMethod}"></span>
        <span class="smallNote">
            Specify a static method reference to execute after the project is loaded.<br/>
            The class should be located in the Assets/Editor project directory.
        </span>
    </td>
</tr>

<tr class="advancedSetting">
    <th><label for="${params.buildTarget}">Build target:</label></th>
    <td>
        <props:textProperty name="${params.buildTarget}" className="longField">
            <jsp:attribute name="afterTextField">
                <bs:projectData type="UnityBuildTarget" sourceFieldId="${params.projectPath}" selectionMode="single"
                                targetFieldId="${params.buildTarget}" popupTitle="Select build target"/>
            </jsp:attribute>
        </props:textProperty>
        <span class="error" id="error_${params.buildTarget}"></span>
        <span class="smallNote">Specify an active build target before loading the project.</span>
    </td>
</tr>

<tr>
    <th><label for="${params.buildPlayer}">Standalone player:</label></th>
    <td>
        <props:selectProperty name="${params.buildPlayer}" enableFilter="true" className="mediumField">
            <props:option value="">&lt;Select&gt;</props:option>
            <c:forEach var="item" items="${params.buildPlayers}">
                <props:option value="${item.id}"><c:out value="${item.description}"/></props:option>
            </c:forEach>
        </props:selectProperty>
        <span class="error" id="error_${params.buildPlayer}"></span>
    </td>
</tr>

<tr style="display: none">
    <th class="noBorder"><label for="${params.buildPlayerPath}">Player output path: <l:star/></label></th>
    <td>
        <props:textProperty name="${params.buildPlayerPath}" className="longField">
            <jsp:attribute name="afterTextField">
                <c:if test="${not buildForm.template}">
                    <bs:agentArtifactsTree fieldId="${params.buildPlayerPath}" buildTypeId="${buildForm.externalId}"
                                           filesOnly="true"/>
                </c:if>
            </jsp:attribute>
        </props:textProperty>
        <span class="error" id="error_${params.buildPlayerPath}"></span>
        <span class="smallNote">Specify the output path for the player binary.</span>
    </td>
</tr>

<tr class="advancedSetting">
    <th>Options:</th>
    <td>
        <props:checkboxProperty name="${params.noGraphics}"/>
        <label for="${params.noGraphics}">Do not initialize the graphics device</label><br/>
        <props:checkboxProperty name="${params.silentCrashes}"/>
        <label for="${params.silentCrashes}">Do not display the error dialog when a standalone player crashes</label>
    </td>
</tr>
</l:settingsGroup>

<l:settingsGroup title="Test Parameters">
    <tr>
        <th>Tests:</th>
        <td>
            <props:checkboxProperty name="${params.runEditorTests}"/>
            <label for="${params.runEditorTests}">Run tests from the project</label>
        </td>
    </tr>
</tbody>
<tbody class="tests">
    <tr class="advancedSetting">
        <th>
            <label for="${params.testPlatform}">Test platform: <bs:help
                urlPrefix="https://docs.unity3d.com/Manual/testing-editortestsrunner.html" file=""/></label>
        </th>
        <td>
            <props:selectProperty name="${params.testPlatform}" enableFilter="true" className="mediumField">
                <props:option value="">&lt;Default&gt;</props:option>
                <props:option value="editmode">Edit mode</props:option>
                <props:option value="playmode">Play mode</props:option>
                <props:option value="all">All modes in sequence</props:option>
            </props:selectProperty>
            <span class="error" id="error_${params.testPlatform}"></span>
        </td>
    </tr>
    <tr class="advancedSetting">
        <th><label for="${params.testCategories}">Test categories: </label></th>
        <td>
            <props:multilineProperty expanded="true" name="${params.testCategories}" className="longField"
                                     rows="3" cols="49" linkTitle="Edit test categories"
                                     note="Specify list of test categories to execute."/>
        </td>
    </tr>
    <tr class="advancedSetting">
        <th><label for="${params.testNames}">Test names: </label></th>
        <td>
            <props:multilineProperty expanded="true" name="${params.testNames}" className="longField"
                                     rows="3" cols="49" linkTitle="Edit test names"
                                     note="Specify list of test names to execute."/>
        </td>
    </tr>
</tbody>
<tbody>
</l:settingsGroup>

<l:settingsGroup title="Unity Parameters" className="advancedSetting">
<tr class="advancedSetting">
    <th>
        <label for="${params.arguments}">Command line arguments: <bs:help
                urlPrefix="https://docs.unity3d.com/Manual/CommandLineArguments.html" file=""/>
        </label>
    </th>
    <td>
        <props:textProperty name="${params.arguments}" className="longField"/>
        <span class="error" id="error_${params.arguments}"></span>
        <span class="smallNote">Specify additional command line arguments for Unity.</span>
    </td>
</tr>

<tr class="advancedSetting">
    <th><label for="${params.unityVersion}">Unity version:</label></th>
    <td>
        <props:textProperty name="${params.unityVersion}" className="longField disableBuildTypeParams"/>
        <span class="error" id="error_${params.unityVersion}"></span>
        <span class="smallNote">Specify the required Unity version, e.g 2018.2.</span>
    </td>
</tr>

<tr class="advancedSetting">
    <th>
        <label for="${params.lineStatusesFile}">Line statuses file: <bs:help
            urlPrefix="https://github.com/JetBrains/teamcity-unity-plugin#custom-error-logging-settings" file=""/></label>
    </th>
    <td>
        <props:textProperty name="${params.lineStatusesFile}" className="longField">
        <jsp:attribute name="afterTextField">
            <bs:vcsTree fieldId="${params.lineStatusesFile}"/>
        </jsp:attribute>
        </props:textProperty>
        <span class="error" id="error_${params.lineStatusesFile}"></span>
        <span class="smallNote">
            Specify the path to the line status file which could override default error logging settings.
        </span>
    </td>
</tr>

<tr class="advancedSetting">
    <th><label for="${params.logFilePath}">Custom log path:</label></th>
    <td>
        <props:textProperty name="${params.logFilePath}" className="longField disableBuildTypeParams"/>
        <span class="error" id="error_${params.logFilePath}"></span>
        <span class="smallNote">Path for Unity log file</span>
    </td>
</tr>

<tr class="advancedSetting">
    <th><label for="${params.verbosity}">Logging verbosity:</label></th>
    <td>
        <props:selectProperty name="${params.verbosity}" enableFilter="true" className="mediumField">
            <props:option value="">&lt;Default&gt;</props:option>
            <c:forEach var="item" items="${params.verbosityValues}">
                <props:option value="${item.id}"><c:out value="${item.description}"/></props:option>
            </c:forEach>
        </props:selectProperty>
        <span class="error" id="error_${params.verbosity}"></span>
    </td>
</tr>
</l:settingsGroup>

<script type="text/javascript">
    BS.UnityParametersForm.updateElements();
</script>
