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



package org.tsaap.questions

import org.tsaap.questions.impl.DefaultExclusiveChoiceQuestion
import org.tsaap.questions.impl.DefaultQuestion
import org.tsaap.questions.impl.DefaultUserAnswerBlock
import org.tsaap.questions.impl.DefaultUserResponse
import org.tsaap.questions.impl.gift.GiftQuizContentHandler
import org.tsaap.questions.impl.gift.GiftReader
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions*/

class UserResponseSpec extends Specification {


  @Unroll
  def "test the evaluation of the valid response given on an Exclusive Choice question"() {

    given: "a question corresponding to an EC question written in gift format"
    DefaultExclusiveChoiceQuestion question = getQuestionFromQuestionText(ec_q3_ok)

    when:"the user choose the good answer"
    UserResponse userResponse = new DefaultUserResponse();
    UserAnswerBlock userAnswerBlock = new DefaultUserAnswerBlock();
    userAnswerBlock.answerBlock = question.answerBlock
    userAnswerBlock.answerList = new ArrayList<Answer>();
    userAnswerBlock.answerList.add(question.goodAnswer)
    userResponse.userAnswerBlockList.add(userAnswerBlock)


    then:"the user has 100% of credit"
    userResponse.evaluatePercentCredit() == 100f

    when:"the user choose the bad answer"
    userAnswerBlock.answerList.clear()
    def badAnswer = question.answerBlock.answerList.find { it != question.goodAnswer}
    userAnswerBlock.answerList.add(badAnswer)

    then:"the user has 0% credit"
    userResponse.evaluatePercentCredit() == 0f

    when: "the user choose no answers"
    userAnswerBlock.answerList.clear()

    then: "the user has 0% credit"
    userResponse.evaluatePercentCredit() == 0f


  }

//  @Unroll
//  def "test the validation of an EC question response"() {
//    given: "a question corresponding to an EC question written in gift format"
//
//    when: "a response given by a user contains more than one answer"
//
//    then:"the response is not valid"
//  }


  @Shared // EC Question with escape characters
  def ec_q3_ok = '::Question \\: 3:: What\'s between orange and green in the \\#spectrum ? \n { =yellow # congrats ! ~red #not \\= ~blue # try again }'



  static private Question getQuestionFromQuestionText(String questionText) {
    GiftQuizContentHandler handler = new GiftQuizContentHandler()
    def quizReader = new GiftReader(quizContentHandler: handler)
    def reader = new StringReader(questionText)
    quizReader.parse(reader)
    def quiz = handler.quiz
    quiz.questionList.size() == 1
    return quiz.questionList[0]
  }

}