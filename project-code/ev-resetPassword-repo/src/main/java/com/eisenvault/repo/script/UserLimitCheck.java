package com.eisenvault.repo.script;

import org.alfresco.repo.processor.BaseProcessorExtension;
import org.apache.log4j.Logger;

import com.eisenvault.repo.overridden.webscript.CustomUserCSVUploadPost;
import com.eisenvault.repo.util.NumberOfUsersLimitCheck;

public class UserLimitCheck extends BaseProcessorExtension{
	
	private NumberOfUsersLimitCheck numberOfUsersLimitCheck;
    public void setNumberOfUsersLimitCheck(NumberOfUsersLimitCheck numberOfUsersLimitCheck) {
		this.numberOfUsersLimitCheck = numberOfUsersLimitCheck;
	}
	
	private static Logger logger = Logger.getLogger(CustomUserCSVUploadPost.class);
	
	public boolean isNumberOfUsersLimitExceeded(){
		logger.debug("Checking users creation limit. Sending true or false accordingly");
		return (numberOfUsersLimitCheck.isNumberOfUsersLimitExceeded());
	}
}