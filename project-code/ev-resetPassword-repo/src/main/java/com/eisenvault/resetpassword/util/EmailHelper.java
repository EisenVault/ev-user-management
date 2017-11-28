package com.eisenvault.resetpassword.util;

import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;

import com.eisenvault.resetpassword.exceptions.EmailTemplateException;

import java.util.List;

public class EmailHelper extends BaseScopableProcessorExtension{

    private ServiceRegistry services;
    private Repository repositoryHelper;

    public ScriptNode getLocalizedEmailTemplate(String xpath) {

        List<NodeRef> nodes = services.getSearchService().selectNodes(repositoryHelper.getCompanyHome(),
                xpath, null, services.getNamespaceService(), true);

        if (nodes.size()!=1) {
        	throw new EmailTemplateException("There is some exception while resetting your password. Please contact your administrator");
        }
        
        NodeRef emailTemplate = services.getFileFolderService().getLocalizedSibling(nodes.get(0));

        return new ScriptNode(emailTemplate, services, this.getScope());
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.services = serviceRegistry;
    }

    public void setRepositoryHelper(Repository repositoryHelper) {
        this.repositoryHelper = repositoryHelper;
    }
}
