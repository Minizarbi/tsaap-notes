<%@ page import="org.springframework.web.servlet.support.RequestContextUtils" %>
<g:set var="sequence" value="${interactionInstance.sequence}"/>
<g:set var="responseInteractionInstance" value="${sequence.responseSubmissionInteraction}"/>
<g:set var="responsesToGrade" value="${sequence.findRecommendedResponsesForUser(user)}"/>
<g:if test="${!responseInteractionInstance.hasResponseForUser(user, 2)}">
    <g:if test="${responsesToGrade}">
    <div class="alert alert-info">${message(code: 'player.sequence.interaction.evaluation.intro')}</div>
    </g:if>
    <g:else>
        <div class="alert alert-info">${message(code: 'player.sequence.interaction.evaluation.intro.noresponsestograde')}</div>
    </g:else>
    <ul class="list-group">
            <g:each in="${responsesToGrade}" var="currentResponse" status="i">
            <g:if test="${i < interactionInstance.interactionSpecification.responseToEvaluateCount}">
            <li class="list-group-item">
                <p>
                    <strong>${message(code: 'player.sequence.interaction.choice.label')} ${currentResponse.choiceList()}</strong>
                    <br/>
                    ${raw(currentResponse.explanation)}
                    <input id="grade_${currentResponse.id}" name="grade_${currentResponse.id}" class="rating">
                </p>
            </li>
            <r:script>
            $("#grade_${currentResponse.id}").rating({
                size: "xs",
                step: 1,
                showCaption: false,
                showClear: false,
                language: "${RequestContextUtils.getLocale(request).language}"
            });
            $('#grade_${currentResponse.id}').on('rating.change', function(event, value, caption) {
                 $.ajax({
                    type: "GET",
                    url: "${createLink(action: 'createOrUpdatePeerGrading', controller: 'player')}",
                    data: {grader_id:${user.id},response_id:${currentResponse.id},grade:value}
                }).done(function(data) {
                    $('#grade_${currentResponse.id}').rating('refresh', {disabled: true});
                });
            });
            </r:script>
            </g:if>
        </g:each>
    </ul>
</g:if>
<g:render template="/assignment/player/ResponseSubmission/learner/show"
          model="[user: user, interactionInstance: responseInteractionInstance, attempt: 2]"/>
