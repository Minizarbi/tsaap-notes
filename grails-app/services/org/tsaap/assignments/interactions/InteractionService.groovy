package org.tsaap.assignments.interactions

import grails.transaction.Transactional
import org.tsaap.assignments.ChoiceInteractionResponse
import org.tsaap.assignments.Interaction
import org.tsaap.assignments.PeerGrading
import org.tsaap.assignments.Sequence
import org.tsaap.assignments.StateType
import org.tsaap.contracts.Contract
import org.tsaap.directory.User

@Transactional
class InteractionService {

    /**
     * Start an interaction
     * @param interaction the interaction to start
     * @return the started interaction
     */

    Interaction startInteraction(Interaction interaction, User user) {
        Contract.requires(interaction.owner == user, ONLY_OWNER_CAN_START_INTERACTION)
        Sequence sequence = interaction.sequence
        interaction.state = StateType.show.name()
        sequence.state = StateType.show.name()
        interaction.save()
        sequence.save()
        interaction
    }

    /**
     * Stop an interaction
     * @param interaction the interaction to stop
     * @return the stopped interaction
     */
    Interaction stopInteraction(Interaction interaction, User user) {
        Contract.requires(interaction.owner == user,ONLY_OWNER_CAN_STOP_INTERACTION)
        Contract.requires(interaction.state == StateType.show.name(),INTERACTION_IS_NOT_STARTED)
        interaction.doAfterStop()
        updateActiveInteractionInSequence(interaction)
        interaction
    }

    /**
     * Save a choice interaction response
     * @param response the response to save
     * @return the saved response with the updated score
     */
    ChoiceInteractionResponse saveChoiceInteractionResponse(ChoiceInteractionResponse response) {
        Contract.requires(response.firstAttemptIsSubmitable() || response.secondAttemptIsSubmitable(), INTERACTION_CANNOT_RECEIVE_RESPONSE)
        Contract.requires(response.learner.isRegisteredInAssignment(response.assignment()),
                LEARNER_NOT_REGISTERED_IN_ASSIGNMENT)
        response.updateScore()
        response.save()
        response
    }

    /**
     * find or create peer grading from user on response
     * @param grader the grader
     * @param response the response
     * @return the peer grading object
     */
    PeerGrading peerGradingFromUserOnResponse(User grader, ChoiceInteractionResponse response, Float grade) {
        Contract.requires(grader.isRegisteredInAssignment(response.assignment()),LEARNER_NOT_REGISTERED_IN_ASSIGNMENT)
        PeerGrading peerGrading = PeerGrading.findByResponseAndGrader(response,grader)
        if (!peerGrading) {
            peerGrading = new PeerGrading(grader: grader, response: response, grade: grade)
            peerGrading.save()
        }
        peerGrading
    }

    /**
     * Update the mean grade of a given response
     * @param response the response
     * @return the response with mean grade updated
     */
    ChoiceInteractionResponse updateMeanGradeOfResponse(ChoiceInteractionResponse response) {
        def query = PeerGrading.where {
            response == response
        }.projections {
            avg('grade')
        }
        def meanGrade = query.find() as Float
        response.meanGrade = meanGrade
        response.save()
        response
    }

    private void updateActiveInteractionInSequence(Interaction interaction) {
        Sequence sequence = interaction.sequence
        for (Integer rank in interaction.rank + 1 .. sequence.interactions.size()) {
            Interaction newActInter = Interaction.findBySequenceAndRankAndEnabled(sequence, rank,true)
            if (newActInter) {
                sequence.activeInteraction = newActInter
                sequence.save()
                break
            }
        }
    }

    private static final String ONLY_OWNER_CAN_START_INTERACTION = 'Only owner can start an interaction'
    private static final String ONLY_OWNER_CAN_STOP_INTERACTION = 'Only owner can stop an interaction'
    private static final String INTERACTION_IS_NOT_STARTED = 'The interaction is not started'
    private static final String INTERACTION_CANNOT_RECEIVE_RESPONSE = 'The interaction cannot receive response'
    private static final String LEARNER_NOT_REGISTERED_IN_ASSIGNMENT = 'Learner is not registered in the relative assignment'
}
