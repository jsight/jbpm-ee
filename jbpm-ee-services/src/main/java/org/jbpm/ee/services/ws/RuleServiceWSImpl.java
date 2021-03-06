package org.jbpm.ee.services.ws;

import javax.ejb.EJB;
import javax.jws.HandlerChain;
import javax.jws.WebService;

import org.jbpm.ee.services.ejb.local.RuleServiceLocal;
import org.jbpm.ee.services.ws.exceptions.RemoteServiceException;

/**
 * @see RuleServiceWS
 * @author bradsdavis
 *
 */
@WebService(targetNamespace="http://jbpm.org/v6/RuleService/wsdl", serviceName="RuleService", endpointInterface="org.jbpm.ee.services.ws.RuleServiceWS")
@HandlerChain(file="jbpm-context-handler.xml")
public class RuleServiceWSImpl implements RuleServiceWS {
	
	@EJB
	private RuleServiceLocal ruleRuntime;
	
	@Override
	public int fireAllRules(Long processInstanceId) {
		try {
			return ruleRuntime.fireAllRules(processInstanceId);
		}
		catch(Exception e) {
			throw new RemoteServiceException(e);
		}
	}

	@Override
	public int fireAllRules(Long processInstanceId, int max) {
		try {
			return ruleRuntime.fireAllRules(processInstanceId, max);
		}
		catch(Exception e) {
			throw new RemoteServiceException(e);
		}
	}

	@Override
	public void insert(Long processInstanceId, Object object) {
		try {
			ruleRuntime.insert(processInstanceId, object);			
		}
		catch(Exception e) {
			throw new RemoteServiceException(e);
		}
	}

}
