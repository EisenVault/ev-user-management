<import resource="classpath:alfresco/extension/templates/webscripts/com/eisenvault/scripts/reset-pass-lib.js">

var ResetPassword = function () {

    this.reviewCreate = function () {
        if (typeof bpm_workflowDueDate != 'undefined') task.dueDate = bpm_workflowDueDate;
        if (typeof bpm_workflowPriority != 'undefined') task.priority = bpm_workflowPriority;

        //Send Email
        this._sendEmail("reset-password.ftl", bpm_assignee.properties.email, {})
    };

    this.reviewComplete = function () {

        var pass = task.getVariable("ev-reset_password");
        pass2 = task.getVariable("ev-reset_confirmPass");

        if (!checkPassword(pass)){
        	throw "Please enter a valid Password.\n Password must have at least one uppercase, one lowercase, one integer and one special character";
        }
        if (!isValid(pass, pass2)) {
            throw "Confirm password does not match password or password length too short";
        }

        var password = task.getVariableLocal("ev-reset_password"),
            userName = bpm_assignee.properties.userName;

        passwordService.setPassword(userName, password);
    };

    this._sendEmail = function (templateName, to) {     
        
        try {
            var template = emailHelper.getLocalizedEmailTemplate("app:dictionary/app:email_templates/cm:" + templateName);
        } catch (e) {
            throw e.javaException.getMessage();
        }
        
        var mail = actions.create("mail");
        var currentToken = token.genToken();

        mail.parameters.template = template;
        mail.parameters.to = to;
        mail.parameters.subject = template.properties.title || "EisenVault : Reset Password";
        mail.parameters.template_model = this.prepareTemplateProps(bpm_assignee, task, currentToken);
        task.setVariable("ev-reset:token", currentToken);

        try {

            mail.execute(bpm_assignee);

        } catch (ex) {

            throw "Failed to send email. Please, check outbound email configuration.";
        }

    };

    this.prepareTemplateProps = function (assignee, task, currentToken) {
        var templateProps = {};
        templateProps["userToken"] = assignee.properties["userName"];
        templateProps["assignee"] = {
            firstname: assignee.properties["firstName"],
            lastname: assignee.properties["lastName"]
        };
        templateProps["taskId"] = "activiti$" + task.id;
        templateProps["token"] = currentToken;
        return templateProps;
    }
};