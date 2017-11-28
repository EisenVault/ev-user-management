<@markup id="reset-pass-css" target="js" action="after">
<#-- CSS Dependencies -->
    <@link href="${url.context}/res/extras/com/eisenvault/components/ResetPassword.css" group="login"/>

<script type="text/javascript">
    new EisenVault.component.ResetPassword("${args.htmlid}-reset-Password-process");
</script>
</@markup>

<@markup id="reset-pass-js">
<#-- JavaScript Dependencies -->
    <@script src="${url.context}/res/extras/com/eisenvault/components/ResetPassword.js" group="login"/>
</@markup>

<@markup id="reset-password" target="buttons" action="after" scope="page">
<div class="form-field">
    <input type="button" id="${args.htmlid}-reset-Password-process" class="login-button hidden"
           value="${msg("button.reset-Password-process")}"/>
</div>
</@>

<@markup id="footer_custom" target="footer" action="replace" scope="page">
<div class="copy">${msg("label.copyright")}</div>
</@markup>