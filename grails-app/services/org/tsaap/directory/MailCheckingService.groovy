package org.tsaap.directory

import grails.plugin.mail.MailService
import groovy.sql.Sql
import org.springframework.context.MessageSource

import javax.sql.DataSource

class MailCheckingService {

  static transactional = false

  MailService mailService
  DataSource dataSource
  MessageSource messageSource

  /**
   * Send email to check user emails and then activate the corresponding user
   * accounts
   * @param user the user to check email in
   */
  def sendCheckingEmailMessages() {
    Map notifications = findAllNotifications()
    List<String> actKeysWithEmailSent = []
    notifications.each { actKey, user ->
      actKeysWithEmailSent << actKey
      try {
        def sub = messageSource.getMessage("email.checking.title",null,new Locale(user.language))
        mailService.sendMail {
          to user.email
          subject sub
          html view: "/email/emailChecking", model: [user: user,
                  actKey: actKey]
        }
      } catch (Exception e) {
        log.error("Error with ${user.email} : ${e.message}")
      }
    }
    log.debug("Nb email sending try : ${actKeysWithEmailSent.size()}")
    if (actKeysWithEmailSent) {
      updateEmailSentStatusForAllNotifications(actKeysWithEmailSent)
    }
  }

  /**
   * The notifications is a map :<br>
   * <ul>
   *   <li> key : the activation key
   *   <li> value : a map representing the corresponding user [user_id:..., first_name:...,email:...]
   * </ul>
   * @return the notifications as a map
   */
  private Map findAllNotifications() {
    def sql = new Sql(dataSource)
    def req = """
              SELECT tuser.id as user_id, tuser.first_name, tuser.email, tuser.language, tact_key.activation_key
              FROM user as tuser
              INNER JOIN  activation_key as tact_key ON tact_key.user_id = tuser.id
              where tact_key.activation_email_sent = false"""
    def rows = sql.rows(req)
    log.debug("Select request : $req")
    log.debug("Nb rows selected : ${rows.size()}")
    def notifications = [:]
    rows.each {
      def key = it.activation_key
      notifications[key] = [user_id: it.user_id, first_name: it.first_name, email: it.email, language: it.language]
    }
    sql.close()
    log.debug("Notifications to process : $notifications")
    notifications
  }

  /**
   *
   * @param actKeys list of activation keys which email was sent for
   */
  private updateEmailSentStatusForAllNotifications(List<String> actKeys) {

    def placeholderKeys = actKeys.collect { '?' }.join( ',' )
    def sql = new Sql(dataSource)
    def req = "update activation_key as tact_key set tact_key.activation_email_sent = true where tact_key.activation_key in ($placeholderKeys)"
    def nbUpdates = sql.executeUpdate(req, actKeys)
    log.debug("the update request : $req")
    log.debug("Nb of rows updated : $nbUpdates")
    sql.close()

  }
}
