/*
 * Copyright (C) 2013-2016 Université Toulouse 3 Paul Sabatier
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.tsaap.questions

import grails.transaction.Transactional
import org.gcontracts.annotations.Requires
import org.hibernate.SQLQuery
import org.hibernate.Session
import org.hibernate.SessionFactory
import org.tsaap.directory.User

@Transactional
class ResultListService {

    SessionFactory sessionFactory

    /**
     * Get result list on a live session
     * @param user the user asking results
     * @param liveSession the live session
     * @return the built NPhasesLiveSessionResultList
     */
    @Requires({
        liveSession?.note?.context?.owner == user
    })
    NPhasesLiveSessionResultList getNPhasesLiveSessionResultListForLiveSession(User user, LiveSession liveSession) {
        NPhasesLiveSessionResultList res = new NPhasesLiveSessionResultList(
                liveSessionId: liveSession.id,
                contextId: liveSession?.note?.contextId,
                contextName: liveSession?.note?.context?.contextName,
                liveSessionStartDate: liveSession.startDate,
                liveSessionEndDate: liveSession.endDate,
                questionId: liveSession.noteId,
                question: liveSession.note?.content,
                title: liveSession.note?.content,
                id: liveSession.id
        )
        // the stats
        Session currentSession = sessionFactory.currentSession

        SessionPhase phase1 = liveSession.findFirstPhase()
        SessionPhase phase2 = liveSession.findSecondPhase()

        //
        // Results
        //

        String query = '''
        select
        last_name, first_name, username, lti_user_id, lti_consumer_key,
        response_1.answer_list_as_string as first_answer, note_1.content as first_explanation,
        response_1.confidence_degree as first_confidence_degree, response_1.`percent_credit` as first_score
        from
        `user`
        left join (`live_session_response` as response_1 left join `note` as note_1 on response_1.`explanation_id` = note_1.id) on  user.`id` = response_1.`user_id`
        left join `lms_user` on user.`id` = `lms_user`.`tsaap_user_id`
        where
        response_1.`session_phase_id` = :phaseId
        order by
        user.last_name, user.first_name
        '''


        SQLQuery sqlQuery = currentSession.createSQLQuery(query)
        List<Object[]> raws = sqlQuery.with {
            setLong('phaseId', phase1.id)
            list()
        }

        // build a map with results
        def resultsByUsername = new LinkedHashMap<String, NPhasesLiveSessionResult>()

        raws.each { raw ->
            def result = new NPhasesLiveSessionResult()
            result.lastName = raw[0]
            result.firstName = raw[1]
            result.username = raw[2]
            result.id = raw[2]
            result.ltiUserId = raw[3]
            result.ltiConsumerKey = raw[4]
            result.firstAnswer = raw[5]
            result.firstExplanation = raw[6]
            result.firstConfidenceDegree = raw[7]
            result.firstScore = raw[8] as Float
            resultsByUsername.put(result.username, result)
        }

        raws = sqlQuery.with {
            setLong('phaseId', phase2.id)
            list()
        }

        raws.each { raw ->
            def result = resultsByUsername.get(raw[2])
            if (result) {
                result.secondAnswer = raw[5]
                result.secondExplanation = raw[6]
                result.secondConfidenceDegree = raw[7]
                result.secondScore = raw[8] as Float
            }
        }

        res.resultList = resultsByUsername.values() as List<NPhasesLiveSessionResult>
        //
        // return res

        res

    }

    Map nPhaseSessionResultListLabels() {
        [
                lastName              : "Last_name",
                firstName             : "First_name",
                username              : "User_name",
                ltiUserId             : "Lti_user_id",
                ltiConsumerKey        : "Lti_consumer_key",

                firstAnswer           : "First_answer",
                firstExplanation      : "First_explanation",
                firstConfidenceDegree : "First_confidence_degree",
                firstScore            : "First_score",

                secondAnswer          : "Second_answer",
                secondExplanation     : "Second_explanation",
                secondConfidenceDegree: "Second_confidence_degree",
                secondScore           : "Second_score"
        ]
    }
}
