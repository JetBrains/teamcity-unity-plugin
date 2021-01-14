<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
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

<c:if test="${not empty propertiesBean.properties[params.buildTarget]}">
    <div class="parameter">
        Build target: <props:displayValue name="${params.buildTarget}"/>
    </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[params.buildPlayer]}">
    <div class="parameter">
        <c:forEach items="${params.buildPlayers}" var="type">
            <c:if test="${propertiesBean.properties[params.buildPlayer] eq type.id}">
                Standalone player: <strong><c:out value="${type.description}"/></strong>
            </c:if>
        </c:forEach><br/>
        Player output path: <props:displayValue name="${params.buildPlayerPath}"/>
    </div>
</c:if>

<c:if test="${propertiesBean.properties[params.noGraphics]}">
    <div class="parameter">
        Do not initialize the graphics device: <strong>ON</strong>
    </div>
</c:if>

<c:if test="${propertiesBean.properties[params.silentCrashes]}">
    <div class="parameter">
        Do not display the error dialog when a standalone player crashes: <strong>ON</strong>
    </div>
</c:if>

<c:if test="${propertiesBean.properties[params.runEditorTests]}">
    <div class="parameter">
        Run Editor tests from the project: <strong>ON</strong>
    </div>

    <c:if test="${not empty propertiesBean.properties[params.testPlatform]}">
        <div class="parameter">
            Test platform: <props:displayValue name="${params.testPlatform}"/>
        </div>
    </c:if>

    <c:if test="${not empty propertiesBean.properties[params.testCategories]}">
        <div class="parameter">
            Test categories: <props:displayValue name="${params.testCategories}"/>
        </div>
    </c:if>

    <c:if test="${not empty propertiesBean.properties[params.testNames]}">
        <div class="parameter">
            Test names: <props:displayValue name="${params.testNames}"/>
        </div>
    </c:if>
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

<c:if test="${not empty propertiesBean.properties[params.lineStatusesFile]}">
    <div class="parameter">
        Line statuses file: <props:displayValue name="${params.lineStatusesFile}"/>
    </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[params.logFilePath]}">
    <div class="parameter">
        Log file path: <props:displayValue name="${params.logFilePath}"/>
    </div>
</c:if>

<c:if test="${not empty propertiesBean.properties[params.verbosity]}">
    <div class="parameter">
        <c:forEach items="${params.verbosityValues}" var="type">
            <c:if test="${propertiesBean.properties[params.verbosity] eq type.id}">
                Logging verbosity: <strong><c:out value="${type.description}"/></strong>
            </c:if>
        </c:forEach>
    </div>
</c:if>
