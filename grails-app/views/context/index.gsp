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

<%@ page import="org.tsaap.notes.FilterReservedValue; org.tsaap.notes.Context" %>
<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main">
    <r:require modules="tsaap_ui_notes,tsaap_icons"/>
    <g:set var="entityName"
           value="${message(code: 'context.label', default: 'Scope')}"/>
    <title>Tsaap Notes - <g:message code="default.list.label"
                                    args="[entityName]"/></title>
</head>

<body>
<div class="container">
    <ol class="breadcrumb">
        <li class="active"><g:message code="default.list.label"
                                      args="[entityName]"/></li>
    </ol>
    <g:link class="btn btn-primary btn-sm pull-right" action="create"><span
            class="glyphicon glyphicon-plus"></span>${message(code: 'context.index.addScope.button')}</g:link>
    <g:form controller="context"  action="index" method="post" role="form">
        <div class="row">
            <div class="col-lg-6">
                <div class="input-group">
                    <input type="text" class="form-control"
                           placeholder="${message(code: 'context.index.filter.placeholder')}"
                           name="filter"
                           value="${!(params.filter in org.tsaap.notes.FilterReservedValue.values()*.name()) ? params.filter : ''}">
                    <span class="input-group-btn">
                        <button class="btn btn-default"
                                type="submit">${message(code: 'context.index.filter.button')}</button>
                    </span>
                </div>
            </div>
        </div>

    </g:form>
    <small>${message(code: 'context.index.scopeCount')} ${contextCount}</small>
</div>

<div class="container">

    <ul class="nav nav-pills" style="margin-top: 10px;">
        <li role="presentation"><g:link
                class="list-group-item ${params.filter == org.tsaap.notes.FilterReservedValue.__MINE__.name() ? 'active' : ''}"
                controller="context" action="index" params="[filter: FilterReservedValue.__MINE__.name()]">
            ${message(code: 'context.index.myScope.link')}
        </g:link></li>
        <li role="presentation"><g:link
                class="list-group-item ${params.filter == org.tsaap.notes.FilterReservedValue.__FOLLOWED__.name() ? 'active' : ''}"
                controller="context" action="index" params="[filter: FilterReservedValue.__FOLLOWED__.name()]">
            ${message(code: 'context.index.followedScope.link')}
        </g:link></li>
        <li role="presentation"><g:link
                class="list-group-item ${(!params.filter || params.filter == org.tsaap.notes.FilterReservedValue.__ALL__.name()) ? 'active' : ''}"
                controller="context" action="index">
            ${message(code: 'context.index.allScope.link')}
        </g:link></li>
    </ul>

    <div id="list-context" class="container">
        <g:if test="${flash.message}">
            <div class="alert alert-info" role="status">${flash.message}</div>
        </g:if>

        <table class="table table-striped table-hover">
            <thead>
            <tr>

                <th>&nbsp;</th>


                <th>&nbsp;</th>
                <th>&nbsp;</th>

            </tr>
            </thead>
            <tbody>
            <g:each in="${contextList}" status="i" var="context">
                <tr>

                    <td>
                        <strong>
                            <g:set var="linkController" value="${context.noteTakingEnabled ? 'notes' : 'questions'}"/>
                            <g:link controller="$linkController"
                                    params="[displaysAll: 'on', contextName: context.contextName, contextId: context.id]">${fieldValue(bean: context, field: "contextName")}
                            </g:link>
                        </strong>
                        <g:if test="${context.isClosed()}">
                            <span class="label label-danger">
                                ${message(code: 'context.scopeStatus.close')}
                            </span>&nbsp;
                        </g:if>
                        <small>
                            @${context.owner}
                        </small>
                        <g:if test="${context.url}">
                            <br/>
                            <small>${fieldValue(bean: context, field: "url")}&nbsp<a
                                    href="${context?.url}"><span
                                        class="glyphicon glyphicon-link"></span></a></small>
                        </g:if>
                        <p>${fieldValue(bean: context, field: "descriptionAsNote")}</p>

                    </td>

                    <g:if test="${context.owner != user}">
                        <g:if test="${context.isFollowedByUser(user)}">
                            <td><g:link controller="context" action="unfollowContext" id="${context.id}"
                                        style="width: 90px;"
                                        class="btn btn-info btn-xs"
                                        onmouseover="updateFollowLink(\$(this),'Unfollow','btn-danger')"
                                        onmouseout="updateFollowLink(\$(this),'Following','btn-info')"
                                        params="${[filter: params.filter ?: '']}">${message(code: 'context.index.following.button')}</g:link></td>
                        </g:if>
                        <g:else>
                            <td>
                                <g:link controller="context" action="followContext" style="width: 90px;"
                                        class="btn btn-default btn-xs" id="${context.id}"
                                        params="${[filter: params.filter ?: '']}">${message(code: 'context.index.follow.button')}</g:link>
                            </td>
                        </g:else>
                    </g:if>
                    <g:else>
                        <td>
                            <div class="btn-group">
                                <button type="button" class="btn btn-default btn-xs dropdown-toggle"
                                        data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                                    <span class="glyphicon glyphicon-cog"></span> <span class="caret"></span>
                                </button>
                                <ul class="dropdown-menu">
                                    <li>
                                        <g:link action="show"
                                                id="${context.id}">${message(code: 'context.index.properties.button')}</g:link>
                                    </li>
                                    <li>
                                        <g:link action="statistics" controller="context" id="${context.id}">
                                            ${message(code: 'context.index.generateStats.button')}
                                        </g:link>
                                    </li>
                                    <li>
                                        <g:form action="duplicate" controller="context" name="${context.id}"
                                                id="${context.id}">
                                        </g:form>
                                        <a onclick="submitDuplicate(${context.id})">${message(code: 'context.index.duplicate.button')}</a>
                                    </li>
                                    <li>
                                        <g:if test="${context.isOpen()}">
                                            <g:link action="close" controller="context"
                                                    params="[id: context.id, filter: params.filter]">
                                                ${message(code: 'context.index.close.button')}
                                            </g:link>
                                        </g:if>
                                        <g:else>
                                            <g:link action="open" controller="context"
                                                    params="[id: context.id, filter: params.filter]">
                                                ${message(code: 'context.index.open.button')}
                                            </g:link>
                                        </g:else>

                                    </li>
                                    <li>
                                        <g:form action="delete" controller="context" name="delete${context.id}"
                                                id="${context.id}" resource="${context}" method="DELETE">
                                        </g:form>
                                        <a onclick="if (confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}'))
                                                ${'delete' + context.id}.
                                        submit();">${message(code: 'default.button.delete.label')}</a>
                                    </li>
                                </ul>
                            </div>
                        </td>
                    </g:else>
                </tr>
            </g:each>
            </tbody>
        </table>

        <div class="note-list-pagination">
            <tsaap:paginate class="pull-right" prev="&laquo;" next="&raquo;" total="${contextCount ?: 0}"
                            params="${[filter: params.filter ?: '']}"/>
        </div>
    </div>

</div>


<r:script>
    $(".nav li").removeClass('active');
    $("#mainLinkContexts").addClass('active');

    function updateFollowLink(followLink, text, classBtn) {
        var finalText ="";
        if(text == "Unfollow"){
           finalText  = "${message(code: 'context.index.unfollow.button')}"
        }
        else {
            finalText = "${message(code: 'context.index.following.button')}"
        }
        $(followLink).text(finalText);
        $(followLink).removeClass();
        $(followLink).addClass("btn btn-xs " + classBtn);
    }

    function submitDuplicate(contextId) {
        $("#"+contextId).submit();
    }
</r:script>
</body>
</html>
