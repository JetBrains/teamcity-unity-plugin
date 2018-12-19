<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="params" class="jetbrains.buildServer.unity.UnityParametersProvider"/>
<jsp:useBean id="teamcityPluginResourcesPath" scope="request" type="java.lang.String"/>

<link rel="stylesheet" href="${teamcityPluginResourcesPath}unity-settings.css">
<script type="text/javascript">
    var buildPlayerId = BS.Util.escapeId('${params.buildPlayer}');

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
            BS.MultilineProperties.updateVisible();
        }
    };

    $j(document).on('change', $j(buildPlayerId), function () {
        BS.UnityParametersForm.updateElements();
    });
</script>

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
            <span id="${params.projectPath}">Specify target project path to build.</span>
        </span>
    </td>
</tr>

<tr>
    <th><label for="${params.executeMethod}">Execute method:</label></th>
    <td>
        <props:textProperty name="${params.executeMethod}" className="longField">
            <jsp:attribute name="afterTextField">
                <bs:projectData type="UnityStaticMethod" sourceFieldId="${params.projectPath}" selectionMode="single"
                                targetFieldId="${params.executeMethod}" popupTitle="Select method reference"/>
            </jsp:attribute>
        </props:textProperty>
        <span class="error" id="error_${params.executeMethod}"></span>
        <span class="smallNote">
            Specify the static method reference to execute after project loading.<br/>
            Class should be located under Assets/Editor project directory.
        </span>
    </td>
</tr>

<tr class="advancedSetting">
    <th><label for="${params.buildTarget}">Target:</label></th>
    <td>
        <props:textProperty name="${params.buildTarget}" className="longField">
            <jsp:attribute name="afterTextField">
                <bs:projectData type="UnityBuildTarget" sourceFieldId="${params.projectPath}" selectionMode="single"
                                targetFieldId="${params.buildTarget}" popupTitle="Select build target"/>
            </jsp:attribute>
        </props:textProperty>
        <span class="error" id="error_${params.buildTarget}"></span>
        <span class="smallNote">Specify the active build target before loading a project.</span>
    </td>
</tr>

<tr>
    <th><label for="${params.buildPlayer}">Standalone player:</label></th>
    <td>
        <props:selectProperty name="${params.buildPlayer}" enableFilter="true" className="mediumField">
            <props:option value="">&lt;Select&gt;</props:option>
            <c:forEach var="item" items="${params.buildPlayers}">
                <props:option value="${item.first}"><c:out value="${item.second}"/></props:option>
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
        <span class="smallNote">Specify the output path for player binary.</span>
    </td>
</tr>

<tr class="advancedSetting">
    <th>Options:</th>
    <td>
        <props:checkboxProperty name="${params.runEditorTests}"/>
        <label for="${params.runEditorTests}">Run Editor tests from the project</label><br/>
        <props:checkboxProperty name="${params.noGraphics}"/>
        <label for="${params.noGraphics}">Do not initialize the graphics device</label>
    </td>
</tr>

<tr class="advancedSetting">
    <th><label for="${params.arguments}">Command line arguments: </label></th>
    <td>
        <props:textProperty name="${params.arguments}" className="longField"/>
        <span class="error" id="error_${params.arguments}"></span>
        <span class="smallNote">Specify the additional command line arguments for Unity<bs:help
                urlPrefix="https://docs.unity3d.com/Manual/CommandLineArguments.html" file=""/>.</span>
    </td>
</tr>

<tr class="advancedSetting">
    <th><label for="${params.unityVersion}">Unity version:</label></th>
    <td>
        <props:textProperty name="${params.unityVersion}" className="longField"/>
        <span class="error" id="error_${params.unityVersion}"></span>
        <span class="smallNote">Specify the required Unity version, e.g 2018.2.</span>
    </td>
</tr>

<script type="text/javascript">
    BS.UnityParametersForm.updateElements();
</script>
