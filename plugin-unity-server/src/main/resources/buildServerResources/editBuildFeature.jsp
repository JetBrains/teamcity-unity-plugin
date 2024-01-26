<%@ include file="/include-internal.jsp"%>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>


<jsp:useBean id="buildForm" type="jetbrains.buildServer.controllers.admin.projects.BuildTypeForm" scope="request"/>
<jsp:useBean id="params" class="jetbrains.buildServer.unity.UnityParametersProvider"/>

<style>
    .runnerFormTable tr td.withTopBorder,
    .runnerFormTable tr th.withTopBorder {
        border-top: 1px dotted #CCC;
    }
    .invisibleUpload input[type='file'] {
        color: transparent;
    }
</style>

<script type="text/javascript">
    const unityLicenseType = BS.Util.escapeId('${params.unityLicenseType}');

    BS.UnityBuildFeatureParametersForm = {
        clearInputValues: function($licenseDetails) {
            $licenseDetails.find(':input').each(function (id, element) {
                const $element = $j(element);
                const name = $element.attr("name");
                if (!name || name.indexOf("prop:") !== 0) {
                    return;
                }

                let changed;
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

        updateLicenseUploadNote: function () {
            const licenseContent = $('${params.unityPersonalLicenseContent}').value
            const note = $j(BS.Util.escapeId('unityLicenseUploadNote'));

            if (licenseContent) {
                note.text("License has been successfully uploaded.");
            } else {
                note.text("Please upload the .ulf file. Note that it can only be used on the machine where the corresponding .alf file was generated. See the Unity documentation for details.");
            }
        },

        validateLicenseFile: function ($file) {
            const allowedExtensions = ['ulf'], sizeLimit = 100_000; // 100 kilobytes
            const {name: fileName, size: fileSize} = $file;
            const fileExtension = fileName.split(".").pop();
            if (!allowedExtensions.includes(fileExtension)) {
                alert("File should have .ulf extension");
                return false;
            } else if (fileSize > sizeLimit) {
                alert("File is too large");
                return false;
            }
            return true;
        },

        updateElements: function () {
            const $professionalLicense = $j(BS.Util.escapeId('professionalLicense'));
            const $personalLicense = $j(BS.Util.escapeId('personalLicense'));

            switch ($j(unityLicenseType).val()) {
                case "professionalLicense":
                    $professionalLicense.show();
                    $personalLicense.hide();
                    this.clearInputValues($personalLicense);
                    break;

                case "personalLicense":
                    $personalLicense.show();
                    $professionalLicense.hide();
                    this.clearInputValues($professionalLicense);
                    break;

                default:
                    $professionalLicense.hide();
                    $personalLicense.hide();
                    this.clearInputValues($professionalLicense);
                    this.clearInputValues($personalLicense);
                    break;
            }

            this.updateLicenseUploadNote();
            BS.MultilineProperties.updateVisible();
        }
    };

    $j('#file\\:unityLicenseFile').on('change', function (event) {
        const input = event.target;
        if ('files' in input && input.files.length > 0) {
            const file = input.files[0];
            if (BS.UnityBuildFeatureParametersForm.validateLicenseFile(file)) {
                const reader = new FileReader();
                reader.onload = event => {
                    $('${params.unityPersonalLicenseContent}').value = event.target.result;
                    BS.UnityBuildFeatureParametersForm.updateLicenseUploadNote();
                };
                reader.onerror = error => BS.UnityBuildFeatureParametersForm.showError('unity_license_upload', error);
                reader.readAsText(file);
            } else {
                file.value = null;
                $('${params.unityPersonalLicenseContent}').value = null;
                BS.UnityBuildFeatureParametersForm.updateLicenseUploadNote();
            }
        }
    });

    $j(document).on('change', unityLicenseType, function () {
        BS.UnityBuildFeatureParametersForm.updateElements();
    });
</script>

<tr>
  <td colspan="2">
    <em>This build feature specifies common options for Unity<bs:help urlPrefix="https://unity3d.com/" file=""/> build steps</em>
  </td>
</tr>

<tr class="unityLicenseType">
    <th>
        <label for="${params.unityLicenseType}">License: <bs:help
                urlPrefix="https://docs.unity3d.com/Manual/ManagingYourUnityLicense.html" file=""/></label>
    </th>
    <td>
        <props:selectProperty name="${params.unityLicenseType}" enableFilter="true" className="mediumField">
            <props:option value="">&lt;Select&gt;</props:option>
            <c:forEach var="item" items="${params.unityLicenseTypes}">
                <props:option value="${item.id}"><c:out value="${item.displayName}"/></props:option>
            </c:forEach>
        </props:selectProperty>
        <span class="error" id="error_${params.unityLicenseType}"></span>
        <span class="smallNote">Specify the type of Unity license.</span>
    </td>
</tr>
<tbody id="professionalLicense">
<tr>
    <th class="noBorder"><label for="${params.serialNumber}">Serial number:</label></th>
    <td>
        <props:passwordProperty name="${params.serialNumber}" className="longField" />
        <span class="error" id="error_${params.serialNumber}"></span>
    </td>
</tr>
<tr>
    <th class="noBorder"><label for="${params.username}">Username:</label></th>
    <td>
        <props:textProperty name="${params.username}" className="longField" />
        <span class="error" id="error_${params.username}"></span>
    </td>
</tr>
<tr>
    <th class="noBorder"><label for="${params.password}">Password:</label></th>
    <td>
        <props:passwordProperty name="${params.password}" className="longField" />
        <span class="error" id="error_${params.password}"></span>
    </td>
</tr>
</tbody>

<tr id="personalLicense">
    <th class="noBorder"><label for="${params.unityPersonalLicenseContent}">Upload license:</label></th>
    <td>
        <div class="posRel invisibleUpload">
            <forms:file name="unityLicenseFile" attributes="accept=\".ulf\""/>
            <span id="uploadError" class="error hidden"></span>
            <props:hiddenProperty name="${params.unityPersonalLicenseContent}"/>
            <span id="unityLicenseUploadNote" class="smallNote"></span>
        </div>
    </td>
</tr>

<tr>
  <th class="withTopBorder">
      <label for="${params.cacheServer}">
          Cache server address: <bs:help urlPrefix="https://docs.unity3d.com/Manual/CacheServer.html" file=""/>
      </label>
  </th>
  <td class="withTopBorder">
    <div class="posRel">
      <props:textProperty name="${params.cacheServer}" className="longField"/>
    </div>
    <span class="error" id="error_${params.cacheServer}"></span>
    <span class="smallNote">Specify Unity cache server address in format &lt;host:port&gt;.</span>
  </td>
</tr>

<jsp:include page="${teamcityPluginResourcesPath}/editUnityInstallationParameter.jsp"/>

<script type="text/javascript">
    BS.UnityBuildFeatureParametersForm.updateElements();
</script>