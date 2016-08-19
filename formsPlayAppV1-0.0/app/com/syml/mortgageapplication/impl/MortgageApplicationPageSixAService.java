package com.syml.mortgageapplication.impl;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import play.Logger;
import play.data.DynamicForm;

import com.syml.couchbase.dao.CouchbaseDaoServiceException;
import com.syml.couchbase.dao.service.CouchBaseService;
import com.syml.hibernate.dao.PostGressDaoServiceException;
import com.syml.hibernate.service.PostGresDaoService;
import com.syml.mortgageapplication.MortgageApplicationConstants;

import controllers.Applicant;
import controllers.Opportunity;
import controllers.PersonalInfo;

public class MortgageApplicationPageSixAService {
	
	String formType = "Mortgage Application";
	String subForm = "Mortgage Application 6";
	
	PostGresDaoService posService=new PostGresDaoService();
	CouchBaseService couchBaseService=new CouchBaseService();
	
	/**
	 * To Map Page 6 values(applicant and co-applicant details) to Opportunity Pojo
	 * @param opportunity
	 * @param dynamicForm
	 * @return Opportunity
	 */
	public Opportunity loadFormData(Opportunity opportunity,DynamicForm dynamicForm){
		
																	 
		opportunity.getApplicants().get(0).setCell( dynamicForm.get("applWorkPhone"));
		String inputBirthDay = dynamicForm.get("applBirthday");
		String insurance = dynamicForm.get("applInsurNum");
		
		DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
		Date birthday = null;
		try {
			birthday = df.parse(inputBirthDay);
		} catch (ParseException e) {
			Logger.error("Error in parsing string to date",e);
		}
		opportunity.getApplicants().get(0).setDob(birthday);
		opportunity.getApplicants().get(0).setSin(insurance);
		
		//depandent we need to add
		
		if(opportunity.getIsAdditionalApplicantExist().equalsIgnoreCase("Yes")){
			opportunity.getApplicants().get(1).setCell( dynamicForm.get("coApplWorkPhone"));
			inputBirthDay = dynamicForm.get("coApplBirthday");
			String CoAppinsurance = dynamicForm.get("coApplInsurNum");
			 birthday = null;
			try {
				birthday = df.parse(inputBirthDay);
			} catch (ParseException e) {
				Logger.error("Error in parsing string to date",e);
			}
			opportunity.getApplicants().get(1).setDob(birthday);
			opportunity.getApplicants().get(1).setSin(CoAppinsurance);
		}

		opportunity.setPogressStatus(55);
		
		return opportunity;
		
	}

	/**
	 * To update opportunity in Db  and update opportunity pojo to couchbase  
	 * @param opportunity
	 * @return Opportunity
	 * @throws MortgageApplicationPageServiceException
	 */
	public Opportunity updateOpportunity(Opportunity opportunity) throws MortgageApplicationPageServiceException{
		try {
			posService.updateApplicantPage6(opportunity);
			couchBaseService.storeDataToCouchbase(MortgageApplicationConstants.MORTGAGE_APPLICATION_COUCHBASE_KEY+opportunity.getId(), opportunity);

		} catch (PostGressDaoServiceException e) {
			throw new MortgageApplicationPageServiceException("Error In updating Applicant Details Into Db ",e);
		} catch (CouchbaseDaoServiceException e) {
			throw new MortgageApplicationPageServiceException("Error In updating Applicant Details Into Couchbase ",e);

		}
		return opportunity;
		
	}
	
	/**
	 * To set the data BioInfo for Page6a back button display
	 * @param opportunity
	 * @return
	 */
	public PersonalInfo getPersonalInfo(Opportunity opportunity){
		
		Logger.info("Inside (.)  getPersonalInfo of MortgageApplicationPageSixAService ");
		PersonalInfo personalInfo = new PersonalInfo();
		Applicant applicant = opportunity.getApplicants().get(0);
		personalInfo.setWorkPhone(applicant.getWork());
		personalInfo.setBirthDay(applicant.getDob()+"");
		personalInfo.setCoSocialInsurance(applicant.getSin());
		personalInfo.setDependents(applicant.getDependant());
	return personalInfo;
	}
}
