<%@ include file="/include-internal.jsp"%>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<jsp:useBean id="buildForm" type="jetbrains.buildServer.controllers.admin.projects.BuildTypeForm" scope="request"/>
<jsp:useBean id="params" class="jetbrains.buildServer.unity.UnityParametersProvider"/>

<style>
    .runnerFormTable tr td.withTopBorder,
    .runnerFormTable tr th.withTopBorder {
        border-top: 1px dotted #CCC;
    }
</style>

<script type="text/javascript">
    var activateLicenseId = BS.Util.escapeId('${params.activateLicense}');

    BS.UnityBuildFeatureParametersForm = {
        updateElements: function () {
            $j(".license").toggle($j(activateLicenseId).is(':checked'));
            BS.MultilineProperties.updateVisible();
        }
    };

    $j(document).on('change', activateLicenseId, function () {
        BS.UnityBuildFeatureParametersForm.updateElements();
    });
</script>

<tr>
  <td colspan="2">
    <em>This build feature specifies common options for Unity<bs:help urlPrefix="https://unity3d.com/" file=""/> build steps</em>
  </td>
</tr>

<tr>
    <th>License: <bs:help urlPrefix="https://docs.unity3d.com/Manual/ManagingYourUnityLicense.html" file=""/></th>
    <td>
        <props:checkboxProperty name="${params.activateLicense}"/>
        <label for="${params.activateLicense}">Activate Unity license on Editor startup</label>
    </td>
</tr>
</tbody>
<tbody class="license">
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
<tbody>

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

<tr class="advancedSetting">
    <th><label for="${params.unityVersion}">Unity version:</label></th>
    <td>
        <props:textProperty name="${params.unityVersion}" className="longField"/>
        <span class="error" id="error_${params.unityVersion}"></span>
        <span class="smallNote">Specify the required Unity version, e.g 2018.2.</span>
    </td>
</tr>

<script type="text/javascript">
    BS.UnityBuildFeatureParametersForm.updateElements();
</script>