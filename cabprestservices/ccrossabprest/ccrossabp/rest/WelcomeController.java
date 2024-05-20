
package cabprestservices.ccrossabprest.ccrossabp.rest;

import java.rmi.RemoteException;
import java.util.Properties;

import javax.ejb.CreateException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import abprestservices.cm.inputObject.CMSubServCreateNewSubInp;
import amdocs.csm3g.datatypes.ActivityDateInfo;
import amdocs.csm3g.datatypes.ActivityInfo;
import amdocs.csm3g.datatypes.AddressInfo;
import amdocs.csm3g.datatypes.BusinessEntityIdInfo;
import amdocs.csm3g.datatypes.ChargeDistributionDetailsInfo;
import amdocs.csm3g.datatypes.CustomerIdInfo;
import amdocs.csm3g.datatypes.EventDistributionDetailsInfo;
import amdocs.csm3g.datatypes.ExternalIdInfo;
import amdocs.csm3g.datatypes.GuidingResourceInfo;
import amdocs.csm3g.datatypes.LogicalResourceInfo;
import amdocs.csm3g.datatypes.NameInfo;
import amdocs.csm3g.datatypes.ParameterInfo;
import amdocs.csm3g.datatypes.PayChannelIdInfo;
import amdocs.csm3g.datatypes.PhysicalResourceInfo;
import amdocs.csm3g.datatypes.ResourceInfo;
import amdocs.csm3g.datatypes.SrvAgrInfo;
import amdocs.csm3g.datatypes.SubscriberGeneralInfo;
import amdocs.csm3g.datatypes.SubscriberIdInfo;
import amdocs.csm3g.datatypes.SubscriberIdsInfo;
import amdocs.csm3g.datatypes.SubscriberTypeInfo;
import amdocs.csm3g.datatypes.UnitIdInfo;
import amdocs.csm3g.datatypes.UserGroupResourceInfo;
import amdocs.csm3g.exceptions.CMException;
import amdocs.csm3g.sessions.interfaces.api.SubscriberServices;
import amdocs.csm3g.sessions.interfaces.home.SubscriberServicesHome;

@RestController
public class WelcomeController {
	
	private static String url;
	private static String host;
	private static String port;
	private static String username;
	private static String password;
	private static final String initialContextFactory = "weblogic.jndi.WLInitialContextFactory";
	public static final String CM_SUB_API_JNDI_NAME = "amdocsBeans.CM1SubscriberServicesHome";
	private static final String jndi = "amdocsBeans.CM1SubscriberServicesHome";
	public static final String CS_STATUS = "CS";
	public static final int ORDER_NOT_AVAILABLE_STATUS = 0;
	
	public static final String ERROR_INVALID_SECURITY_TOKEN = "UNAUTHORIZED. The ASM Security Token is INVALID.";
	public static final String ERROR_JNDI_LOOKUP = "Error in JNDI Lookup";
	public static final String ERROR_EJB_CREATION = "Error in creating EJB";
	
	private static final Logger logger = LoggerFactory.getLogger(WelcomeController.class);

	@GetMapping("/hello")
	public String helloWorld() {
		logger.trace("A TRACE Message");
		logger.debug("A DEBUG Message");
		logger.info("An INFO Message");
		logger.warn("A WARN Message");
		logger.error("An ERROR Message");
		return "Welcome to ABP REST WebServices";
	}
	
	private void setInitialContext() {
		host = System.getenv("AR_WEBLOGIC_HOST");
		port = System.getenv("AR_WEBLOGIC_PORT");
		url = new StringBuffer("t3://").append(host).append(":").append(port)
				.toString();
		username = System.getenv("ABP_CM_USER");
		password = System.getenv("L9_SMS_PASS");
		logger.debug("URL:" + url);
		/*log.debug("username:password=" + username + ":" + password);*/
		System.out.println("username:password=" + username + ":" + password);
		System.out.println("URL:" + url);

	}
	
	
	
	
	
	@PostMapping("/createSubscriber")
	public ResponseEntity<SubscriberIdsInfo> createSubscriber(@RequestHeader(name="Authorization", required = false) String secTicket, @RequestBody CMSubServCreateNewSubInp cMSubServCreateNewSubInp) {
		
		
		setInitialContext();
		
		InitialContext initialContext = null;
		if(secTicket==null) {
			System.out.println("secTicket is null");
			//secTicket="EXT<Tksmau4CM-bQC9kNAYuIqBuYqf65Fqs7ax64votZGT0;appId=MI;>";
		}
		
		Properties p = new Properties();
		p.put(Context.INITIAL_CONTEXT_FACTORY, "" + initialContextFactory);
		p.put(Context.PROVIDER_URL, "" + url);
		p.put(Context.SECURITY_PRINCIPAL, "" + secTicket);
		p.put(Context.SECURITY_CREDENTIALS, "" + "");
		System.out.println("secTicket :"+secTicket);
		try {
			initialContext = new InitialContext(p);
		} catch (NamingException e) {
			System.out.println(e.toString());//logger.debug(e.toString());
			return (ResponseEntity<SubscriberIdsInfo>) ResponseEntity.status(HttpStatus.UNAUTHORIZED);//.entity(ERROR_INVALID_SECURITY_TOKEN).build();
			
			
		}	
		
		
		SubscriberServicesHome subscriberServicesHome = null;
		try {
			subscriberServicesHome = (SubscriberServicesHome) initialContext.lookup(jndi);//NOSONAR
		} catch (NamingException e) {
			System.out.println(e.toString());//logger.debug(e.toString());
			return (ResponseEntity<SubscriberIdsInfo>) ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR);//.entity(ERROR_JNDI_LOOKUP).build();
		}
		
		SubscriberServices subscriberServices = null;
		try {
			subscriberServices = subscriberServicesHome.create();//NOSONAR
		} catch (RemoteException e) {
			System.out.println(e.toString());//logger.debug(e.toString());
			return (ResponseEntity<SubscriberIdsInfo>) ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR);//.entity(ERROR_EJB_CREATION).build();

		} catch (CreateException e) {
			System.out.println(e.toString());//logger.debug(e.toString());
			return (ResponseEntity<SubscriberIdsInfo>) ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR);//entity(ERROR_EJB_CREATION).build();

		}
		
		
		
		CustomerIdInfo customerIdInfo  = cMSubServCreateNewSubInp.getCustomerIdInfo();
		
		SubscriberIdInfo predefinedSubscriberIdInfo = cMSubServCreateNewSubInp.getPredefinedSubscriberIdInfo();
		
		UnitIdInfo  predefinedSubscriberUnitIdInfo = cMSubServCreateNewSubInp.getPredefinedSubscriberUnitIdInfo();

		ExternalIdInfo externalIdInfo = cMSubServCreateNewSubInp.getSubscriberExternalIdInfo();
		
		UnitIdInfo  unitIdInfo = cMSubServCreateNewSubInp.getUnitIdInfo();
		
		SubscriberTypeInfo subscriberTypeInfo = cMSubServCreateNewSubInp.getSubscriberTypeInfo();
		
		SubscriberGeneralInfo  subscriberGeneralInfo = cMSubServCreateNewSubInp.getSubscriberGeneralInfo();		
		//SrvAgrInfo srvAgrInfo = null;
		NameInfo[] nameInfo = cMSubServCreateNewSubInp.getNameInfo();
		AddressInfo[] addressInfo = cMSubServCreateNewSubInp.getAddressInfo();
		
		SrvAgrInfo[] srvAgrInfo = cMSubServCreateNewSubInp.getOffers();
		
		LogicalResourceInfo[] logicalResourceInfo = cMSubServCreateNewSubInp.getLogicalResourceInfo();

		PhysicalResourceInfo[] physicalResourceInfo = cMSubServCreateNewSubInp.getPhysicalResourceInfo();
			
		
		ParameterInfo[]  parameterInfo  = cMSubServCreateNewSubInp.getParameterInfo();
		
		
		
		 
		PayChannelIdInfo payChannelIdInfo1 = cMSubServCreateNewSubInp.getDefaultOCPayChannelInfo();
		PayChannelIdInfo payChannelIdInfo2 = cMSubServCreateNewSubInp.getDefaultRCPayChannelIdInfo();
		PayChannelIdInfo payChannelIdInfo3 = cMSubServCreateNewSubInp.getPrimaryEventPayChannelIdInfo();
		ChargeDistributionDetailsInfo[] chargeDistributionDetailsInfo = cMSubServCreateNewSubInp.getChargeDistributionDetailsInfo();
		EventDistributionDetailsInfo[] eventDistributionDetailsInfo = cMSubServCreateNewSubInp.getEventDistributionDetailsInfo();
		GuidingResourceInfo[] guidingResourceInfo = cMSubServCreateNewSubInp.getGuidingResourceInfo();
		UserGroupResourceInfo[] userGroupResourceInfo = cMSubServCreateNewSubInp.getUserGroupResourceInfo();
		ResourceInfo[] resourceRangeList = cMSubServCreateNewSubInp.getResourceRangeList();
		BusinessEntityIdInfo businessEntityIdInfo = cMSubServCreateNewSubInp.getBusinessEntityIdInfo();
		
		ActivityInfo activityInfo = cMSubServCreateNewSubInp.getActivityInfo();
		
		ActivityDateInfo activityDateInfo = cMSubServCreateNewSubInp.getActivityDateInfo();
		
		SubscriberIdsInfo subscriberIdInfoResult = null;
		
		try {
			
					
			subscriberIdInfoResult = subscriberServices.createNewActivateSubscriber(customerIdInfo, predefinedSubscriberIdInfo, predefinedSubscriberUnitIdInfo, externalIdInfo, unitIdInfo, subscriberTypeInfo, subscriberGeneralInfo	,
			nameInfo, addressInfo, srvAgrInfo, logicalResourceInfo, physicalResourceInfo, parameterInfo, payChannelIdInfo1, payChannelIdInfo2, payChannelIdInfo3, chargeDistributionDetailsInfo, 		eventDistributionDetailsInfo, guidingResourceInfo, userGroupResourceInfo, resourceRangeList, businessEntityIdInfo, activityInfo, activityDateInfo);

			
			
		} catch (CMException e) {
			// TODO Auto-generated catch block
			System.out.println(e.toString());//logger.debug(e.toString());
			return (ResponseEntity<SubscriberIdsInfo>) ResponseEntity.status(HttpStatus.BAD_REQUEST);//.entity(e).build();

		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			System.out.println(e.toString());//logger.debug(e.toString());
			return (ResponseEntity<SubscriberIdsInfo>) ResponseEntity.status(HttpStatus.BAD_REQUEST);//.entity(e).build();

		}
		
		/*
		 * String json = null; ObjectWriter ow = new
		 * ObjectMapper().writer().withDefaultPrettyPrinter();
		 * 
		 * try { json = ow.writeValueAsString(subscriberIdInfoResult); } catch
		 * (JsonProcessingException e) { // TODO Auto-generated catch block
		 * log.debug(e); return
		 * ResponseEntity.status(HttpStatus.BAD_REQUEST).entity(e).build();
		 * 
		 * }
		 */
		
		
		//return ResponseEntity.status(HttpStatus.OK).entity(json).build();
		return new ResponseEntity<>(subscriberIdInfoResult, HttpStatus.CREATED);

	}
	
}
