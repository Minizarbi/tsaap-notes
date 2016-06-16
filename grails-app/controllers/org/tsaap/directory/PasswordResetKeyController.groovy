package org.tsaap.directory

class PasswordResetKeyController {

    PasswordResetService passwordResetService
    /**
     * render the forget password view
     */
    def doForget() {
        render view:'forgetPassword'
    }

    /**
     * Reset the password if the email address is valid, otherwise return message error
     */
    def doReset() {

        def user = User.findByEmail(params.email)
        if (user) {
            passwordResetService.generatePasswordResetKeyForUser(user)
            flash.message = message(code: 'useraccount.reset.email.success')
            redirect(uri: '/login/auth')
        }
        else {
            flash.message = message(code: 'password.reset.email.fail')
            render view:'forgetPassword'
        }

    }

    /**
     * render passwordReset view to confirm the new password
     */
    def doPasswordReset() {
        render view:'passwordReset'
    }

    def resetPassword() {
        def key = params.passwordResetKey
        User user = PasswordResetKey.findByPasswordResetKey(key).user
        //List<User> user = PasswordResetKey.executeQuery("SELECT user from PasswordResetKey where password_reset_key = :var", [var: key])

        if (params.password != params.passwordConfirm) {
            user.errors.rejectValue('password', 'user.password.confirm.fail', 'The two passwords must be the same.')
        } else {
            user.password = params.password
            user.save(flush: true)
        }
        if (user.hasErrors()) {
            flash.message = message(code: 'password.reset.fail')
            render(view: 'passwordReset', model: [passwordResetKey: params.passwordResetKey])
        } else {
            flash.message = message(code: 'useraccount.update.success')
            redirect(uri: '/login/auth')
        }

    }


}
