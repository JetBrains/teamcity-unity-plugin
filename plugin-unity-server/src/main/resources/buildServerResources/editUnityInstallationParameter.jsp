<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<%--
  ~ Copyright 2020 Aaron Zurawski
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

<jsp:useBean id="params" class="jetbrains.buildServer.unity.UnityParametersProvider"/>
<jsp:useBean id="constants" class="jetbrains.buildServer.unity.UnityConstantsProvider"/>

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
            if(detectionMode == undefined) {
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
      <props:option value="">&lt;Default&gt;</props:option>
      <c:forEach var="item" items="${params.detectionModeValues}">
        <props:option value="${item.id}"><c:out value="${item.description}"/></props:option>
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
        <span class="smallNote" id="${params.unityVersion}-hint">Specify the required Unity version, e.g 2018.2.</span>
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