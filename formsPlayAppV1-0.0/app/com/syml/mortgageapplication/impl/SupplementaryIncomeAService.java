package com.syml.mortgageapplication.impl;
import java.util.List;

import play.Logger;
import play.data.DynamicForm;
import play.mvc.Http.Session;

import com.syml.couchbase.dao.CouchbaseDaoServiceException;
import com.syml.couchbase.dao.service.CouchBaseService;
import com.syml.couchbasehelper.CouchbaseServiceHelper;
import com.syml.hibernate.dao.PostGressDaoServiceException;
import com.syml.hibernate.service.PostGresDaoService;
import com.syml.mortgageapplication.MortgageApplicationConstants;

import controllers.Income;
import controllers.Opportunity;
/**
 * 
 * @author Bizruntime
 *
 */
public class SupplementaryIncomeAService {
	PostGresDaoService posService=null;
	CouchBaseService couchBaseService = null;
	
	MortgageApplicationPageService mortAppService = new MortgageApplicationPageService();
	CouchbaseServiceHelper couchbaseServiceHelper = new CouchbaseServiceHelper();
	Opportunity opportunity = null;
	Income income = null;
	
	/**
	 * To set the all the form data inside the opportunity and call to Postgress and cb for storing.
	 * @param dynamicForm
	 * @param session
	 * @param listOfIncomeType
	 * @throws MortgageApplicationPageServiceException
	 */
	public Opportunity updateApplicanntIncomeDetails(DynamicForm dynamicForm,Session session,List<String[]> listOfIncomeType) throws MortgageApplicationPageServiceException{
		posService= new PostGresDaoService();
		couchBaseService = new CouchBaseService();
		Logger.info("Inside (.) getListOfIncome of Page8ApplSupplementaryService");
		try {
			
			int crm_LeadId = mortAppService.parseLeadId(session);
			
			opportunity = couchbaseServiceHelper.getCouhbaseDataByKey(crm_LeadId);
			Logger.debug("Comming after couchbase invoke");
			Logger.debug("list of income size " + listOfIncomeType.size());

			setSupplementaryIncomeSrcInApplicant(listOfIncomeType.get(0), opportunity);

			if (setAddtionalIncomeSrcInApplicant(listOfIncomeType.get(1), opportunity)) {
				setAboutIncomeSrc(listOfIncomeType.get(2), opportunity);
				//TODO
				// Need to decide where and how to store   
				String otherIncomes = dynamicForm.get("otherIncomes");
				opportunity.getApplicants().get(0).setDescOtherIncome(otherIncomes);
				Logger.debug("otherIncomes description " + otherIncomes);
			}
			
			opportunity.setPogressStatus(70);
			if(listOfIncomeType!=null && listOfIncomeType.size()>0){
			posService.updateApplicantIncomePage8Or9(opportunity);
			}
			couchBaseService.storeDataToCouchbase(MortgageApplicationConstants.MORTGAGE_APPLICATION_COUCHBASE_KEY+crm_LeadId,opportunity);
			
		}catch(MortgageApplicationPageServiceException mortAppExcpetion){
			throw new MortgageApplicationPageServiceException("Error when reading crm_LeadId Data from Couchbase / Invalid crm_LeadId ",mortAppExcpetion);
		}catch (PostGressDaoServiceException pgException) {
			throw new MortgageApplicationPageServiceException("Error when inserting page2 Purchase Details into Postgress DB with given crm_LeadId"+opportunity.getId(), pgException);
		}catch(CouchbaseDaoServiceException cbException){
			Logger.error(">>>>>>>>>>>>>"+cbException);
			throw new MortgageApplicationPageServiceException("Error when inserting page2 Purchase Details into Couchbase DB with given crm_Leadid "+opportunity.getId(), cbException);
		}catch(Exception e){
			Logger.error("Parent >>>>>>>>>>>>>"+e);
		}
		return opportunity;
	}
	
	/**
	 * To the the list of income inside the appicant
	 * @param supplementaryIncomeSrc
	 * @param opportunity
	 */
	private void setSupplementaryIncomeSrcInApplicant(String[] supplementaryIncomeSrc,Opportunity opportunity){
		
		for (String paymtSrc : supplementaryIncomeSrc) {
			String[] splits = paymtSrc.replaceAll("^\\s*\\[|\\]|\\s*$\"",
					"").split("\\s*,\\s*");
			Logger.info("Inside for loop " + splits);
		for (String mixedString : splits) {
			String paymentSource = mixedString.replace("\"", "");
			if (paymentSource.equalsIgnoreCase("LivingAllow")){
				income = new Income();
				income.setTypeOfIncome("11");
				income.setSupplementary(true);
				income.setApplicantId(opportunity.getApplicants().get(0).getId());
				opportunity.getApplicants().get(0).getIncomes().add(income);
			}else if (paymentSource.equalsIgnoreCase("Bonus")){
				income = new Income();
				income.setTypeOfIncome("8");
				income.setSupplementary(true);
				income.setApplicantId(opportunity.getApplicants().get(0).getId());
				opportunity.getApplicants().get(0).getIncomes().add(income);	
			}else if (paymentSource.equalsIgnoreCase("Vehicle Allowance")){
				income = new Income();
				income.setTypeOfIncome("12");
				income.setSupplementary(true);
				income.setApplicantId(opportunity.getApplicants().get(0).getId());
				opportunity.getApplicants().get(0).getIncomes().add(income);
			}else if (paymentSource.equalsIgnoreCase("Commission")){
				income = new Income();
				income.setTypeOfIncome("4");
				income.setSupplementary(true);
				income.setApplicantId(opportunity.getApplicants().get(0).getId());
				opportunity.getApplicants().get(0).getIncomes().add(income);
			}
		}
		}
	}
	/**
	 * To set the Additional income source in applicant
	 * @param addtionalIncomeSrc
	 * @param opportunity
	 * @return
	 */
	private Boolean setAddtionalIncomeSrcInApplicant(String[] addtionalIncomeSrc,Opportunity opportunity){
		Boolean isOther=false;
		for (String paymtSrc : addtionalIncomeSrc) {
			String[] splits = paymtSrc.replaceAll("^\\s*\\[|\\]|\\s*$\"",
					"").split("\\s*,\\s*");
			Logger.info("Inside for loop " + splits);
		for (String mixedString : splits) {
			String paymentSource = mixedString.replace("\"", "");
			if (paymentSource.equalsIgnoreCase("Investments")){
				income = new Income();
				income.setTypeOfIncome("Investments");
				income.setSupplementary(true);
				income.setApplicantId(opportunity.getApplicants().get(0).getId());
				opportunity.getApplicants().get(0).getIncomes().add(income);
			}else if (paymentSource.equalsIgnoreCase("Retirement")){
				income = new Income();
				income.setTypeOfIncome("3");
				income.setSupplementary(true);
				income.setApplicantId(opportunity.getApplicants().get(0).getId());
				opportunity.getApplicants().get(0).getIncomes().add(income);
			}else if (paymentSource.equalsIgnoreCase("Maternity")){
				income = new Income();
				income.setTypeOfIncome("14");
				income.setSupplementary(true);
				income.setApplicantId(opportunity.getApplicants().get(0).getId());
				opportunity.getApplicants().get(0).getIncomes().add(income);
			}else if (paymentSource.equalsIgnoreCase("Other")){
				income = new Income();
				income.setTypeOfIncome("13");
				income.setSupplementary(true);
				income.setApplicantId(opportunity.getApplicants().get(0).getId());
				opportunity.getApplicants().get(0).getIncomes().add(income);
				isOther = true;
				}
			}
		}
		return isOther;
	}
	
	/**
	 * To set the About other income source inside applicant
	 * @param aboutIncomeSrc
	 * @param opportunity
	 */
	private void setAboutIncomeSrc(String[] aboutIncomeSrc,Opportunity opportunity){
		for (String paymtSrc : aboutIncomeSrc) {
			String[] splits = paymtSrc.replaceAll("^\\s*\\[|\\]|\\s*$\"",
					"").split("\\s*,\\s*");
			Logger.info("Inside for loop " + splits);
		for (String mixedString : splits) {
			String paymentSource = mixedString.replace("\"", "");
			if (paymentSource.equalsIgnoreCase("Pension")){
				income = new Income();
				income.setTypeOfIncome("6");
				income.setApplicantId(opportunity.getApplicants().get(0).getId());
				income.setSupplementary(true);
				opportunity.getApplicants().get(0).getIncomes().add(income);
			}else if (paymentSource.equalsIgnoreCase("Disability")){
				income = new Income();
				income.setTypeOfIncome("Disability");
				income.setApplicantId(opportunity.getApplicants().get(0).getId());
				income.setSupplementary(true);
				opportunity.getApplicants().get(0).getIncomes().add(income);
				}
			}
		}
	}
	
	/**
	 * The below written code is for back button functionality of this current page. 
	 * @throws MortgageApplicationPageServiceException
	 */
	public void removeData(Opportunity opportunity) throws MortgageApplicationPageServiceException{
		posService = new PostGresDaoService();
		couchBaseService = new CouchBaseService();
		try {
			opportunity.getApplicants().get(0).getIncomes().add(null);
			
			posService.updateApplicantIncomePage8Or9(opportunity);
			couchBaseService.storeDataToCouchbase(MortgageApplicationConstants.MORTGAGE_APPLICATION_COUCHBASE_KEY+opportunity.getId(), opportunity);
		} catch (PostGressDaoServiceException e) {
			throw new MortgageApplicationPageServiceException("",e);
		}catch(CouchbaseDaoServiceException CB){
			throw new MortgageApplicationPageServiceException();
		}
		
	}
	
}