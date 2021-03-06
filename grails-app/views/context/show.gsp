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

<%@ page import="org.tsaap.notes.Context" %>
<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main">
    <r:require modules="tsaap_ui_notes,tsaap_icons"/>

    <g:set var="entityName"
           value="${message(code: 'context.label', default: 'Scope')}"/>
    <title>Tsaap Notes - <g:message code="default.show.label"
                                    args="[entityName]"/></title>
</head>

<body>

<div class="container context-nav" role="navigation">
    <ol class="breadcrumb">
        <li><g:link class="list" action="index"><g:message code="default.list.label"
                                                           args="[entityName]"/></g:link></li>
        <li class="active">${message(code: 'context.show.li.active')} "${context?.contextName}"</li>
    </ol>
</div>

<div id="show-context" class="container" role="main">
    <g:if test="${flash.message}">
        <div class="alert alert-info" role="status">
            <a href="#" class="close" data-dismiss="alert" aria-label="close">&times;</a>
            ${flash.message}
        </div>
    </g:if>

    <g:if test="${context}">
        <table class="table table-bordered">
            <colgroup>
                <col class="col-lg-1">
                <col class="col-lg-7">
            </colgroup>
            <tbody>
            <tr>
                <td>
                    <g:message code="context.scopeName.label" default="Scope Name"/>
                </td>
                <td>${context.contextName}
                    <span class="label ${context.isOpen() ? 'label-info' : 'label-danger'}">
                        ${message(code: context.isOpen() ? 'context.scopeStatus.open' : 'context.scopeStatus.close')}
                    </span>
                </td>
            </tr>
            <tr>
                <td>
                    <g:message code="context.url.label" default="Url"/>
                </td>
                <td><a href="${context.url}" target="_blank">${context.url}</a></td>
            </tr>
            <tr>
                <td>
                    <g:message code="context.show.description.label"
                               default="Description"/>
                </td>
                <td>${context.descriptionAsNote}</td>
            </tr>
            <tr>
                <td>
                    <g:message code="context.show.source.label"
                               default="Description"/>
                </td>
                <td>${context.source}</td>
            </tr>
            <tr>
                <td>
                    <g:message code="context.show.owner.label" default="Owner"/>
                </td>
                <td>@${context.owner.username} <g:if
                        test="${context.ownerIsTeacher}">${message(code: 'context.show.teacher.label')}</g:if></td>
            </tr>
            <tr>
                <td>
                    <g:message code="context.show.dateCreated.label" default="Created"/>
                </td>
                <td><g:formatDate
                        date="${context.dateCreated}"/></td>
            </tr>
            <tr>
                <td>
                    <g:message code="context.show.lastUpdated.label" default="Updated"/>
                </td>
                <td><g:formatDate
                        date="${context.lastUpdated}"/></td>
            </tr>
            <g:if test="${context.noteTakingEnabled || context.hasStandardNotes()}">

                <tr>
                    <td>
                        ${message(code: 'context.show.notes')}
                    </td>
                    <td>
                        <g:link controller="notes"
                                params="[displaysAll: 'on', contextName: context?.contextName, contextId: context.id]"><g:createLink
                                absolute="true" controller="notes"
                                params="[displaysAll: 'on', contextName: context?.contextName, contextId: context.id]"/></g:link>

                    </td>
                </tr>
            </g:if>

            <tr>
                <td>
                    ${message(code: 'context.show.questions')}
                </td>
                <td><g:link controller="questions"
                            params="[displaysAll: 'on', contextName: context?.contextName, contextId: context.id]"><g:createLink
                            absolute="true" controller="questions"
                            params="[displaysAll: 'on', contextName: context?.contextName, contextId: context.id]"/></g:link></td>
            </tr>
            </tbody>
        </table>


        <g:if test="${context.owner == user}">
            <g:form url="[resource: context, action: 'delete']" method="DELETE">
                <fieldset class="buttons">

                    <g:link class="btn btn-primary" action="edit"
                            resource="${context}"><g:message
                            code="default.button.edit.label" default="Edit"/></g:link>
                    <g:actionSubmit class="btn btn-default" action="delete"
                                    value="${message(code: 'default.button.delete.label', default: 'Delete')}"
                                    onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');"/>
                    <g:link class="btn btn-primary" controller="context" action="exportQuestionsAsGift"
                            id="${context.id}" target="_blank">${message(code: 'context.show.export.link')}</g:link>
                    <g:link class="btn btn-primary" controller="context" action="exportQuestionsAsGiftWithFeedbacks"
                            id="${context.id}"
                            target="_blank">${message(code: 'context.show.exportFeedback.link')}</g:link>

                    <g:if test="${context.isOpen()}">
                        <g:link class="btn btn-primary" controller="context" action="close"
                                params="[id: context.id, show: 1]">${message(code: 'context.index.close.button')}</g:link>
                    </g:if>
                    <g:elseif test="${context.isClosed()}">
                        <g:link class="btn btn-primary" controller="context" action="open"
                                params="[id: context.id, show: 1]">${message(code: 'context.index.open.button')}</g:link>

                    </g:elseif>
                    <g:if test="${context.hasLinkedLtiActivity()}">
                        <g:link class="btn btn-primary" controller="context" action="syncGrades"
                                id="${context.id}">
                            <span class="glyphicon glyphicon-refresh">
                            <g:message code="default.button.synchronized.label" default="Synchroniser"/>
                        </g:link>
                    </g:if>
                </fieldset>
            </g:form>
        </g:if>
    </g:if>
</div>
</body>
</html>
