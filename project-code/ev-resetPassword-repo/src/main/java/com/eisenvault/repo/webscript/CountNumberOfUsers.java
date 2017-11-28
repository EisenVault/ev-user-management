package com.eisenvault.repo.webscript;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import com.eisenvault.repo.util.NumberOfUsersLimitCheck;

public class CountNumberOfUsers extends DeclarativeWebScript
{
	private NumberOfUsersLimitCheck numberOfUsersLimitCheck;
    public void setNumberOfUsersLimitCheck(NumberOfUsersLimitCheck numberOfUsersLimitCheck) {
		this.numberOfUsersLimitCheck = numberOfUsersLimitCheck;
	}

    private static Logger logger = Logger.getLogger(CountNumberOfUsers.class); 

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status) {
    	
    	Map<String, Object> model = new HashMap<String, Object>(1, 1.0f);
    	if(numberOfUsersLimitCheck.isNumberOfUsersLimitExceeded()){
    		logger.debug("Number of users less than the max number of users. Returning false");
    		model.put("returnStatus", Boolean.FALSE);
    	}else{
    		logger.debug("Number of users greater than the max number of users. Returning true");
    		model.put("returnStatus", Boolean.TRUE);
    	}
        return model;
    }
}