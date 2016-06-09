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

package org.tsaap.taglibs

import org.tsaap.attachement.Attachement
import org.tsaap.attachement.AttachementService
import org.tsaap.attachement.Dimension

/**
 * Created by dylan on 19/05/15.
 */
class AttachementTagLib {

    AttachementService attachementService

    static namespace = "tsaap"

/**
 * Affiche un fichier.
 *
 * @attr attachement REQUIRED the attachement to show
 */
    def viewAttachement = { attrs ->
        if (attrs.attachement) {
            Attachement attachement = attrs.attachement
            String link = g.createLink(action: 'viewAttachement',
                    controller: 'attachement',
                    id: attachement.id)
            if (attachement.imageIsDisplayable()) {

                out << '<img src="' << link << '"'
                if (attrs.width && attrs.height) {
                    def dimInitial = new Dimension(width: attrs.width, height: attrs.height)
                    def dimensionRendu = attachement.calculateDisplayDimension(dimInitial)
                    out << ' width="' << dimensionRendu.width << '"'
                    out << ' height="' << dimensionRendu.height << '"'
                }
                if (attrs.id) {
                    out << ' id="' << attrs.id << '"'
                }
                if (attrs.class) {
                    out << ' class="' << attrs.class << '"'
                }
                out << '/>'
            } else if (attachement.textIsDisplayable()) {
                def is = attachementService.getInputStreamForAttachement(attachement)
                out << '<span style="white-space: pre;">' << is.text.encodeAsHTML() << '</span>'
            } else {
                out << '<a target="_blank" href="' << link << '">' <<
                        attachement.originalName <<
                        '</a>'
            }
        }
    }
}