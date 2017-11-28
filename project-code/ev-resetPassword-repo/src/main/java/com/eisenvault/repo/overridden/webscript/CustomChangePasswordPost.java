package com.eisenvault.repo.overridden.webscript;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.MailActionExecuter;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.web.scripts.person.ChangePasswordPost;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Webscript implementation for the POST method for 'changepassword' API.
 */
public class CustomChangePasswordPost extends ChangePasswordPost
{
    private static final String PARAM_NEWPW = "newpw";
    private static final String PARAM_OLDPW = "oldpw";
    private MutableAuthenticationService authenticationService;
    private AuthorityService authorityService;
    private ServiceRegistry serviceRegistry;
    
	private static Logger logger = Logger.getLogger(CustomChangePasswordPost.class);
    
    /**
     * @param authenticationService    the AuthenticationService to set
     */
    public void setAuthenticationService(MutableAuthenticationService authenticationService)
    {
        this.authenticationService = authenticationService;
    }
    
    /**
     * @param authorityService          the AuthorityService to set
     */
    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }
    

    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.serviceRegistry = serviceRegistry;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.DeclarativeWebScript#executeImpl(org.alfresco.web.scripts.WebScriptRequest, org.alfresco.web.scripts.Status)
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status)
    {
        // Extract user name from the URL - cannot be null or webscript desc would not match
        String userName = req.getExtensionPath();
        
        // Extract old and new password details from JSON POST
        Content c = req.getContent();
        if (c == null)
        {
            throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR, "Missing POST body.");
        }
        JSONObject json;
        try
        {
            json = new JSONObject(c.getContent());
            
            String oldPassword = null;
            String newPassword;
            
            // admin users can change/set a password without knowing the old one
            boolean isAdmin = authorityService.hasAdminAuthority();
            if (!isAdmin || (userName.equalsIgnoreCase(authenticationService.getCurrentUserName())))
            {
                if (!json.has(PARAM_OLDPW) || json.getString(PARAM_OLDPW).length() == 0)
                {
                    throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                        "Old password 'oldpw' is a required POST parameter.");
                }
                oldPassword = json.getString(PARAM_OLDPW);
            }
            if (!json.has(PARAM_NEWPW) || json.getString(PARAM_NEWPW).length() == 0)
            {
                throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                    "New password 'newpw' is a required POST parameter.");
            }
            newPassword = json.getString(PARAM_NEWPW);
     
            // update the password
            // an Admin user can update without knowing the original pass - but must know their own!
            if (!isAdmin || (userName.equalsIgnoreCase(authenticationService.getCurrentUserName())))
            {
                authenticationService.updateAuthentication(userName, oldPassword.toCharArray(), newPassword.toCharArray());
                // EisenVault - Monica - Added sendMail method
                logger.debug("\n\n\n\n************ User " + userName + " changed password. Sending mail to the user *********** ");
                sendMail(true, userName, newPassword);             
            }
            else
            {
                authenticationService.setAuthentication(userName, newPassword.toCharArray());
                // EisenVault - Monica - Added sendMail method
                logger.debug("\n\n\n\n************ Admin changed password of user " + userName + ". Sending mail to the user and admin *********** ");
                sendMail(false, userName, newPassword);                
            }
        }
        catch (AuthenticationException err)
        {
            throw new WebScriptException(Status.STATUS_UNAUTHORIZED,
                    "Do not have appropriate auth or wrong auth details provided.");
        }
        catch (JSONException jErr)
        {
            throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR,
                    "Unable to parse JSON POST body: " + jErr.getMessage());
        }
        catch (IOException ioErr)
        {
            throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR,
                    "Unable to retrieve POST body: " + ioErr.getMessage());
        }
        
        Map<String, Object> model = new HashMap<String, Object>(1, 1.0f);
        model.put("success", Boolean.TRUE);
        return model;
    }
    
    /**
     * 
     * @param check - check whether user is setting the password(true) else admin is setting the password(false)
     * @param userName
     * @param newPassword
     */
    private void sendMail(boolean check, String userName, String newPassword)
    {
    	// Get the required services
    	PersonService personService = serviceRegistry.getPersonService();
    	NodeService nodeService = serviceRegistry.getNodeService();
    	ActionService actionService = serviceRegistry.getActionService();
    	SearchService searchService = serviceRegistry.getSearchService();
    	
    	Map<String, Serializable> templateArgs = new HashMap<String, Serializable>();
    	
    	String adminUserName = AuthenticationUtil.getAdminUserName();
        NodeRef personRef = personService.getPerson(userName); 
        NodeRef adminRef = personService.getPerson(adminUserName);      
        String templatePATH = "PATH:\"/app:company_home/app:dictionary/app:email_templates/cm:changed-user-password-email.html.ftl\"";
        String firstName = nodeService.getProperty(personRef, ContentModel.PROP_FIRSTNAME).toString();
        String userEmail = nodeService.getProperty(personRef, ContentModel.PROP_EMAIL).toString();
        String adminEmail = nodeService.getProperty(adminRef, ContentModel.PROP_EMAIL).toString();
        Action mailAction = actionService.createAction(MailActionExecuter.NAME);
        ResultSet resultSet = searchService.query(new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore"), SearchService.LANGUAGE_LUCENE, templatePATH);
        if (resultSet.length()==0){
           logger.error("--------------------------Template "+ templatePATH +" not found.");
        }        
        NodeRef template = resultSet.getNodeRef(0);
        
        mailAction.setParameterValue(MailActionExecuter.PARAM_SUBJECT, "EisenVault : Password changed.");       
        mailAction.setParameterValue(MailActionExecuter.PARAM_TO, userEmail);
        mailAction.setParameterValue(MailActionExecuter.PARAM_TEMPLATE, template);
        
        templateArgs.put("firstname", firstName);
        templateArgs.put("password", newPassword);
        
        if(check) {
        	// The user has changes the password. No need to send mail to admin in CC
        }
        else
        {
            mailAction.setParameterValue(MailActionExecuter.PARAM_CC, adminEmail);
            templateArgs.put("admin", adminUserName);
        }        
        Map<String, Serializable> templateModel = new HashMap<String, Serializable>();
        templateModel.put("args",(Serializable)templateArgs);
        mailAction.setParameterValue(MailActionExecuter.PARAM_TEMPLATE_MODEL,(Serializable)templateModel);
        actionService.executeAction(mailAction, null);     
    }
}