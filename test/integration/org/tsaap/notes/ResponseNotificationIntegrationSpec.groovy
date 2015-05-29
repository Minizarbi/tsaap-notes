package org.tsaap.notes

import org.tsaap.BootstrapTestService
import org.tsaap.questions.ResponseNotificationService
import spock.lang.Specification

/**
 * Created by dylan on 27/05/15.
 */
class ResponseNotificationIntegrationSpec extends Specification {

    ResponseNotificationService responseNotificationService
    BootstrapTestService bootstrapTestService
    NoteService noteService

    def setup() {
        bootstrapTestService.initializeTests()
        bootstrapTestService.context1.refresh()
        bootstrapTestService.context2.refresh()
    }

    void "finding all responses to notify"() {
        given: "the user paul ask two questions"
        Tag tag = Tag.findOrSaveWhere(name: 'tagtest')
        Note note1 = noteService.addNote(bootstrapTestService.learnerPaul,"paul's question 1",bootstrapTestService.context1,tag)
        Note note2 = noteService.addNote(bootstrapTestService.learnerPaul,"paul's question 2",bootstrapTestService.context1,tag)

        and: "two other users answer to paul's questions"
        Note note3 = noteService.addNote(bootstrapTestService.learnerMary,"mary's response 1",bootstrapTestService.context1,tag,note1)
        Note note4 = noteService.addNote(bootstrapTestService.learnerMary,"mary's response 2",bootstrapTestService.context2,tag,note2)
        Note note5 = noteService.addNote(bootstrapTestService.teacherJeanne,"jeanne's response 1",bootstrapTestService.context1,tag,note1)

        when: "we want to know if someone in the five last minutes answer to an user question"
        Map result = responseNotificationService.findAllResponsesNotifications()

        then: "we got a map with 1 notifications for Paul's questions."
        result.containsKey([question_author: bootstrapTestService.learnerPaul.id, first_name: bootstrapTestService.learnerPaul.firstName, email: bootstrapTestService.learnerPaul.email])
        def res2 = result.get([question_author: bootstrapTestService.learnerPaul.id, first_name: bootstrapTestService.learnerPaul.firstName, email: bootstrapTestService.learnerPaul.email])
        res2.containsKey([context_id: bootstrapTestService.context1.id, context_name: bootstrapTestService.context1.contextName, fragment_tag: tag.id,
                          question_id: note1.id, question_content: "paul's question 1"])
        def res3 = res2.get([context_id: bootstrapTestService.context1.id, context_name: bootstrapTestService.context1.contextName, fragment_tag: tag.id,
                             question_id: note1.id, question_content: "paul's question 1"])
        res3.contains([response_author: bootstrapTestService.learnerMary.username, response_id: note3.id, response_content: "mary's response 1"])
    }
}