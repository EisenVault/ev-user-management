package com.eisenvault.repo.util;

import java.util.Arrays;

import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.util.PropertyCheck;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

public class NumberOfUsersLimitCheck implements InitializingBean {

	private String maxNumberOfUsers;
	private PersonService personService;
	private TenantService tenantService;

	private static Logger logger = Logger
			.getLogger(NumberOfUsersLimitCheck.class);

	public void setMaxNumberOfUsers(String maxNumberOfUsers) {
		this.maxNumberOfUsers = maxNumberOfUsers;
	}

	public void setPersonService(PersonService personService) {
		this.personService = personService;
	}

	public void setTenantService(TenantService tenantService) {
		this.tenantService = tenantService;
	}

	public boolean isNumberOfUsersLimitExceeded() {
		int maxUsersLimit = 0;

		String tenantDomain = tenantService.getCurrentUserDomain();
		logger.debug("\n\n-------- Tenant Domain is : " + tenantDomain);

		// maxNumberOfUsers will be in the form
		// maxNumberOfUsers|tenant1=maxNumberOfUsers1|tenant2=maxNumberOfUsers2
		String[] maxUsersTenantSplit = maxNumberOfUsers.split(",");
		logger.debug("\n\n-------- Length of maxUsersTenantSplit : "
				+ maxUsersTenantSplit.length);

		// set maxUsersLimit to first value of maxUsersTenantSplit which is
		// maxNumberOfUsers
		maxUsersLimit = Integer.parseInt(maxUsersTenantSplit[0]);
		logger.debug("\n\n-------- Default max number of user : "
				+ maxUsersLimit);

		if (maxUsersTenantSplit.length == 1) {
			logger.debug("\n\n-------- There are no tenants ");
		} else {
			String[] maxUsersTenantSplitSmall = Arrays.copyOfRange(
					maxUsersTenantSplit, 1, maxUsersTenantSplit.length);
			logger.debug("\n\n-------- Length of maxUsersTenantSplitSmall : "
					+ maxUsersTenantSplitSmall.length);

			if (tenantDomain != null && !tenantDomain.trim().isEmpty()) {
				// We are in a tenant domain
				for (int i = 0; i < maxUsersTenantSplitSmall.length; i++) {
					String tenant = maxUsersTenantSplitSmall[i];
					logger.debug("\n\n-------- Tenant is  : " + tenant);
					if (tenant.startsWith(tenantDomain)) {
						logger.debug("\n\n-------- Tenant found starting with  : "
								+ tenantDomain);
						String[] tenantInfo = tenant.split("=");
						maxUsersLimit = Integer.parseInt(tenantInfo[1]);
						logger.debug("\n\n-------- Changing max number of user : "
								+ maxUsersLimit);
						break;
					}
				}
			}
		}

		logger.debug("\n\n-------- Max Number of Users now : " + maxUsersLimit);
		logger.debug("\n\n-------- Number of Users in DMS : "
				+ personService.countPeople());

		return (!(personService.countPeople() < maxUsersLimit));
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		logger.debug("/n/n------- Initializing bean for class UserCount");
		PropertyCheck.mandatory(this, "personService", personService);
	}
}
