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

package org.tsaap.lti

import groovy.sql.Sql

/**
 * Created by dorian on 29/06/15.
 */
class LmsContextHelper {

    /**
     * find if an lsm_context exist for given lti instance id and lti context id
     * @param ltiCourseId lti course id
     * @param ltiActivityId lti activity id
     * @return a tsaap context id if the lms context exist else null
     */
    Long selectLmsContextId(Sql sql, String consumerKey, String ltiCourseId, String ltiActivityId) {
        def req = sql.firstRow("SELECT tsaap_context_id FROM lms_context WHERE lti_consumer_key = $consumerKey and lti_activity_id = $ltiActivityId and lti_course_id = $ltiCourseId")
        def res = null
        if (req != null) {
            res = req.tsaap_context_id
        }
        res
    }

    /**
     * Insert a new context in database
     * @param sql
     * @param lmsContext the lms context to insert
     * @return the inserted lms context
     */
    LmsContext insertContext(Sql sql, LmsContext lmsContext) {
        sql.execute("INSERT INTO context (context_name, date_created, description_as_note, last_updated, owner_id, owner_is_teacher, url, source, note_taking_enabled, closed, removed) VALUES ($lmsContext.contextTitle,now(),null,now(),$lmsContext.owner.userId,${!lmsContext.owner.isLearner},null,$lmsContext.ltiConsumerName,$lmsContext.noteTakingEnabled,0,0)")
        lmsContext.contextId = selectContextId(sql, lmsContext.contextTitle, lmsContext.ltiConsumerName)
        lmsContext
    }

    /**
     * Select a context Id for a given contextName and source
     * @param sql
     * @param contextName tsaap context name
     * @param source lti context source
     * @return res the context id
     */
    Long selectContextId(Sql sql, String contextName, String source) {
        def req = sql.firstRow("SELECT id FROM context WHERE context_name = $contextName and source = $source")
        Long res = req.id
        res
    }

    /**
     * Select a context name for a given context id
     * @param sql
     * @param tsaapContextId context id
     * @return res context name
     */
    String selectContextName(Sql sql, Long tsaapContextId) {
        def req = sql.firstRow("SELECT context_name FROM context WHERE id = $tsaapContextId")
        def res = req.context_name
        res
    }

    /**
     * Insert a new Lms context in database
     * @param sql
     * @param lmsContext the lms context to insert
     */
    def insertLmsContext(Sql sql, LmsContext lmsContext) {
        sql.execute("INSERT INTO lms_context VALUES ($lmsContext.contextId,$lmsContext.ltiCourseId,$lmsContext.ltiActivityId,$lmsContext.ltiConsumerKey,$lmsContext.ltiConsumerName)")
    }

    /**
     * Select a lms context for a given tsaap context id
     * @param sql
     * @param contextId tsaap context id
     */
    def selectLmsContextForContextId(Sql sql, Long contextId) {
        def res = sql.firstRow("SELECT tsaap_context_id FROM lms_context WHERE tsaap_context_id = $contextId")
        res
    }

    /**
     * Delete a lms context for a given tsaap context id
     * @param sql
     * @param contextId tsaap context id
     */
    def deleteLmsContext(Sql sql, long contextId) {
        sql.execute("DELETE FROM lms_context WHERE tsaap_context_id = $contextId")
    }

    /**
     * Select a consumer key and lti course id for a given context id
     * @param sql
     * @param contextId context id
     * @return res an array with consumer key and lti course id
     */
    def selectConsumerKeyAndCourseId(Sql sql, long contextId) {
        def req = sql.firstRow("SELECT lti_consumer_key,lti_course_id from lms_context WHERE tsaap_context_id = $contextId")
        def res = [req.lti_consumer_key, req.lti_course_id]
        res
    }

    /**
     * Add a given user to followers of a given context if this one isn't already a follower for this context
     * @param sql
     * @param userId user id
     * @param contextId context id
     */
    def addUserToContextFollower(Sql sql, long userId, long contextId) {
        sql.execute("INSERT INTO context_follower (context_id, date_created, follower_id, follower_is_teacher, is_no_more_subscribed, unsusbscription_date) VALUES ($contextId,now(),$userId,0,0,null)")
    }

    /**
     *
     * @param sql
     * @param userId user id
     * @param contextId context id
     * @return res true if user is already a follower else false
     */
    def checkIfUserIsAContextFollower(Sql sql, long userId, long contextId) {
        def req = sql.firstRow("SELECT id FROM context_follower WHERE context_id = $contextId and follower_id = $userId")
        def res = false
        if (req != null) {
            res = true
        }
        res
    }

    /**
     * Check the existance of a local context
     * @param sql
     * @param lmsContext
     * @return true if the context exists
     */
    boolean contextExists(Sql sql, LmsContext lmsContext) {
        def req = sql.firstRow("select count(*) as count_context FROM context WHERE id = $lmsContext.contextId")
        def res = req.count_context == 1 ? true : false
        res
    }

    /**
     * Check existence of a local lms context
     * @param sql
     * @param lmsContext
     * @return true if the local lms context exists
     */
    boolean lmsContextExists(Sql sql, LmsContext lmsContext) {
        def req = sql.firstRow("select count(*) as count_context FROM lms_context WHERE tsaap_context_id = $lmsContext.contextId and lti_course_id = $lmsContext.ltiCourseId and lti_activity_id = $lmsContext.ltiActivityId and lti_consumer_key = $lmsContext.ltiConsumerKey")
        def res = req.count_context == 1 ? true : false
        res
    }
}
