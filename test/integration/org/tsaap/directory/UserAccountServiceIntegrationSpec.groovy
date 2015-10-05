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

package org.tsaap.directory

import grails.plugins.springsecurity.SpringSecurityService
import org.tsaap.BootstrapService
import spock.lang.Specification
import spock.lang.Unroll

class UserAccountServiceIntegrationSpec extends Specification {

  BootstrapService bootstrapService
  UserAccountService userAccountService
  SpringSecurityService springSecurityService

  def "add user"() {
    given: 'the initialised referential for roles'
    bootstrapService.initializeRoles()

    when: "adding a user"
    def u = userAccountService.addUser(new User(firstName: "Mary", lastName: "S",
                                                username: "Mary_test",
                                                email: "mary@nomail.com",
                                                password: "password",
                                                language: 'en'),
                                       RoleEnum.STUDENT_ROLE.role)
    println "${u.errors}"

    then: "the user is persisted in the datastore"
    User user = User.findByUsername("Mary_test")
    user != null
    !user.enabled
    user.normalizedUsername == "mary_test"
    user.email == "mary@nomail.com"
    user.language == 'en'
    !user.authorities.empty
    user.authorities.first().roleName == RoleEnum.STUDENT_ROLE.name()
    user.password == springSecurityService.encodePassword("password")

    when: "adding a user with email checking activated"
    def user2 = userAccountService.addUser(new User(firstName: "Mary", lastName: "G",
                                                    username: "Mary_test_g",
                                                    email: "mary@mary.com",
                                                    password: "password",
                                                    language: 'en'),
                                           RoleEnum.STUDENT_ROLE.role, false, true)

    then: "the activation key is generated"
    !user2.hasErrors()
    def actKey = ActivationKey.findByUser(user2)
    actKey != null

  }

  def "enable user"() {
    bootstrapService.initializeRoles()

    when:
    def mary = userAccountService.addUser(new User(firstName: "Mary", lastName: "S",
                                                   username: "Mary_test",
                                                   email: "mary@nomail.com",
                                                   password: "password",
                                                   language: 'en'),
                                          RoleEnum.STUDENT_ROLE.role)
    userAccountService.enableUser(mary)

    then: User user = User.findByUsername("Mary_test")
    user != null
    user.enabled
  }

  def "disable user"() {

    given: 'a user with account enabled'
    bootstrapService.initializeRoles()
    def mary = userAccountService.addUser(new User(firstName: "Mary", lastName: "S",
                                                   username: "Mary_test",
                                                   email: "mary@nomail.com",
                                                   password: "password",
                                                   language: 'en'),
                                          RoleEnum.STUDENT_ROLE.role)
    userAccountService.enableUser(mary)

    when: 'the user account is asking to be disabled'
    userAccountService.disableUser(mary)

    then: 'the user is disabled '
    User user = User.findByUsername("Mary_test")
    user != null
    !user.enabled
  }

  @Unroll
  def "update user"() {

    given: ' an existing user'
    bootstrapService.initializeRoles()
    def mary = userAccountService.addUser(new User(firstName: "Mary", lastName: "S",
                                                   username: "Mary_test",
                                                   email: "mary@nomail.com",
                                                   password: "password",
                                                   language: 'en'),
                                          RoleEnum.STUDENT_ROLE.role)

    when: 'modifying properties of the user and ask for an update'
    mary.firstName = newFirstName
    mary.email = newEmail
    mary.language = newLanguage
    Role mainRole = RoleEnum.valueOf(newRoleName).role
    userAccountService.updateUser(mary, mainRole)

    then: 'the user is modified or has errors'
    User user = User.findByUsername("Mary_test")
    user != null
    println user.errors
    (user.version != 0) == userHasChanged
    user.hasErrors() == userHasErrors
    if(userHasChanged) {
      UserRole.get(user.id, mainRole.id) != null
    }

    where: newFirstName | newEmail          | newLanguage | newRoleName                   | userHasChanged | userHasErrors
    "Franck"            | "fsil@fsil.com"   | 'fr'        |  RoleEnum.TEACHER_ROLE.name() | true           | false
    "Mary"              | "mary@nomail.com" | 'en'        |  RoleEnum.TEACHER_ROLE.name() | false          | false
    "Mary"              | "mary@"           | 'en'        |  RoleEnum.TEACHER_ROLE.name() | false          | true
  }

  def "test language is supported"() {

      given: "some languages"
      String english = 'en'
      String russian = 'ru'

      when: "we test if given language are in LANGUAGE_SUPPORTED"
      def res1 = userAccountService.languageIsSupported(english)
      def res2 = userAccountService.languageIsSupported(russian)

      then: "we kwow if the language is supported by tsaap note"
      res1 == true
      res2 == false
  }

}
