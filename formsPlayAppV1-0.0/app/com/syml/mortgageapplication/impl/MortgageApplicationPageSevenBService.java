package com.syml.mortgageapplication.impl;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import play.Logger;
import play.data.DynamicForm;
import play.mvc.Http.Session;

import com.syml.SplitAddress;
import com.syml.couchbase.dao.CouchbaseDaoServiceException;
import com.syml.couchbase.dao.service.CouchBaseService;
import com.syml.couchbasehelper.CouchbaseServiceHelper;
import com.syml.hibernate.dao.PostGressDaoServiceException;
import com.syml.hibernate.service.PostGresDaoService;
import com.syml.mortgageapplication.MortgageApplicationConstants;

import controllers.Address;
import controllers.Applicant;
import controllers.Opportunity;

public class MortgageApplicationPageSevenBService {
	String subForm = "Mortgage Application 7";
	MortgageApplicationPageService mortAppService = new MortgageApplicationPageService();
	CouchBaseService couchBaseService= null;
	CouchbaseServiceHelper couchbaseServiceHelper = new CouchbaseServiceHelper();
	PostGresDaoService postGresDaoService = new PostGresDaoService(); 
	Opportunity opportunity = null;
	Address address = null;
	SplitAddress addressSplit = new SplitAddress();
	public Opportunity updateOpportunityAddresses(DynamicForm dynamicForm,Session session) throws MortgageApplicationPageServiceException{
		Logger.info("Inside (.) updateOpportunityAddresses of MortgageApplicationPageSevenService");
		Map<String,String> readFormData = null;
		List<Address> listOfAddress = new ArrayList<Address>();
		List<Applicant> listOfApplicant = new ArrayList<Applicant>();
		Applicant applicant=new Applicant();
		Date movedInDate=null;
		Integer totalNoOfMonth=null;
		try{
		int crm_LeadId = mortAppService.parseLeadId(session); 
		opportunity = couchbaseServiceHelper.getCouhbaseDataByKey(crm_LeadId);
		readFormData = getFirstPriorAddressDataOfCoApplicant(dynamicForm);
		
		movedInDate = convertString2Date(readFormData.get("inputMovedIn1"));
		totalNoOfMonth = mortAppService.convertStringToInteger(readFormData.get("totalcurrentMonths"));
		
		if (readFormData.get("currentAddress1") != null) {
			listOfAddress.add(getAddress(readFormData.get("currentAddress1"), movedInDate, opportunity.getApplicants().get(1).getId()));	
		}
		Logger.debug("totalNoOfMonth "+totalNoOfMonth);
		if (totalNoOfMonth != null && totalNoOfMonth < 36) {
			readFormData = getSecPriorAddressDataOfCoApplicant(dynamicForm);
			movedInDate = convertString2Date(readFormData.get("inputMovedIn2"));
			totalNoOfMonth = mortAppService.convertStringToInteger(readFormData.get("totalpriorcurrentmonths2"));
			if(readFormData.get("priorAddress1") !=null){
				listOfAddress.add(getAddress(readFormData.get("priorAddress1"), movedInDate, opportunity.getApplicants().get(1).getId()));
			}
			Logger.debug("totalNoOfMonth "+totalNoOfMonth);
			if(totalNoOfMonth != null && totalNoOfMonth < 36){
				readFormData = getThirdPriorAddressDataOfCoApplicant(dynamicForm);
				movedInDate = convertString2Date(readFormData.get("inputMovedIn3"));
				if(readFormData.get("priorAddress2") !=null){
					listOfAddress.add(getAddress(readFormData.get("priorAddress2"), movedInDate, opportunity.getApplicants().get(1).getId()));
				}
			}
		}
		
		applicant.setListOfAddress(listOfAddress);
		listOfApplicant.add(applicant);
		applicant=new Applicant();
		listOfAddress = new ArrayList<controllers.Address>();
		
		applicant.setListOfAddress(listOfAddress);
		listOfApplicant.add(applicant);
		opportunity.setApplicants(listOfApplicant);
		opportunity.setPogressStatus(65);
		postGresDaoService.updateApplicantPage7(opportunity);
		couchBaseService.storeDataToCouchbase(MortgageApplicationConstants.MORTGAGE_APPLICATION_COUCHBASE_KEY+crm_LeadId, opportunity);
		}catch(MortgageApplicationPageServiceException mortAppException){
			throw new MortgageApplicationPageServiceException("Error when reading crm_LeadId Data from Couchbase / Invalid crm_LeadId ",mortAppException);
		}catch(PostGressDaoServiceException pgException){
			throw new MortgageApplicationPageServiceException(" Error when Updating Applicant Addresses into the PostgressDB ", pgException);
		}catch(CouchbaseDaoServiceException cbException){
			throw new MortgageApplicationPageServiceException("Error when Updating Applicant Addresses into the CouchbaseDB  ",cbException);
		}
		return opportunity;
	}
	
	private Address getAddress(String inputAddress,Date moved,int applicantId){
		Map<String,String> currentAddressSplit = addressSplit.getProperAddress(inputAddress);
		
		if(currentAddressSplit != null){
			address = new Address();
			address.setName(currentAddressSplit.get("address1"));
			address.setCity(currentAddressSplit.get("city"));
			address.setProvience(currentAddressSplit.get("Province"));
			address.setPostalCode(currentAddressSplit.get("postalcode"));
			address.setMovedIn(moved);
			address.setApplicant_id(applicantId);
		}
		return address;
	}
	
	public Date convertString2Date(String inputDateString){
		DateFormat df2 = new SimpleDateFormat("MM/dd/yyyy");
		Date movedIn1 = null;
		try{
			movedIn1 = df2.parse(inputDateString);
		}catch (ParseException e) {
			Logger.error("Error in parsing string to date");
		}
		return movedIn1;
	}
	
	/**
	 * Reading CoCoApplicant's 1st Prior Address Details
	 * @param dynamicForm
	 * @return
	 */
	private Map<String,String> getFirstPriorAddressDataOfCoApplicant(DynamicForm dynamicForm){
		Logger.info("inside (.) getFirstPriorAddressDataOfCoApplicant of MortgageApplicationPageSevenService ");
		Map<String,String> firstPriorAddressDataOfCoApplicant = new HashMap<String, String>();
		
		String coAppcurrentAddress = dynamicForm.get("CoCurrentAddress1");
		String coAppInputMovedIn1 = dynamicForm.get("CoMovedIn1");
		String coAppCurrentSumMonth = dynamicForm.get("coAppcurrentaddressmonthsum");
		String coAppTotalcurrentMonths = dynamicForm.get("coAppTotalcurrentMonths");
		
		Logger.debug("coAppcurrentAddress" + coAppcurrentAddress+ "\n coAppInputMovedIn1 " + 
		coAppInputMovedIn1+ "\n coAppCurrentSumMonth" + coAppCurrentSumMonth+"\n coAppTotalcurrentMonths "+coAppTotalcurrentMonths);

		firstPriorAddressDataOfCoApplicant.put("coAppcurrentAddress", coAppcurrentAddress);
		firstPriorAddressDataOfCoApplicant.put("coAppInputMovedIn1", coAppInputMovedIn1);
		firstPriorAddressDataOfCoApplicant.put("coAppCurrentSumMonth", coAppCurrentSumMonth);
		firstPriorAddressDataOfCoApplicant.put("coAppTotalcurrentMonths", coAppTotalcurrentMonths);
		return firstPriorAddressDataOfCoApplicant;
	}
	
	/**
	 * Reading CoCoApplicant's 2nd Prior Address Details
	 * @param dynamicForm
	 * @return
	 */
	private Map<String,String> getSecPriorAddressDataOfCoApplicant(DynamicForm dynamicForm){
		Logger.info("inside (.) getSecPriorAddressDataOfCoApplicant of MortgageApplicationPageSevenService ");
		Map<String,String> secPriorAddressDataOfCoApplicant = new HashMap<String, String>();
		
		String coAppPriorAddress1 = dynamicForm.get("CoCurrentAddress2");
		String coAppInputMovedIn2 = dynamicForm.get("CoMovedIn2");
		String coAppPriorSumMonth1 = dynamicForm.get("coAppPriorSumMonth1");
		String coAppTotalpriorcurrentmonths1 = dynamicForm.get("coAppTotalpriorcurrentmonths1");
		//calculated total month by the below variable
		String coApptotalpriorcurrentmonths2 = dynamicForm.get("coApptotalpriorcurrentmonths2");
		
		Logger.debug("coAppPriorAddress1" + coAppPriorAddress1+ "\n coAppInputMovedIn2 "  
				+coAppInputMovedIn2+ "\n coAppPriorSumMonth1" + coAppPriorSumMonth1
				+"\n coAppTotalpriorcurrentmonths1 "+coAppTotalpriorcurrentmonths1);

		secPriorAddressDataOfCoApplicant.put("coAppPriorAddress1", coAppPriorAddress1);
		secPriorAddressDataOfCoApplicant.put("coAppInputMovedIn2", coAppInputMovedIn2);
		secPriorAddressDataOfCoApplicant.put("coAppPriorSumMonth1", coAppPriorSumMonth1);
		secPriorAddressDataOfCoApplicant.put("coAppTotalpriorcurrentmonths1", coAppTotalpriorcurrentmonths1);
		secPriorAddressDataOfCoApplicant.put("coApptotalpriorcurrentmonths2",coApptotalpriorcurrentmonths2);
		return secPriorAddressDataOfCoApplicant;
	}
	
	/**
	 * Reading CoCoApplicant's 3rd Prior Address Details
	 * @param dynamicForm
	 * @return
	 */
	private Map<String,String> getThirdPriorAddressDataOfCoApplicant(DynamicForm dynamicForm){
		Logger.info("inside (.) getThirdPriorAddressDataOfCoApplicant of MortgageApplicationPageSevenService ");
		Map<String,String> secPriorAddressDataOfCoApplicant = new HashMap<String, String>();
		
		String coApppriorAddress2 = dynamicForm.get("CoCurrentAddress4");
		String coAppInputMovedIn3 = dynamicForm.get("CoMovedIn3");
		String coApppriorSumMonth2 = dynamicForm.get("coApppriorSumMonth2");
		
		Logger.debug("coApppriorAddress2" + coApppriorAddress2+ "\n coAppInputMovedIn3 "  
				+coAppInputMovedIn3+ "\n coApppriorSumMonth2" + coApppriorSumMonth2);

		secPriorAddressDataOfCoApplicant.put("coApppriorAddress2", coApppriorAddress2);
		secPriorAddressDataOfCoApplicant.put("coAppInputMovedIn3", coAppInputMovedIn3);
		secPriorAddressDataOfCoApplicant.put("coApppriorSumMonth2", coApppriorSumMonth2);
		
		return secPriorAddressDataOfCoApplicant;
	}
}