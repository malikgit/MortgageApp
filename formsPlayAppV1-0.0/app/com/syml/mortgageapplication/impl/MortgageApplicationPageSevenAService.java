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
import controllers.ApplicantAddressParameter7;
import controllers.Opportunity;

public class MortgageApplicationPageSevenAService {
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
		readFormData = getFirstPriorAddressDataOfApplicant(dynamicForm);
		
		movedInDate = convertString2Date(readFormData.get("inputMovedIn1"));
		totalNoOfMonth = mortAppService.convertStringToInteger(readFormData.get("totalcurrentMonths"));
		
		if (readFormData.get("currentAddress1") != null) {
			listOfAddress.add(getAddress(readFormData.get("currentAddress1"), movedInDate, opportunity.getApplicants().get(0).getId()));	
		}
		Logger.debug("totalNoOfMonth "+totalNoOfMonth);
		if (totalNoOfMonth != null && totalNoOfMonth < 36) {
			readFormData = getSecPriorAddressDataOfApplicant(dynamicForm);
			movedInDate = convertString2Date(readFormData.get("inputMovedIn2"));
			totalNoOfMonth = mortAppService.convertStringToInteger(readFormData.get("totalpriorcurrentmonths2"));
			if(readFormData.get("priorAddress1") !=null){
				listOfAddress.add(getAddress(readFormData.get("priorAddress1"), movedInDate, opportunity.getApplicants().get(0).getId()));
			}
			Logger.debug("totalNoOfMonth "+totalNoOfMonth);
			if(totalNoOfMonth != null && totalNoOfMonth < 36){
				readFormData = getThirdPriorAddressDataOfApplicant(dynamicForm);
				movedInDate = convertString2Date(readFormData.get("inputMovedIn3"));
				if(readFormData.get("priorAddress2") !=null){
					listOfAddress.add(getAddress(readFormData.get("priorAddress2"), movedInDate, opportunity.getApplicants().get(0).getId()));
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
	 * Reading Applicant's First Prior Address Details
	 * @param dynamicForm
	 * @return
	 */
	private Map<String,String> getFirstPriorAddressDataOfApplicant(DynamicForm dynamicForm){
		Logger.info("inside (.) getFirstPriorAddressDataOfApplicant of MortgageApplicationPageSevenService ");
		Map<String,String> firstPriorAddressDataOfApplicant = new HashMap<String, String>();
		String currentAddress = dynamicForm.get("currentAddress1");
		String inputMovedIn1 = dynamicForm.get("movedIn1");
		String currentSumMonth = dynamicForm.get("currentaddressmonthsum");
		String totalcurrentMonths = dynamicForm.get("totalcurrentmonths");
		
		Logger.debug("currentAddress" + currentAddress+ "\n input MovedIn1 " + inputMovedIn1
				+ "\n After string-to-date movedIn1 " + inputMovedIn1
				+ "\n currentSumMonth" + currentSumMonth
				+ "\n totalcurrentMonths" + totalcurrentMonths);
		
		firstPriorAddressDataOfApplicant.put("currentAddress1", currentAddress);
		firstPriorAddressDataOfApplicant.put("inputMovedIn1", inputMovedIn1);
		firstPriorAddressDataOfApplicant.put("currentSumMonth", currentSumMonth);
		firstPriorAddressDataOfApplicant.put("totalcurrentMonths", totalcurrentMonths);
		return firstPriorAddressDataOfApplicant;
	}
	/**
	 * Reading Applicant's 2nd Prior Address Details
	 * @param dynamicForm
	 * @return
	 */
	private Map<String,String> getSecPriorAddressDataOfApplicant(DynamicForm dynamicForm){
		Logger.info("inside (.) getSecPriorAddressDataOfApplicant of MortgageApplicationPageSevenService ");
		Map<String,String> secPriorAddressDataOfApplicant = new HashMap<String, String>();
		String priorAddress1 = dynamicForm.get("currentAddress2");
		String inputMovedIn2 = dynamicForm.get("movedIn2");
		String priorSumMonth1 = dynamicForm.get("priormonthsum1");
		String totalpriorcurrentmonths1 = dynamicForm.get("totalpriormonths1");
		String totalpriorcurrentmonths2 = dynamicForm.get("totalpriormonths2");
		
		Logger.debug("currentAddress" + priorAddress1+ "\n input MovedIn1 " + inputMovedIn2
				+ "\n After string-to-date movedIn1 " + priorSumMonth1
				+ "\n currentSumMonth" + totalpriorcurrentmonths1
				+ "\n totalcurrentMonths" + totalpriorcurrentmonths2);
		
		secPriorAddressDataOfApplicant.put("priorAddress1", priorAddress1);
		secPriorAddressDataOfApplicant.put("inputMovedIn2", inputMovedIn2);
		secPriorAddressDataOfApplicant.put("priorSumMonth1", priorSumMonth1);
		secPriorAddressDataOfApplicant.put("totalpriorcurrentmonths1", totalpriorcurrentmonths1);
		secPriorAddressDataOfApplicant.put("totalpriorcurrentmonths2", totalpriorcurrentmonths2);
		return secPriorAddressDataOfApplicant;
	}
	
	/**
	 * Reading Applicant's 3rd Prior Address Details
	 * @param dynamicForm
	 * @return
	 */
	private Map<String,String> getThirdPriorAddressDataOfApplicant(DynamicForm dynamicForm){
		Logger.info("inside (.) getThirdPriorAddressDataOfApplicant of MortgageApplicationPageSevenService ");
		Map<String,String> thirdPriorAddressDataOfApplicant = new HashMap<String, String>();
		String priorAddress2 = dynamicForm.get("currentAddress3");
		String inputMovedIn3 = dynamicForm.get("movedIn3");
		String priorSumMonth2 = dynamicForm.get("priormonthsum2");
		
		Logger.debug("priorAddress2" + priorAddress2+ "\n inputMovedIn3 " + inputMovedIn3+ "\n priorSumMonth2" + priorSumMonth2);

		thirdPriorAddressDataOfApplicant.put("priorAddress2", priorAddress2);
		thirdPriorAddressDataOfApplicant.put("inputMovedIn3", inputMovedIn3);
		thirdPriorAddressDataOfApplicant.put("priorSumMonth2", priorSumMonth2);
		return thirdPriorAddressDataOfApplicant;
	}
	
	/*The below code is for the back button fuctinolity */
	/**
	 * To set the address details of Applicant into the pojo for display
	 * @param opportunity
	 * @return
	 */
	public ApplicantAddressParameter7 getApplicantAddresses(Opportunity opportunity){
		
		Logger.info("Inside getApplicantAddresses (.) of MortageAppPage7 ");
		ApplicantAddressParameter7 applAddrPrm = new ApplicantAddressParameter7();
		Applicant applicant = opportunity.getApplicants().get(0);
		List<Address> listOfAddress = applicant.getListOfAddress();
		
		for(Address address : listOfAddress){
			Logger.debug("address "+address.getName()+"\n City= "+address.getCity()+"\n Provience= "+address.getProvience()+"\n PostalCode="+address.getPostalCode());
		}
		if(listOfAddress != null && listOfAddress.size() >0){
			applAddrPrm.setApplicantName(applicant.getApplicant_name());
			applAddrPrm.setAdditionalApplicant(opportunity.getIsAdditionalApplicantExist());
			applAddrPrm.setApplicantCurrentAddress(listOfAddress.get(0).getName()+","+listOfAddress.get(0).getCity());
			applAddrPrm.setApplicantMovedIn1(listOfAddress.get(0).getMovedIn()+"");
			
//			private String applicantCurrentSumMonth = "";
//			private String applicantTotalcurrentMonths = "";
		}
		return applAddrPrm;
	}
		
}