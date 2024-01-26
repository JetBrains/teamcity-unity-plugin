<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>


<jsp:useBean id="params" class="jetbrains.buildServer.unity.UnityParametersProvider"/>
<jsp:useBean id="constants" class="jetbrains.buildServer.unity.UnityConstantsProvider"/>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>

<script type="text/javascript">
    var detectionModeId = BS.Util.escapeId('${params.detectionMode}');
    BS.UnityInstallationForm = {
        clearInputValues: function(row) {
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

        updateElements: function() {
            var $autoRow = $j(BS.Util.escapeId('unity-auto-settings'));
            var $manualRow = $j(BS.Util.escapeId('unity-manual-settings'));

            var detectionMode = $j(detectionModeId).val();
            if(detectionMode === undefined) {
                detectionMode = '';
            }

            switch(detectionMode) {
                case "auto":
                    $autoRow.show();
                    $manualRow.hide();
                    BS.UnityInstallationForm.clearInputValues($manualRow);
                    break;

                case "manual":
                    $autoRow.hide();
                    $manualRow.show();
                    BS.UnityInstallationForm.clearInputValues($autoRow);

                    break;

                default:
                    BS.UnityInstallationForm.clearInputValues($manualRow);
                    BS.UnityInstallationForm.clearInputValues($autoRow);

                    $autoRow.show();
                    $manualRow.hide();
                    break;
            }
            BS.MultilineProperties.updateVisible();
        }
    };

    $j(document).on('change', detectionModeId, function () {
        BS.UnityInstallationForm.updateElements();
    });

</script>

<l:settingsGroup title="Unity Installation" className="advancedSetting">
<tr class="advancedSetting">
  <th class="noBorder"><label for="${params.detectionMode}">Detection mode:</label></th>
  <td>
    <props:selectProperty name="${params.detectionMode}" enableFilter="true" className="mediumField">
      <c:forEach var="item" items="${params.detectionModeValues}">
        <props:option value="${item.id}" currValue="${propertiesBean.properties[params.detectionMode]}">
            <c:out value="${item.description}"/>
        </props:option>
      </c:forEach>
    </props:selectProperty>
    <span class="error" id="error_${params.detectionMode}"></span>
  </td>
</tr>
<tbody id="unity-auto-settings">
  <tr class="advancedSetting">
    <th><label for="${params.unityVersion}">Unity version:</label></th>
    <td>
        <props:textProperty name="${params.unityVersion}" className="longField disableBuildTypeParams"/>
        <span class="error" id="error_${params.unityVersion}"></span>
        <span class="smallNote" id="${params.unityVersion}-hint">
            Specify the required Unity version, e.g 2018.2.
            If left blank, the version specified in the project settings will be selected if available.
            Otherwise, the latest Unity installed on the agent will be chosen for the build.
        </span>
    </td>
  </tr>
</tbody>
<tbody id="unity-manual-settings">
    <tr class="advancedSetting">
        <th><label>Unity:<l:star/></label></th>
        <td>
            <jsp:include page="/tools/selector.html?toolType=${constants.unityToolName}&versionParameterName=${params.unityRoot}&class=longField"/>
        </td>
    </tr>
</tbody>
</l:settingsGroup>

<script type="text/javascript">
  BS.UnityInstallationForm.updateElements();
</script>