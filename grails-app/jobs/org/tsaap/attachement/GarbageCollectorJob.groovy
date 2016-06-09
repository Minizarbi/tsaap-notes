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

package org.tsaap.attachement

/**
 * Created by dylan on 03/06/15.
 */
class GarbageCollectorJob {

    AttachementService attachementService

    static triggers = {
        cron name: 'garbageCronTrigger', startDelay: 10000, cronExpression: '0 0 4 * * ?'
    }

    def execute() {
        log.debug("Start garbage collector job...")
        attachementService.deleteAttachementAndFileInSystem()
        log.debug("End garbage collector job.")
    }
}
