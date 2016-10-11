<g:set var="responseInteraction" value="${interactionInstance.sequence.responseSubmissionInteraction}"/>
<p>
<div class="well well-sm">${message(code:'player.sequence.interaction.secondAttemptSubmissionCount',args:[])} <span id="second_attempt_count_${responseInteraction.id}">${responseInteraction.choiceInteractionResponseCount(2)}</span> <g:remoteLink controller="player" action="updateSecondAttemptCount" id="${responseInteraction.id}" title="Refresh" update="second_attempt_count_${responseInteraction.id}"><span class="glyphicon glyphicon-refresh" aria-hidden="true"></span></g:remoteLink></div>
<g:remoteLink class="btn-group btn-success" controller="player" action="stopInteraction" id="${interactionInstance.id}" update="sequence_${interactionInstance.sequenceId}"><span class="glyphicon glyphicon-play"></span> ${message(code: "player.sequence.interaction.stop",args:[interactionInstance.rank])}</g:remoteLink>
</p>