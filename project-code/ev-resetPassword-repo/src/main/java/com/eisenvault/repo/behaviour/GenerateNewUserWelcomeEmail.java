package com.eisenvault.repo.behaviour;

import java.security.SecureRandom;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.PasswordGenerator;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

public class GenerateNewUserWelcomeEmail implements NodeServicePolicies.OnCreateNodePolicy, InitializingBean {
	
	private char[] possibleCharacters = (new String("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789~!@#$%*-_)")).toCharArray();
    private PolicyComponent policyComponent;
    private NodeService nodeService;
    private PersonService personService;
    private MutableAuthenticationService authenticationService;
    
    private static Logger logger = Logger.getLogger(GenerateNewUserWelcomeEmail.class);

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    public void setAuthenticationService(AuthenticationService authenticationService) {
        if (authenticationService instanceof MutableAuthenticationService) {
            this.authenticationService = (MutableAuthenticationService) authenticationService;
        }
    }

    @Override
    public void onCreateNode(ChildAssociationRef childAssocRef) {
    	logger.debug("\n\n--------------------------------- Initializing GenerateNewUserWelcomeEmail behaviour");
        notifyUser(childAssocRef);
    }

    private void notifyUser(ChildAssociationRef childAssocRef) {
        NodeRef personRef = childAssocRef.getChildRef();
        // get the user name
        String username = (String) this.nodeService.getProperty(personRef, ContentModel.PROP_USERNAME);
        // generate the new password (Alfresco's rules)
        String newPassword = RandomStringUtils.random( 10, 0, possibleCharacters.length-1, false, false, possibleCharacters, new SecureRandom() );
        // set the new password
        authenticationService.setAuthentication(username, newPassword.toCharArray());
        // send default notification to the user
        personService.notifyPerson(username, newPassword);
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        PropertyCheck.mandatory(this, "policyComponent", policyComponent);
        PropertyCheck.mandatory(this, "nodeService", nodeService);
        PropertyCheck.mandatory(this, "authenticationService", authenticationService);
        PropertyCheck.mandatory(this, "personService", personService);

        this.policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnCreateNodePolicy.QNAME,
                ContentModel.TYPE_PERSON,
                new JavaBehaviour(this, NodeServicePolicies.OnCreateNodePolicy.QNAME.getLocalName()
                		, Behaviour.NotificationFrequency.TRANSACTION_COMMIT));
    }
}