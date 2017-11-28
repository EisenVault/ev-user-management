var namespace = function (identifier) {
    var klasses = arguments[1] || false;
    var ns = window;

    if (identifier !== '') {
        var parts = identifier.split(".");
        for (var i = 0; i < parts.length; i++) {
            if (!ns[parts[i]]) {
                ns[parts[i]] = {};
            }
            ns = ns[parts[i]];
        }
    }

    if (klasses) {
        for (var klass in klasses) {
            if (klasses.hasOwnProperty(klass)) {
                ns[klass] = klasses[klass];
            }
        }
    }

    return ns;

};

(function () {
    namespace("EisenVault.component");

    var Dom = YAHOO.util.Dom;

    EisenVault.component.ResetPassword = function Login_constructor(htmlId) {
        EisenVault.component.ResetPassword.superclass.constructor.call(this, "EisenVault.component.ResetPassword", htmlId, ["button"]);

        return this;
    };

    YAHOO.extend(EisenVault.component.ResetPassword, Alfresco.component.Base,
        {
            options: {
                spinner: null
            },

            onReady: function Login_onReady() {
                var resetPasswordButton = Alfresco.util.createYUIButton(this, "show", this.showDialog, {type: "button"}, this.id);

                var resetPasswordElement = Dom.get(resetPasswordButton);

                var parent = resetPasswordElement.parentElement;

                this.getFormElement().appendChild(parent);
            },

            showDialog: function (p_event, p_obj) {

                Alfresco.util.PopupManager.getUserInput({
                    title: Alfresco.util.message("dialog.title"),
                    text: Alfresco.util.message("dialog.text"),
                    okButtonText: Alfresco.util.message("dialog.okButton.label"),
                    input: "text",
                    callback: {
                        fn: this.resetPassword,
                        scope: this
                    }
                });
            },

            resetPassword: function (text) {

                this.showSpinner();

                Alfresco.util.Ajax.jsonPost({
                    url: Alfresco.constants.URL_CONTEXT + "proxy/alfresco-noauth/com/eisenvault/reset-password",
                    dataObj: {
                        userName: text
                    },
                    successCallback: {
                        fn: this.successCallback,
                        scope: this
                    },
                    failureCallback: {
                        fn: this.failureCallback,
                        scope: this
                    }
                });

            },

            successCallback: function () {
                
                this.hideSpinner();

                Alfresco.util.PopupManager.displayMessage({
                    text: Alfresco.util.message("success.text"),
                    displayTime: 5
                })

            },

            failureCallback: function (resp) {

                this.hideSpinner();
                
                Alfresco.util.PopupManager.displayPrompt(
                    {
                        title: Alfresco.util.message("error.title"),
                        text: resp.json ? resp.json.message : resp.serverResponse || Alfresco.util.message("error.server-side")
                    });
            },


            getFormElement: function () {

                var loginComponent = Alfresco.util.ComponentManager.findFirst("Alfresco.component.Login");

                return loginComponent.widgets.submitButton.getForm();
            },

            showSpinner: function () {

                this.options.spinner = Alfresco.util.PopupManager.displayMessage({
                    text: Alfresco.util.message("spinner.message"),
                    displayTime: 0,
                    spanClass: "wait"
                });
            },

            hideSpinner: function () {
                this.options.spinner.destroy();
            }

        });
})();