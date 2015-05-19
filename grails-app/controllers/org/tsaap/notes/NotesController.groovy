/*
 * Copyright 2013 Tsaap Development Group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tsaap.notes

import grails.plugins.springsecurity.Secured
import grails.plugins.springsecurity.SpringSecurityService
import org.tsaap.attachement.AttachementDto
import org.tsaap.attachement.AttachementService
import org.tsaap.directory.User
import org.tsaap.questions.Question
import org.tsaap.questions.impl.gift.GiftQuestionService


class NotesController {

    SpringSecurityService springSecurityService
    NoteService noteService
    GiftQuestionService giftQuestionService
    AttachementService attachementService

    private static final okcontents = ['image/png', 'image/jpeg', 'image/gif']

    /**
     *
     * @return
     */
    @Secured(['IS_AUTHENTICATED_REMEMBERED'])
    def index() {
        User user = springSecurityService.currentUser
        renderMainPage(params, user)
    }

    @Secured(['IS_AUTHENTICATED_REMEMBERED'])
    def addNote() {
        def user = springSecurityService.currentUser
        def noteContent = params.noteContent
        Context context = null
        if (params.contextId) {
            context = Context.get(params.contextId)
        }
        Tag fragmentTag = null
        if (params.fragmentTagId) {
            fragmentTag = Tag.get(params.fragmentTagId)
        } else if (params.fragmentTagName) {
            fragmentTag = Tag.findOrSaveWhere(name: params.fragmentTagName.toLowerCase())
        }
        Note parentNote = null
        if (params.parentNoteId) {
            parentNote = Note.get(params.parentNoteId)
            if (!context) {
                context = parentNote.context
            }
            if (!fragmentTag) {
                fragmentTag = parentNote.fragmentTag
            }
        }

        Attachement attachementNote = null
        def file = request.getFile('myFile')
        String nom_image = file.getOriginalFilename()
        boolean image = false
        if (okcontents.contains(file.getContentType())) {
            attachementNote = attachementService.createAttachementForMultipartFile(file)
            image = true
        }

        Note myNote = noteService.addNote(user, noteContent, context, fragmentTag, parentNote)
        if(image){
            if(context != null){
                attachementNote = attachementService.addNoteAndContextToAttachement(context,myNote,attachementNote)
            }
            else {
                attachementNote = attachementService.addNoteToAttachement(myNote,attachementNote)
            }
        }

        params.remove('noteContent')
        redirect(action: index(), params: params)
    }

    @Secured(['IS_AUTHENTICATED_REMEMBERED'])
    def bookmarkNote() {
        def user = springSecurityService.currentUser
        Note note = Note.get(params.noteId)
        noteService.bookmarkNotebyUser(note, user)
        params.remove('noteId')
        redirect(action: index(), params: params)
    }

    @Secured(['IS_AUTHENTICATED_REMEMBERED'])
    def unbookmarkNote() {
        def user = springSecurityService.currentUser
        Note note = Note.get(params.noteId)
        noteService.unbookmarkedNoteByUser(note, user)
        params.remove('noteId')
        redirect(action: index(), params: params)
    }

    @Secured(['IS_AUTHENTICATED_REMEMBERED'])
    def markAsLikedNote() {
        def user = springSecurityService.currentUser
        Note note = Note.get(params.noteId)
        noteService.scoreNotebyUser(note, user)
        params.remove('noteId')
        redirect(action: index(), params: params)
    }

    @Secured(['IS_AUTHENTICATED_REMEMBERED'])
    def deleteNote() {
        def user = springSecurityService.currentUser
        Note note = Note.load(params.noteId)
        attachementService.deleteAttachementForNote(note)
        noteService.deleteNoteByAuthor(note, user)
        params.remove('noteId')
        redirect(action: index(), params: params)
    }

    @Secured(['IS_AUTHENTICATED_REMEMBERED'])
    def showDiscussion() {
        def user = springSecurityService.currentUser
        Long noteId = params.noteId as Long
        Map showDiscussion = [:]
        showDiscussion[noteId] = true
        params.remove('noteId')
        renderMainPage(params, user, showDiscussion)
    }

    @Secured(['IS_AUTHENTICATED_REMEMBERED'])
    def hideDiscussion() {
        def user = springSecurityService.currentUser
        Map showDiscussion = [:]
        renderMainPage(params, user, showDiscussion)
    }

    @Secured(['IS_AUTHENTICATED_REMEMBERED'])
    def evaluateContentAsNote() {
        String noteInput = params.content
        if (noteInput?.startsWith('::')) {
            try {
                Question question = giftQuestionService.getQuestionFromGiftText(noteInput)
                render(template: '/questions/preview/detail',model: [question: question])
            }catch (Exception e) {
                render("${e.message}")
            }
        } else {
          render(noteInput ?: '')
        }
    }

    /**
     * Render the main page given the params and the user
     * @param params the params
     * @param user the user
     * @return the result of the render operation
     */
    private def renderMainPage(def params, User user, Map showDiscussion = [:]) {
        Context context = null
        if (params.contextId && params.contextId != 'null') {
            context = Context.get(params.contextId)
        }
        Tag fragmentTag = null
        if (params.fragmentTagId && params.fragmentTagId != 'null') {
            fragmentTag = Tag.get(params.fragmentTagId)
        } else if (params.fragmentTagName && params.fragmentTagName != 'null') {
            fragmentTag = Tag.findOrSaveWhere(name: params.fragmentTagName.toLowerCase())
            if (fragmentTag != null) {
                params.fragmentTagId = fragmentTag.id
            }
        }
        def displaysMyNotes = true
        def displaysMyFavorites = false
        def displaysAll = false
        if (params.displaysMyNotes != 'on') {
            displaysMyNotes = false
        }
        if (params.displaysMyFavorites == 'on') {
            displaysMyFavorites = true
        }
        if (params.displaysAll == 'on') {
            displaysAll = true
        }
        params.max = Math.min(params.max as Long ?: 5, 20)
        def paginationAndSorting = [sort: 'dateCreated', order: 'desc', max: params.max]
        if (params.offset) {
            paginationAndSorting.offset = params.offset
        }
        def notes = noteService.findAllNotes(user,
                displaysMyNotes,
                displaysMyFavorites,
                displaysAll,
                context,
                fragmentTag,
                paginationAndSorting)
        render(view: '/notes/index', model: [user: user,
                notes: notes,
                context: context,
                fragmentTag: fragmentTag,
                showDiscussion: showDiscussion])

    }

}
