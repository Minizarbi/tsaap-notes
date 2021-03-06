%{--
  - Copyright (C) 2013-2016 Université Toulouse 3 Paul Sabatier
  -
  -     This program is free software: you can redistribute it and/or modify
  -     it under the terms of the GNU Affero General Public License as published by
  -     the Free Software Foundation, either version 3 of the License, or
  -     (at your option) any later version.
  -
  -     This program is distributed in the hope that it will be useful,
  -     but WITHOUT ANY WARRANTY; without even the implied warranty of
  -     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  -     GNU Affero General Public License for more details.
  -
  -     You should have received a copy of the GNU Affero General Public License
  -     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  --}%

<%--
  Created by IntelliJ IDEA.
  User: franck
  Date: 22/03/15
  Time: 09:02
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <g:if test="${params.inline && params.inline == 'on'}">
        <meta name="layout" content="inline"/>
    </g:if>
    <g:else>
        <meta name="layout" content="main"/>
    </g:else>
    <r:require modules="tsaap_ui_notes,tsaap_icons"/>
    <r:require module="export"/>
    <title>Tsaap-Notes result list</title>
</head>

<body>
<div class="container">
    <export:formats params="[id: liveSession.id]" controller="question" action="results"/>
    <hr/>

    <p>
        <strong>Scope</strong> <br/>${results.contextName}
    </p>

    <p>
        <strong>Question</strong> <br/>${results.question}
    </p>

    <p>
        <strong>Responses count</strong> <br/> ${results.resultList.size()}
    </p>
    <table class="table">
        <tr>
            <g:each in="${labels.keySet()}" var="propName">
                <th style="border: medium">${labels."$propName"}</th>
            </g:each>
        </tr>
        <g:each in="${results.resultList}" var="currentResult">
            <tr>
                <g:each in="${labels.keySet()}" var="propName">
                    <td>${currentResult."$propName"}</td>
                </g:each>
            </tr>
        </g:each>
    </table>
    <hr/>
</div>

</body>
</html>