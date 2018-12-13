<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="params" class="jetbrains.buildServer.unity.UnityParametersProvider"/>
<jsp:useBean id="teamcityPluginResourcesPath" scope="request" type="java.lang.String"/>

<c:if test="${not empty propertiesBean.properties[params.projectPath]}">
    <div class="parameter">
        Project path: <props:displayValue name="${params.projectPath}"/>
    </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[params.executeMethod]}">
    <div class="parameter">
        Execute method: <props:displayValue name="${params.executeMethod}"/>
    </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[params.arguments]}">
    <div class="parameter">
        Command line arguments: <props:displayValue name="${params.arguments}"/>
    </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[params.unityVersion]}">
    <div class="parameter">
        Unity version: <props:displayValue name="${params.unityVersion}"/>
    </div>
</c:if>
