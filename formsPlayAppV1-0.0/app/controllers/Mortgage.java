package controllers;

import static play.data.Form.form;

import java.util.ArrayList;
import java.util.List;

import play.Logger;
import play.data.DynamicForm;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Http.Session;
import play.mvc.Result;
import views.html.completed_application;
import views.html.expired_page;
import views.html.leadsucess;
import views.html.mortgagePage1;
import views.html.mortgagePage10Assets;
import views.html.mortgagePage11Properties;
import views.html.mortgagePage12Disclose;
import views.html.mortgagePage1a;
import views.html.mortgagePage1b;
import views.html.mortgagePage2Pre;
import views.html.mortgagePage2Pur;
import views.html.mortgagePage2Ref;
import views.html.mortgagePage3;
import views.html.mortgagePage4;
import views.html.mortgagePage5a;
import views.html.mortgagePage5b;
import views.html.mortgagePage6;
import views.html.mortgagePage6a;
import views.html.mortgagePage6b;
import views.html.mortgagePage7Address;
import views.html.mortgagePage7a;
import views.html.mortgagePage7b;
import views.html.mortgagePage8;
import views.html.mortgagePage9;
import views.html.privacypolicy;
import views.html.supplementary_income;

import com.syml.Constants;
import com.syml.couchbase.dao.service.CouchBaseService;
import com.syml.couchbasehelper.CouchbaseServiceHelper;
import com.syml.hibernate.dao.PostGressDaoServiceException;
import com.syml.mortgageapplication.MortgageApplicationConstants;
import com.syml.mortgageapplication.backbuttonimpl.MortgageApplication11BackButtonService;
import com.syml.mortgageapplication.backbuttonimpl.MortgageApplicationPage10BackButtonService;
import com.syml.mortgageapplication.backbuttonimpl.MortgageApplicationPageOneBBackButtonService;
import com.syml.mortgageapplication.backbuttonimpl.MortgageApplicationPageOneBackButtonService;
import com.syml.mortgageapplication.impl.MortgageApplicationPage2PreService;
import com.syml.mortgageapplication.impl.MortgageApplicationPage2PurService;
import com.syml.mortgageapplication.impl.MortgageApplicationPage2RefService;
import com.syml.mortgageapplication.impl.MortgageApplicationPage2Service;
import com.syml.mortgageapplication.impl.MortgageApplicationPageElevenService;
import com.syml.mortgageapplication.impl.MortgageApplicationPageFiveAService;
import com.syml.mortgageapplication.impl.MortgageApplicationPageFiveBService;
import com.syml.mortgageapplication.impl.MortgageApplicationPageFourService;
import com.syml.mortgageapplication.impl.MortgageApplicationPageOneAService;
import com.syml.mortgageapplication.impl.MortgageApplicationPageOneBService;
import com.syml.mortgageapplication.impl.MortgageApplicationPageService;
import com.syml.mortgageapplication.impl.MortgageApplicationPageServiceException;
import com.syml.mortgageapplication.impl.MortgageApplicationPageSevenAService;
import com.syml.mortgageapplication.impl.MortgageApplicationPageSevenBService;
import com.syml.mortgageapplication.impl.MortgageApplicationPageSevenService;
import com.syml.mortgageapplication.impl.MortgageApplicationPageSixAService;
import com.syml.mortgageapplication.impl.MortgageApplicationPageSixBService;
import com.syml.mortgageapplication.impl.MortgageApplicationPageSixService;
import com.syml.mortgageapplication.impl.MortgageApplicationPageTenService;
import com.syml.mortgageapplication.impl.MortgageApplicationPageThreeService;
import com.syml.mortgageapplication.impl.MortgageApplicationPageTwelevService;
import com.syml.mortgageapplication.impl.SupplementaryIncomeAService;

public class Mortgage extends Controller {

	public static Result mortgageApplication() {
		
		Session session = Http.Context.current().session();
		MortgageApplicationPageService maoPageService = new MortgageApplicationPageService();
		Opportunity opportunity = new Opportunity();
		int pogressStatus = 0;
		try {
			opportunity.setId(Integer.parseInt(request().getQueryString(
					"crmLeadId")));
			opportunity.setReferralSourceEmail(request().getQueryString(
					"referrerEmail"));
			opportunity.setReferralSourceName(request().getQueryString(
					"referralName"));
			opportunity.setRole(request().getQueryString("role"));
			opportunity.setExpireDate(request().getQueryString("expireDate"));
			boolean isExpired = maoPageService.checkExpireDate(opportunity);
			Logger.debug("crmLead id is  ------ " + opportunity.getId()
					+ "\n Referral Email id is  ------  "
					+ opportunity.getReferralSourceEmail() + "\n referralName "
					+ opportunity.getReferralSourceName() + " \n ExpirDate = "
					+ opportunity.getExpireDate());

			if (opportunity.getId() != 0
					&& opportunity.getReferralSourceEmail() != null
					&& opportunity.getReferralSourceName() != null) {
				session.put("crmLeadId", opportunity.getId() + "");
				session.put("referralEmail",
						opportunity.getReferralSourceEmail());
				session.put("referralName", opportunity.getReferralSourceName());
			}
			String devicType = request().getHeader("User-Agent");
			opportunity = maoPageService
					.checkDeviceType(devicType, opportunity);

			Opportunity oldOpportunity = maoPageService
					.checkOpporunityExist(opportunity);
			Applicant applicant = new Applicant();
			Logger.debug("Old Opporunity Details " + oldOpportunity);
			boolean isMobile = opportunity.getForms().isMobile();
			//boolean isMobile = true;
			Logger.info("Is it mobileDevice =" + isMobile);
			if (isMobile) {
				session.put("isMobile", "isMobile");
				opportunity.getForms().setMobile(isMobile);
			} else {
				session.put("isMobile", "Desktop");

			}
			if (oldOpportunity != null)
				pogressStatus = oldOpportunity.getPogressStatus();

			if (oldOpportunity == null
					&& (opportunity.getRole().equalsIgnoreCase("B") || opportunity
							.getRole().equalsIgnoreCase("C") && isExpired)) {

				new CouchBaseService()
						.storeDataToCouchbase(
								MortgageApplicationConstants.MORTGAGE_APPLICATION_COUCHBASE_KEY
										+ opportunity.getId(), opportunity);

				if (!isMobile)
					return ok(mortgagePage1.render(opportunity, applicant,
							applicant));
				else
					return ok(mortgagePage1a.render(opportunity, applicant));
			} else {
				Logger.debug("Old Opporunity Details Exsting  ");
				Logger.debug("pogressStatus " + pogressStatus);
				if (opportunity.getRole().equalsIgnoreCase("B")
						|| opportunity.getRole().equalsIgnoreCase("C")
						&& isExpired) {
					Applicant applicantDetails = null;

					if (pogressStatus == 0) {
						if (!isMobile)
							return ok(mortgagePage1.render(opportunity,
									applicant, applicant));
						else
							return ok(mortgagePage1a.render(opportunity,
									applicant));
					} else if (pogressStatus < 99) {
						applicantDetails = oldOpportunity.getApplicants()
								.get(0);
						session.put("leadingGoal",
								oldOpportunity.getLendingGoal());
						session.put("applicantID", applicantDetails.getId()
								+ "");
						session.put("additionalApplicants",
								oldOpportunity.getIsAdditionalApplicantExist());

						session.put("applicantFirstName",
								applicantDetails.getApplicant_name());
						session.put("applicantLasttName",
								applicantDetails.getApplicant_last_name());
						session.put("applicantEmail",
								applicantDetails.getEmail_personal());

						if (oldOpportunity.getApplicants() != null
								&& oldOpportunity.getApplicants().size() > 1
								&& oldOpportunity
										.getIsAdditionalApplicantExist()
										.equalsIgnoreCase("Yes")) {
							applicantDetails = oldOpportunity.getApplicants()
									.get(1);
							session.put("co_applicantFirstName",
									applicantDetails.getApplicant_name());
							session.put("co_applicantLastName",
									applicantDetails.getApplicant_last_name());
							session.put("co_applicantEmail",
									applicantDetails.getEmail_personal());
							session.put("applicantID2",
									applicantDetails.getId() + "");
						}
						String mlsListed = oldOpportunity.getMls();
						 
						Logger.info("ismobile before calling existing user Mathod "
								+ isMobile);
						Logger.debug("mlsListed >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>  "+mlsListed);
						return existingUser(pogressStatus, isMobile, mlsListed);
					} else
						return ok(completed_application
								.render("Your Application is Completed . Please email Visdom at support@visdom.ca if you requirement continued access."));

				} else
					return ok(expired_page
							.render("Your Application is expired. Try Again"));
			}
		} catch (Exception e) {
			Logger.error(
					"Error in mortgageApplication method of Mortgage class : ",
					e);
			return ok("Some thing went wrong when rendering mortgage First page");
		}
	}

	public static Result existingUser(int progressStatus, boolean isMobile,
			String mlsListed) {
		
		//Logger.info("Inside (.) existingUser of Mortgage Controller.");
		Session session = Http.Context.current().session();
		ApplicantAddressParameter7 appAddressParam = null;
		PersonalInfo personalInfo = null;
		TotalAssets totalAssets = new TotalAssets();
		totalAssets.setVehicle(new ArrayList<Assetes>());
		totalAssets.setBankAccount(new ArrayList<Assetes>());
		totalAssets.setRrsp(new ArrayList<Assetes>());
		totalAssets.setInvestments(new ArrayList<Assetes>());
		totalAssets.setOthers(new ArrayList<Assetes>());
		CoApplicantAddressParameter7 coAppAddressParam = null;
		String leadingGoal = "";
		String applicantName = "";
		String coApplicantName = "";
		String additionalApplicant = "";
		LendingTerm lendingTerm = new LendingTerm();

		try {

			leadingGoal = (String) session.get("leadingGoal");
			applicantName = (String) session.get("applicantFirstName");
			
			coApplicantName = (String) session.get("co_applicantFirstName");
			additionalApplicant = (String) session.get("additionalApplicants");
			
			
			if(coApplicantName == null)
				coApplicantName = "";
				
			
		} catch (Exception e) {
			Logger.error(
					"Error when reading applicantName and coApplicant name from couchbase ",
					e);
		}
		Logger.info("lending goal =" + leadingGoal);
		switch (progressStatus) {
		case 10:
			if (leadingGoal
					.equalsIgnoreCase(MortgageApplicationConstants.PURCHASE_LENDING_GOAL)) {

				return ok(mortgagePage2Pur.render(lendingTerm, ""));
			} else if (leadingGoal
					.equalsIgnoreCase(MortgageApplicationConstants.REFINANCE_LENDING_GOAL)) {

				return ok(mortgagePage2Ref.render(lendingTerm, ""));
			} else {
				return ok(mortgagePage2Pre.render("", lendingTerm));

			}

		case 20:

			if (mlsListed != null && mlsListed.equalsIgnoreCase("PrivateSale"))
				return ok(mortgagePage3.render("", "", "", "", "", "", "", "",
						""));
			else
				return ok(mortgagePage4.render("", "", "", "", ""));
		case 30:
			return ok(mortgagePage4.render("", "", "", "", ""));
		case 35:
			return ok(mortgagePage5a.render("", "", "", "", "", ""));
		case 40:
			return ok(mortgagePage5b.render("", "", "", "", ""));
		case 45:
			if (!isMobile)
				return ok(mortgagePage6.render(additionalApplicant,
						applicantName, coApplicantName, "", "", "", "", "", "",
						"", "", "", "", "", "", "", "", "", "", "", ""));
			else {
				personalInfo = new PersonalInfo();
				personalInfo.setAdditionalApplicant(additionalApplicant);
				personalInfo.setApplicantName(applicantName);
				return ok(mortgagePage6a.render(personalInfo));
			}
		case 50:
			if (!isMobile)
				return ok(mortgagePage6.render(additionalApplicant,
						applicantName, coApplicantName, "", "", "", "", "", "",
						"", "", "", "", "", "", "", "", "", "", "", ""));
			else {
				personalInfo = new PersonalInfo();
				personalInfo.setAdditionalApplicant(additionalApplicant);
				personalInfo.setCoApplicantName(coApplicantName);
				return ok(mortgagePage6b.render(new PersonalInfo()));
			}
		case 55:
			if (!isMobile)
				return ok(mortgagePage7Address.render(additionalApplicant,
						applicantName, coApplicantName,
						new ApplicantAddressParameter7(),
						new CoApplicantAddressParameter7()));
			else {
				appAddressParam = new ApplicantAddressParameter7();
				appAddressParam.setAdditionalApplicant(additionalApplicant);
				appAddressParam.setApplicantName(applicantName);
				return ok(mortgagePage7a.render(appAddressParam));
			}
		case 60:
			if (!isMobile)
				return ok(mortgagePage7Address.render(additionalApplicant,
						applicantName, coApplicantName,
						new ApplicantAddressParameter7(),
						new CoApplicantAddressParameter7()));
			else {
				coAppAddressParam = new CoApplicantAddressParameter7();
				coAppAddressParam.setCoApplicantName(coApplicantName);
				return ok(mortgagePage7b.render(coAppAddressParam));
			}
		case 65:
			return ok(supplementary_income.render(new Applicant()));
		case 70:
			if (additionalApplicant != null
					&& additionalApplicant.equalsIgnoreCase("yes"))
				return ok(supplementary_income.render(new Applicant()));
			else
				return ok(mortgagePage10Assets.render("", applicantName,
						coApplicantName, totalAssets));
		case 75:

			return ok(mortgagePage10Assets.render("", applicantName,
					coApplicantName, totalAssets));
		case 85:
			
			List<Property> listOfProperty = new ArrayList<Property>();
			listOfProperty.add(new Property());
			return ok(mortgagePage11Properties.render("", applicantName,
					coApplicantName, listOfProperty));
		case 95:
			return ok(mortgagePage12Disclose.render("", additionalApplicant,
					new Integer(4), applicantName, coApplicantName));
		default:
			return ok("Previous Data not found please fill newly application .");
		}
	}

	public static Result mortgagePage1a() {
	
		Logger.debug("Inside (.) mortgagePage1a Controller Method ");
		CouchbaseServiceHelper couHelper = new CouchbaseServiceHelper();
		MortgageApplicationPageService mService = new MortgageApplicationPageService();
		MortgageApplicationPageOneAService moPageOneAService = new MortgageApplicationPageOneAService();
		DynamicForm dynamicForm = form().bindFromRequest();

		try {
			Session session = Http.Context.current().session();

			Opportunity opportunity = couHelper.getCouhbaseDataByKey(mService
					.parseLeadId(session));

			moPageOneAService.loadFormData(opportunity, dynamicForm);
			moPageOneAService.createApplicants(opportunity);
			String lendingGoal = opportunity.getLendingGoal();
			String additionalApplicants = opportunity
					.getIsAdditionalApplicantExist();
			if (additionalApplicants != null
					&& additionalApplicants.equalsIgnoreCase("yes"))
				return ok(mortgagePage1b.render(opportunity, new Applicant()));
			else if (lendingGoal != null
					&& lendingGoal
							.equalsIgnoreCase(MortgageApplicationConstants.PRE_APPROVAL_LENDING_GOAL))
				return ok(mortgagePage2Pre.render("uniid", new LendingTerm()));
			else if (lendingGoal != null
					&& lendingGoal
							.equalsIgnoreCase(MortgageApplicationConstants.PURCHASE_LENDING_GOAL))
				return ok(mortgagePage2Pur.render(new LendingTerm(),
						additionalApplicants));
			else if (lendingGoal != null
					&& lendingGoal
							.equalsIgnoreCase(MortgageApplicationConstants.REFINANCE_LENDING_GOAL))
				return ok(mortgagePage2Ref.render(new LendingTerm(),
						additionalApplicants));
			else

				return ok("Demo return, it should be MortgageApplicationSucess.jsp");

		} catch (MortgageApplicationPageServiceException e) {
			Logger.error("Error in Processing MortgagePage 1a : ", e);
			return ok(leadsucess
					.render(Constants.COUCHBASE_CONNECTION_FAILED_MESSAGE));

		}
	}

	public static Result mortgagePage1b() {
		Logger.info("Inside mortgagePage1b");

		DynamicForm dynamicForm = form().bindFromRequest();
		MortgageApplicationPageService mService = new MortgageApplicationPageService();
		CouchbaseServiceHelper couchbaseServiceHelper = new CouchbaseServiceHelper();
		MortgageApplicationPageOneBService mService2 = new MortgageApplicationPageOneBService();
		try {
			Session session = Http.Context.current().session();
			Opportunity opportunity = couchbaseServiceHelper
					.getCouhbaseDataByKey(mService.parseLeadId(session));
			mService2.loadFormData(opportunity, dynamicForm);
			String lendingGoal = opportunity.getLendingGoal();
			mService2.createCoApplicant(opportunity);
			String additionalApplicants = opportunity
					.getIsAdditionalApplicantExist();
			if (lendingGoal != null
					&& lendingGoal
							.equalsIgnoreCase(MortgageApplicationConstants.PRE_APPROVAL_LENDING_GOAL))
				return ok(mortgagePage2Pre.render("uniid", new LendingTerm()));
			else if (lendingGoal != null
					&& lendingGoal
							.equalsIgnoreCase(MortgageApplicationConstants.PURCHASE_LENDING_GOAL))
				return ok(mortgagePage2Pur.render(new LendingTerm(),
						additionalApplicants));
			else if (lendingGoal != null
					&& lendingGoal
							.equalsIgnoreCase(MortgageApplicationConstants.REFINANCE_LENDING_GOAL))
				return ok(mortgagePage2Ref.render(new LendingTerm(),
						additionalApplicants));

			return ok("Demo return, it should be MortgageApplicationSucess.jsp");

		} catch (MortgageApplicationPageServiceException e) {
			Logger.error("error in mortgagePage1b : ", e);
			return ok(leadsucess
					.render(Constants.COUCHBASE_CONNECTION_FAILED_MESSAGE));
		}
	}

	public static Result mortgagePage2() {

		Logger.info("Inside mortgagePage2 ");
		MortgageApplicationPage2Service p2Service = new MortgageApplicationPage2Service();
		DynamicForm dynamicForm = form().bindFromRequest();
		Session session = Http.Context.current().session();
		try {
			Opportunity opportunity = p2Service.updatePage2Details(dynamicForm,
					session);
			Logger.info("Insert of page Two details sucess Full " + opportunity);
			if (opportunity.getLendingGoal() != null
					&& opportunity.getLendingGoal().equalsIgnoreCase(MortgageApplicationConstants.PRE_APPROVAL_LENDING_GOAL))
				return ok(mortgagePage2Pre.render("uniid", new LendingTerm()));
			else if (opportunity.getLendingGoal() != null
					&& opportunity.getLendingGoal().equalsIgnoreCase(MortgageApplicationConstants.PURCHASE_LENDING_GOAL))
				return ok(mortgagePage2Pur.render(new LendingTerm(),
						opportunity.getIsAdditionalApplicantExist()));
			else
				return ok(mortgagePage2Ref.render(new LendingTerm(),opportunity.getIsAdditionalApplicantExist()));
		} catch (MortgageApplicationPageServiceException mortAppException) {
			Logger.error("Error when creating new Applicants", mortAppException);
			return ok(leadsucess
					.render(Constants.COUCHBASE_CONNECTION_FAILED_MESSAGE));
		}
	}

	public static Result mortgagePage2Pre() {

		Logger.debug("Inside mortgagePage2Pre");
		MortgageApplicationPage2PreService mortAppPage2PreService = null;
		DynamicForm dynamicForm = form().bindFromRequest();
		Session session = Http.Context.current().session();
		String[] paymtSrcList = request().body().asFormUrlEncoded()
				.get("paymtSrcList[]");
		Opportunity opportunity = null;
		try {
			mortAppPage2PreService = new MortgageApplicationPage2PreService();
			opportunity = mortAppPage2PreService.updatePage2PreDetails(dynamicForm, session, paymtSrcList);
			return ok(mortgagePage4
					.render(opportunity.getIsAdditionalApplicantExist(), "",
							"", "", ""));
		} catch (MortgageApplicationPageServiceException mortAppException) {
			Logger.error("Error when update Pre Approval details ",
					mortAppException);
			return ok(leadsucess
					.render(Constants.COUCHBASE_CONNECTION_FAILED_MESSAGE));
		}
	}

	public static Result mortgagePage2Pur() {

		Logger.info("Inside mortgagePage2 Purchase");
		MortgageApplicationPage2PurService mortAppPage2PreService = null;
		DynamicForm dynamicForm = form().bindFromRequest();
		Session session = Http.Context.current().session();
		String[] paymtSrcList = request().body().asFormUrlEncoded()
				.get("paymtSrcList[]");
		Opportunity opportunity = null;
		try {
			mortAppPage2PreService = new MortgageApplicationPage2PurService();
			opportunity = mortAppPage2PreService.updatePage2PurDetails(
					dynamicForm, session, paymtSrcList);
			if (opportunity.getMlsListed() != null
					&& opportunity.getMlsListed().equalsIgnoreCase(
							MortgageApplicationConstants.MLS_MLSLISTED)
					|| opportunity.getMlsListed().equalsIgnoreCase(
							MortgageApplicationConstants.MLS_NEWBUILD))
				return ok(mortgagePage4.render(
						opportunity.getIsAdditionalApplicantExist(), "", "",
						"", ""));
			else
				return ok(mortgagePage3.render(
						opportunity.getIsAdditionalApplicantExist(), "", "",
						"", "", "", "", "", ""));
		} catch (MortgageApplicationPageServiceException mortAppException) {
			Logger.error("Error when update Pur Approval details ",
					mortAppException);
			return ok(leadsucess
					.render(Constants.COUCHBASE_CONNECTION_FAILED_MESSAGE));
		}
	}

	public static Result mortgagePage2Ref() {

		Logger.info("Inside mortgagePage2Ref Refinance Selected.");
		MortgageApplicationPage2RefService mortAppPage2RefService = null;
		DynamicForm dynamicForm = form().bindFromRequest();
		Opportunity opportunity = null;
		Session session = Http.Context.current().session();
		String[] paymtSrcList = request().body().asFormUrlEncoded()
				.get("paymtSrcList[]");
		try {
			mortAppPage2RefService = new MortgageApplicationPage2RefService();
			opportunity = mortAppPage2RefService.updatePage2RefDetails(
					dynamicForm, session, paymtSrcList);

			return ok(mortgagePage3.render(
					opportunity.getIsAdditionalApplicantExist(), "", "", "",
					"", "", "", "", ""));
		} catch (MortgageApplicationPageServiceException mortAppException) {
			Logger.error("Error when update Pur Approval details ",
					mortAppException);
			return ok(leadsucess
					.render(Constants.COUCHBASE_CONNECTION_FAILED_MESSAGE));
		}
	}

	public static Result mortgagePage3() {
		Logger.debug("Inside mortgagePage3");

		CouchbaseServiceHelper couchbaseServiceHelper = new CouchbaseServiceHelper();
		MortgageApplicationPageService mService = new MortgageApplicationPageService();
		DynamicForm dynamicForm = form().bindFromRequest();
		Session session = Http.Context.current().session();
		MortgageApplicationPageThreeService threeService = new MortgageApplicationPageThreeService();
		try {
			Opportunity opportunity = couchbaseServiceHelper.getCouhbaseDataByKey(mService.parseLeadId(session));
			threeService.loadFormData(opportunity, dynamicForm);
			threeService.updateOpportunity(opportunity);
			String additionalApplicants = opportunity.getIsAdditionalApplicantExist();

			return ok(mortgagePage4
					.render(additionalApplicants, "", "", "", ""));
		} catch (MortgageApplicationPageServiceException e) {
			return ok(leadsucess
					.render(Constants.COUCHBASE_CONNECTION_FAILED_MESSAGE));
		}
	}

	public static Result mortgagePage4() {
		Logger.debug("Inside (.)   mortgagePage4   ");
		CouchbaseServiceHelper couchbaseServiceHelper = new CouchbaseServiceHelper();
		MortgageApplicationPageService mService = new MortgageApplicationPageService();
		MortgageApplicationPageFourService pageFourService = new MortgageApplicationPageFourService();

		DynamicForm dynamicForm = form().bindFromRequest();

		try {
			Session session = Http.Context.current().session();
			Opportunity opportunity = couchbaseServiceHelper
					.getCouhbaseDataByKey(mService.parseLeadId(session));
			pageFourService.loadFormData(opportunity, dynamicForm);
			pageFourService.updateOpportunity(opportunity);
			return ok(mortgagePage5a.render("", "", "", "", "", ""));
		} catch (MortgageApplicationPageServiceException e) {
			Logger.error("Error in mortgagePage4 ", e);
			return ok(leadsucess
					.render(Constants.COUCHBASE_CONNECTION_FAILED_MESSAGE));
		}
	}

	public static Result mortgagePage5a() {
		Logger.debug(" Inside (.) mortgagePage5a controller method ");
		CouchbaseServiceHelper couchbaseServiceHelper = new CouchbaseServiceHelper();
		DynamicForm dynamicForm = form().bindFromRequest();
		MortgageApplicationPageService mService = new MortgageApplicationPageService();
		MortgageApplicationPageFiveAService pageFiveAService = new MortgageApplicationPageFiveAService();

		try {
			Session session = Http.Context.current().session();
			Opportunity opportunity = couchbaseServiceHelper
					.getCouhbaseDataByKey(mService.parseLeadId(session));
			pageFiveAService.loadFormData(opportunity, dynamicForm);
			pageFiveAService.updateOpportunity(opportunity);

			return ok(mortgagePage5b
					.render(opportunity.getIsAdditionalApplicantExist(), "",
							"", "", ""));
		} catch (MortgageApplicationPageServiceException e) {
			Logger.error("Error in mortgage5a method.", e);
			return ok(leadsucess
					.render(Constants.COUCHBASE_CONNECTION_FAILED_MESSAGE));
		}
	}

	public static Result mortgagePage5b() {
		Logger.info("Inside method mortgagePage5b ");
		CouchbaseServiceHelper couchbaseServiceHelper = new CouchbaseServiceHelper();
		DynamicForm dynamicForm = form().bindFromRequest();
		MortgageApplicationPageService mService = new MortgageApplicationPageService();
		MortgageApplicationPageFiveBService mFiveBService = new MortgageApplicationPageFiveBService();

		try {
			Session session = Http.Context.current().session();
			Opportunity opportunity = couchbaseServiceHelper
					.getCouhbaseDataByKey(mService.parseLeadId(session));

			mFiveBService.loadFormData(opportunity, dynamicForm);
			mFiveBService.updateOpportunity(opportunity);
			PersonalInfo pserInfo = mFiveBService
					.mapApplicantDetails(opportunity);

			boolean isMobile = opportunity.getForms().isMobile();

			if (!isMobile)
				return ok(mortgagePage6.render(
						pserInfo.getAdditionalApplicant(),
						pserInfo.getApplicantName(),
						pserInfo.getCoApplicantName(), "", "", "", "", "", "",
						"", "", "", "", "", "", "", "", "", "", "", ""));
			else
				return ok(mortgagePage6a.render(pserInfo));
		} catch (MortgageApplicationPageServiceException e) {
			Logger.error("Error in mortgagePage5b", e);
			return ok(leadsucess
					.render(Constants.COUCHBASE_CONNECTION_FAILED_MESSAGE));
		}

	}

	public static Result mortgagePage6() {
		Logger.info(" Inside (.) mortgagePage6  Method ");
		CouchbaseServiceHelper couchbaseServiceHelper = new CouchbaseServiceHelper();
		DynamicForm dynamicForm = form().bindFromRequest();
		Session session = Http.Context.current().session();
		MortgageApplicationPageService mService = new MortgageApplicationPageService();
		MortgageApplicationPageSixService mPageSixService = new MortgageApplicationPageSixService();

		try {
			Opportunity opportunity = couchbaseServiceHelper
					.getCouhbaseDataByKey(mService.parseLeadId(session));
			mPageSixService.loadFormData(opportunity, dynamicForm);
			mPageSixService.updateOpportunity(opportunity);
			String applicantName = opportunity.getApplicants().get(0)
					.getApplicant_name();
			String additionalApplicants = opportunity
					.getIsAdditionalApplicantExist();
			
			String coApplicantName = "";
			if (additionalApplicants != null && additionalApplicants.equalsIgnoreCase("yes")) {
				coApplicantName = opportunity.getApplicants().get(1)
						.getApplicant_name();
			}else
				additionalApplicants = "";

			ApplicantAddressParameter7 appAddressObj = new ApplicantAddressParameter7();
			CoApplicantAddressParameter7 coAppAddressObj = new CoApplicantAddressParameter7();
			return ok(mortgagePage7Address.render(additionalApplicants,
					applicantName, coApplicantName, appAddressObj,
					coAppAddressObj));
		} catch (MortgageApplicationPageServiceException e) {
			Logger.error("Error in mortgagePage6", e);
			return ok(leadsucess
					.render(Constants.COUCHBASE_CONNECTION_FAILED_MESSAGE));
		}
	}

	public static Result mortgagePage7() {

		Logger.info("Inside  (.) mortgagePage7");
		DynamicForm dynamicForm = form().bindFromRequest();
		Session session = Http.Context.current().session();
		MortgageApplicationPageSevenService mortAppPage7Service = new MortgageApplicationPageSevenService();
		Opportunity opportunity = null;
		try {
			opportunity = mortAppPage7Service.updateOpportunityAddresses(
					dynamicForm, session);
			Applicant applicant = opportunity.getApplicants().get(0);
			Logger.debug("applicant-->"+applicant);
			return ok(supplementary_income.render(applicant));
		} catch (MortgageApplicationPageServiceException mortAppException) {
			Logger.error("Error in (.) mortgagePage7", mortAppException);
			return ok(leadsucess.render(Constants.COUCHBASE_CONNECTION_FAILED_MESSAGE));
		}

	}

	public static Result mortgagePage7a() {
		Logger.info("Inside (.) mortgagePage7a");
		DynamicForm dynamicForm = form().bindFromRequest();
		Session session = Http.Context.current().session();
		MortgageApplicationPageSevenAService mortAppPage7AService = new MortgageApplicationPageSevenAService();
		Opportunity opportunity = null;
		try {
			opportunity = mortAppPage7AService.updateOpportunityAddresses(dynamicForm, session);
			CoApplicantAddressParameter7 coApplicantAddress = new CoApplicantAddressParameter7();
			coApplicantAddress.setCoApplicantName(opportunity.getApplicants().get(0).getApplicant_name());
			if (opportunity.getIsAdditionalApplicantExist() != null
					&& opportunity.getIsAdditionalApplicantExist()
							.equalsIgnoreCase("yes"))
				return ok(mortgagePage7b.render(coApplicantAddress));
			else{
				Applicant applicant = opportunity.getApplicants().get(1);
				return ok(supplementary_income.render(applicant));
			}
		} catch (MortgageApplicationPageServiceException mortAppException) {
			Logger.error("Error in (.) mortgagePage7 ", mortAppException);
			return ok(leadsucess.render(Constants.COUCHBASE_CONNECTION_FAILED_MESSAGE));
		}
	}

	public static Result mortgagePage7b() {
		DynamicForm dynamicForm = form().bindFromRequest();
		Session session = Http.Context.current().session();
		MortgageApplicationPageSevenBService mortAppPage7BService = new MortgageApplicationPageSevenBService();
		Opportunity opportunity = null;
		try {
			opportunity = mortAppPage7BService.updateOpportunityAddresses(
					dynamicForm, session);
			Applicant applicant = opportunity.getApplicants().get(1);
			return ok(supplementary_income.render(applicant));
		} catch (MortgageApplicationPageServiceException mortAppException) {
			Logger.error("Error in (.) mortgagePage7", mortAppException);
			return ok(leadsucess
					.render(Constants.COUCHBASE_CONNECTION_FAILED_MESSAGE));
		}
	}
	public static Result mortgageSuplementaryIncomeA() {
		Logger.info("Inside mortgagePage8Suplementary() of Mortgage Controller");
		Session session = Http.Context.current().session();
		SupplementaryIncomeAService supplIncomeService = new SupplementaryIncomeAService();
		DynamicForm dynamicForm = form().bindFromRequest();
		String[] supplIncomeSrc = request().body().asFormUrlEncoded().get("supplIncomeSrc[]");
		Logger.debug("supplIncomeSrc "+supplIncomeSrc);
		String[] addtionalSrc = request().body().asFormUrlEncoded().get("addtionalSrc[]");
		Logger.debug("addtionalSrc "+addtionalSrc);
		String[] aboutIncomeSrc = request().body().asFormUrlEncoded().get("aboutIncomeSrc[]");
		List<String[]> listOfIncomeType=new ArrayList<String[]>();
		listOfIncomeType.add(supplIncomeSrc);
		listOfIncomeType.add(addtionalSrc);
		listOfIncomeType.add(aboutIncomeSrc);
		try {
			Opportunity opportunity = supplIncomeService.updateApplicanntIncomeDetails(dynamicForm, session, listOfIncomeType);
			TotalAssets total = new TotalAssets(); 
			String coApplName = "";
			if(opportunity.getIsAdditionalApplicantExist() != null && opportunity.getIsAdditionalApplicantExist().equalsIgnoreCase("yes"))
				coApplName = opportunity.getApplicants().get(1).getApplicant_name();
			return ok(mortgagePage10Assets.render("", opportunity.getApplicants().get(0).getApplicant_name(),
					coApplName, total));
		} catch (MortgageApplicationPageServiceException e) {
			return ok(MortgageApplicationConstants.MORTGAGE_APPLICATION_COUCHBASE_KEY);
		}

	}
	public static Result mortgagePage8() {
		Logger.info("inside mortgagePage8");
		try {
		} catch (Exception e) {
			Logger.error("Error in mortgagePage8 ", e);
			return ok(leadsucess
					.render(Constants.COUCHBASE_CONNECTION_FAILED_MESSAGE));
		}
		return ok(mortgagePage10Assets.render("", "Test", "Test",
				new TotalAssets()));
	}

	public static Result mortgagePage9() {
		
		return ok(leadsucess
				.render(Constants.COUCHBASE_CONNECTION_FAILED_MESSAGE));
	}

	public static Result mortgagePage10Assets() {
		Logger.info("inside (.) mortgagePage10Assets");
		MortgageApplicationPageService mService = new MortgageApplicationPageService();
		CouchbaseServiceHelper couchbaseServiceHelper = new CouchbaseServiceHelper();
		DynamicForm dynamicForm = form().bindFromRequest();
		MortgageApplicationPageTenService mservice = new MortgageApplicationPageTenService();
		Session session = Http.Context.current().session();
		try {
			Opportunity opportunity = couchbaseServiceHelper
					.getCouhbaseDataByKey(mService.parseLeadId(session));

			String None = dynamicForm.get("None");
			if (None.equalsIgnoreCase("true")) {
				None = "";
				opportunity.setPogressStatus(85);
				mservice.deleteAssest(opportunity);
			} else {
				String[] assetTypes = request().body().asFormUrlEncoded()
						.get("assetTypes[]");
				List<String> selectedValues = mservice
						.getListOfAssets(assetTypes);
				Logger.debug("after incomeoddlist = " + selectedValues.size());
				mservice.deleteAssest(opportunity);

				for (String selectedValue : selectedValues) {

					Logger.debug("selected values inside loop : "
							+ selectedValue);

					if (selectedValue != null
							&& selectedValue.equalsIgnoreCase("Vehicle")) {

						Logger.debug("Vehicle Selected");
						final String[] asset = request().body()
								.asFormUrlEncoded().get("asset[]");
						final String[] description = request().body()
								.asFormUrlEncoded().get("description[]");
						final String[] values = request().body()
								.asFormUrlEncoded().get("value[]");
						final String[] ownerShip = request().body()
								.asFormUrlEncoded().get("designation[]");
						mservice.listAssets(opportunity, asset, description,
								values, ownerShip, "Vehicle", "Vehicle");

					}// End of Vehicle

					if (selectedValue != null
							&& selectedValue.equalsIgnoreCase("BankAccount")) {
						Logger.debug("Bank Account selected.");
						final String[] asset1 = request().body()
								.asFormUrlEncoded().get("asset1[]");
						final String[] description1 = request().body()
								.asFormUrlEncoded().get("description1[]");
						final String[] values1 = request().body()
								.asFormUrlEncoded().get("value1[]");
						final String[] ownerShip1 = request().body()
								.asFormUrlEncoded().get("designation1[]");
						mservice.listAssets(opportunity, asset1, description1,
								values1, ownerShip1, "other", "BankAccount");

					}

					if (selectedValue != null
							&& selectedValue.equalsIgnoreCase("RRSPTSFA")) {

						Logger.debug("RRSPTSFA selected ");
						final String[] asset2 = request().body()
								.asFormUrlEncoded().get("asset2[]");
						final String[] description2 = request().body()
								.asFormUrlEncoded().get("description2[]");
						final String[] values2 = request().body()
								.asFormUrlEncoded().get("value2[]");
						final String[] ownerShip2 = request().body()
								.asFormUrlEncoded().get("designation2[]");
						mservice.listAssets(opportunity, asset2, description2,
								values2, ownerShip2, "RRSPs", "RRSPTSFA");

					}// End of RRSPs

					if (selectedValue != null
							&& selectedValue.equalsIgnoreCase("Investments")) {

						Logger.debug("Investment selected...");
						final String[] asset3 = request().body().asFormUrlEncoded().get("asset3[]");
						final String[] description3 = request().body().asFormUrlEncoded().get("description3[]");
						final String[] values3 = request().body().asFormUrlEncoded().get("value3[]");
						final String[] ownerShip3 = request().body().asFormUrlEncoded().get("designation3[]");
						mservice.listAssets(opportunity, asset3, description3,values3, ownerShip3, "other", "Investments");
					}// End of Investments

					if (selectedValue != null
							&& selectedValue.equalsIgnoreCase("Other")) {

						Logger.debug("Other selected...");
						final String[] asset4 = request().body() .asFormUrlEncoded().get("asset4[]");
						final String[] description4 = request().body()
								.asFormUrlEncoded().get("description4[]");
						final String[] values4 = request().body()
								.asFormUrlEncoded().get("value4[]");
						final String[] ownerShip4 = request().body()
								.asFormUrlEncoded().get("designation4[]");
						mservice.listAssets(opportunity, asset4, description4,
								values4, ownerShip4, "other", "other");
					}
				}// End of For Loop

			}// End of else
			opportunity.setPogressStatus(85);
			mservice.createAssests(opportunity);

			//String applicantName = session.get("applicantFirstName");
			//String coApplicantName = session.get("co_applicantFirstName");
			
			String applicantName = opportunity.getApplicants().get(0).getApplicant_name();
			String coApplicantName = "";
			if(opportunity.getIsAdditionalApplicantExist() != null && opportunity.getIsAdditionalApplicantExist().equalsIgnoreCase("yes"))
				coApplicantName = opportunity.getApplicants().get(1).getApplicant_name();
			
			Logger.debug("applicantName "+applicantName);
			Logger.debug("coApplicantName "+coApplicantName);
			
			return ok(mortgagePage11Properties.render("", applicantName,
					coApplicantName, new ArrayList<Property>()));
		} catch (MortgageApplicationPageServiceException e) {
			Logger.error("Error in mortgagePage10Assets", e);
			return ok("Some thing went wrong in mortgagePage10Assets");
		} catch (PostGressDaoServiceException e) {
			return ok("Some thing went wrong in mortgagePage10Assets");
		}
	}

	public static Result mortgagePage11Properties() {

		Logger.debug("Inside mortgagePage11Properties");
		CouchbaseServiceHelper couchbaseServiceHelper = new CouchbaseServiceHelper();
		MortgageApplicationPageService mService = new MortgageApplicationPageService();
		MortgageApplicationPageElevenService mElevenService = new MortgageApplicationPageElevenService();

		DynamicForm dynamicForm = form().bindFromRequest();
		Session session = Http.Context.current().session();
		String applicantName = "";
		String coApplicantName = "";
		String additionalApplicant = "";

		try {
			Opportunity opportunity = couchbaseServiceHelper
					.getCouhbaseDataByKey(mService.parseLeadId(session));
			//delete Applicant Propery If EXist before in Couchbase as well as Postgress
			mElevenService.deleteProperty(opportunity);
			mElevenService.getPropertyList(opportunity, dynamicForm);
			mElevenService.updateApplicantProperties(opportunity);
			additionalApplicant = opportunity.getIsAdditionalApplicantExist();
			applicantName = opportunity.getApplicants().get(0)
					.getApplicant_name();
			if (additionalApplicant != null
					&& additionalApplicant.equalsIgnoreCase("yes")) {
				coApplicantName = opportunity.getApplicants().get(1)
						.getApplicant_name();
			}

			return ok(mortgagePage12Disclose.render("", additionalApplicant, 7,
					applicantName, coApplicantName));
		} catch (MortgageApplicationPageServiceException e) {
			Logger.error("Error in mortgagePage11Properties", e);
			return ok("Something went wrong in mortgagePage11Properties...");
		}catch (PostGressDaoServiceException e) {
			Logger.error("Error in mortgagePage11Properties", e);
			return ok("Something went wrong in mortgagePage11Properties...");

		}

	}

	public static Result mortgagePage6a() {

		Logger.info(" Inside (.) mortgagePage6a Method ");
		CouchbaseServiceHelper couchbaseServiceHelper = new CouchbaseServiceHelper();
		DynamicForm dynamicForm = form().bindFromRequest();
		Session session = Http.Context.current().session();
		MortgageApplicationPageService mService = new MortgageApplicationPageService();
		MortgageApplicationPageSixAService mPageSixService = new MortgageApplicationPageSixAService();

		try {
			Opportunity opportunity = couchbaseServiceHelper
					.getCouhbaseDataByKey(mService.parseLeadId(session));
			mPageSixService.loadFormData(opportunity, dynamicForm);
			mPageSixService.updateOpportunity(opportunity);
			String applicantName = opportunity.getApplicants().get(0)
					.getApplicant_name();
			String additionalApplicants = opportunity
					.getIsAdditionalApplicantExist();
			String coApplicantName = "";
			if (additionalApplicants.equalsIgnoreCase("yes")) {
				coApplicantName = opportunity.getApplicants().get(1)
						.getApplicant_name();
			}
			PersonalInfo personalInfo = new PersonalInfo();
			personalInfo.setCoApplicantName(coApplicantName);
			ApplicantAddressParameter7 address = new ApplicantAddressParameter7();
			address.setApplicantName(applicantName);
			if (additionalApplicants != null
					&& additionalApplicants.equalsIgnoreCase("yes"))
				return ok(mortgagePage6b.render(personalInfo));
			else
				return ok(mortgagePage7a.render(address));
		} catch (MortgageApplicationPageServiceException e) {
			Logger.error("Error in mortgagePage6", e);
			return ok(leadsucess
					.render(Constants.COUCHBASE_CONNECTION_FAILED_MESSAGE));
		}
	}

	public static Result mortgagePage6b() {
		Logger.info(" Inside mortgagePage6b ");
		CouchbaseServiceHelper couchbaseServiceHelper = new CouchbaseServiceHelper();
		DynamicForm dynamicForm = form().bindFromRequest();
		Session session = Http.Context.current().session();
		MortgageApplicationPageService mService = new MortgageApplicationPageService();
		MortgageApplicationPageSixBService mPageSixService = new MortgageApplicationPageSixBService();
		try {
			Opportunity opportunity = couchbaseServiceHelper
					.getCouhbaseDataByKey(mService.parseLeadId(session));
			mPageSixService.loadFormData(opportunity, dynamicForm);
			mPageSixService.updateOpportunity(opportunity);
			String applicantName = opportunity.getApplicants().get(0)
					.getApplicant_name();
			String additionalApplicants = opportunity
					.getIsAdditionalApplicantExist();
			String coApplicantName = "";
			if (additionalApplicants.equalsIgnoreCase("yes")) {
				coApplicantName = opportunity.getApplicants().get(1)
						.getApplicant_name();
			}
			PersonalInfo personalInfo = new PersonalInfo();
			personalInfo.setCoApplicantName(coApplicantName);
			ApplicantAddressParameter7 address = new ApplicantAddressParameter7();
			address.setApplicantName(applicantName);
			return ok(mortgagePage7a.render(address));
		} catch (MortgageApplicationPageServiceException e) {
			Logger.error("Error in mortgagePage6", e);
			return ok(leadsucess
					.render(Constants.COUCHBASE_CONNECTION_FAILED_MESSAGE));
		}
	}

	public static Result mortgagePage12() {
		Logger.info("Inside mortgagePage12() method of Mortgage Controller ");
		CouchbaseServiceHelper couchbaseServiceHelper = new CouchbaseServiceHelper();
		DynamicForm dynamicForm = form().bindFromRequest();
		Session session = Http.Context.current().session();
		MortgageApplicationPageService mService = new MortgageApplicationPageService();
		MortgageApplicationPageTwelevService mTwelevService = new MortgageApplicationPageTwelevService();

		try {
			Opportunity opportunity = couchbaseServiceHelper
					.getCouhbaseDataByKey(mService.parseLeadId(session));
			mTwelevService.loadFormData(opportunity, dynamicForm);
			mTwelevService.updateOpportunity(opportunity);
			return ok(leadsucess
					.render("Thank you for completing the Visdom Mortgage Application.  We will be in touch with you very soon."));

		} catch (MortgageApplicationPageServiceException e) {

			Logger.error("Error in mortgagePage12 ", e);
			return ok("Some thing went wrong in mortgagePage7");

		}

	}

	// Back Button Operation methods...
	public static Result mortgageBackPage1() {

		Logger.info(" inside mortgageBackPage1 ");
		CouchbaseServiceHelper couchbaseServiceHelper = new CouchbaseServiceHelper();
		MortgageApplicationPageOneBackButtonService mButtonService = new MortgageApplicationPageOneBackButtonService();
		MortgageApplicationPageService mPageService = new MortgageApplicationPageService();
		Session session = Http.Context.current().session();

		try {
			Opportunity opportunity = couchbaseServiceHelper
					.getCouhbaseDataByKey(mPageService.parseLeadId(session));
			Applicant applicant = mButtonService.getApplicant(opportunity);
			Applicant coApplicant = mButtonService.getCoApplicant(opportunity);
			boolean isMobile = mPageService.checkDeviceBySetInSession(session);

			Logger.info("Back button Functionality with Mobile Device Type="
					+ isMobile);
			if (isMobile) {
				if (coApplicant != null) {

					return ok(mortgagePage1b.render(opportunity, coApplicant));
				} else {
					return ok(mortgagePage1a.render(opportunity, applicant));

				}
			} else {
				if (coApplicant == null)
					coApplicant = new Applicant();
				return ok(mortgagePage1.render(opportunity, applicant,
						coApplicant));
			}
		} catch (MortgageApplicationPageServiceException e) {
			Logger.error("Error in linking back page 1 " + e);
			return ok("Some thing went wrong in linking back page 1 ");
		}

	}

	public static Result mortgageBackPage2() {
		Logger.info("Inside (.) mortgageBackPage2 of Mortgage Ctrlr");

		LendingTerm lendingTerm = null;
		MortgageApplicationPageService mService = new MortgageApplicationPageService();
		CouchbaseServiceHelper couchbaseServiceHelper = new CouchbaseServiceHelper();
		MortgageApplicationPage2PurService mPg2PurService = new MortgageApplicationPage2PurService();
		MortgageApplicationPage2RefService mPg2RefService = new MortgageApplicationPage2RefService();
		Session session = Http.Context.current().session();

		try {
			Opportunity opportunity = couchbaseServiceHelper
					.getCouhbaseDataByKey(mService.parseLeadId(session));
			if (opportunity.getLendingGoal() != null
					&& opportunity.getLendingGoal()
							.equalsIgnoreCase("Purchase")) {
				lendingTerm = mPg2PurService.getPurchaseDetails(opportunity);
				return ok(mortgagePage2Pur.render(lendingTerm, ""));
			} else {
				lendingTerm = mPg2RefService.getRefinanceDetails(opportunity);
				return ok(mortgagePage2Ref.render(lendingTerm, ""));
			}
		} catch (MortgageApplicationPageServiceException e) {
			Logger.error("Error in page2 method of Mortgage...", e);
			return ok(leadsucess
					.render(Constants.COUCHBASE_CONNECTION_FAILED_MESSAGE));
		}
	}

	public static Result mortgageBackPage23() {

		Logger.info("Inside (.) mortgageBackPage23 of Mortgage ctrl");
		LendingTerm lendingTerm = null;
		MortgageApplicationPageService mService = new MortgageApplicationPageService();
		CouchbaseServiceHelper couchbaseServiceHelper = new CouchbaseServiceHelper();
		MortgageApplicationPage2PreService mPg2PreService = new MortgageApplicationPage2PreService();
		Session session = Http.Context.current().session();

		try {
			Opportunity opportunity = couchbaseServiceHelper
					.getCouhbaseDataByKey(mService.parseLeadId(session));
			Logger.debug("Applicant_leadingGoal "
					+ opportunity.getLendingGoal());

			if (opportunity.getLendingGoal() != null
					&& opportunity.getLendingGoal().equalsIgnoreCase(
							"PreApproval")) {
				Logger.debug("Rendering mortgagePage PreApproval");
				lendingTerm = mPg2PreService.getPreApprovalDetails(opportunity);
				return ok(mortgagePage2Pre.render("", lendingTerm));
			} else if (opportunity.getLendingGoal() != null
					&& opportunity.getLendingGoal().equalsIgnoreCase(
							"Refinance")) {
				Logger.debug("Rendering mortgageBackPage3 ");
				return mortgageBackPage3();
			} else {
				if (opportunity.getMlsListed() != null
						&& !opportunity.getMlsListed().equalsIgnoreCase("")
						&& opportunity.getMlsListed().equalsIgnoreCase(
								"MLSListed")
						|| opportunity.getMlsListed().equalsIgnoreCase(
								"NewBuild")) {
					Logger.debug("Calling mortgagePage2()  from  mortgageBackPage23() ");
					return mortgageBackPage2();
				} else {
					Logger.debug("Calling mortgagePage3() from mortgageBackPage23() ");
					return mortgageBackPage3();
				}
			}

		} catch (MortgageApplicationPageServiceException e) {
			Logger.error("Error in page23 method of Mortgage...", e);
			return ok(leadsucess
					.render(Constants.COUCHBASE_CONNECTION_FAILED_MESSAGE));
		}
	}

	public static Result mortgageBackPage3() {

		Logger.debug("Inside (.) mortgageBackPage3");
		MortgageApplicationPageService mService = new MortgageApplicationPageService();
		CouchbaseServiceHelper couchbaseServiceHelper = new CouchbaseServiceHelper();
		Session session = Http.Context.current().session();
		try {
			Opportunity opportunity = couchbaseServiceHelper
					.getCouhbaseDataByKey(mService.parseLeadId(session));

			String applicant_typeofbuilding = opportunity.getPropertyType();
			;
			String applicant_propertystyle = opportunity.getPropertyStyle();
			String applicant_sqaurefootage = opportunity.getSquareFootage();
			String applicant_propertyheated = opportunity.getPropertyHeated();
			String applicant_getwater = opportunity.getPropertyWater();
			String applicant_propertydispose = opportunity.getPropertySewage();
			String applicant_garagetype = opportunity.getGarage_type();
			String applicant_garageSize = opportunity.getGarageSize();

			return ok(mortgagePage3.render("", applicant_typeofbuilding,
					applicant_propertystyle, applicant_sqaurefootage,
					applicant_propertyheated, applicant_getwater,
					applicant_propertydispose, applicant_garagetype,
					applicant_garageSize));
		} catch (MortgageApplicationPageServiceException e) {
			Logger.error("Error in page23 method of Mortgage...", e);
			return ok(leadsucess
					.render(Constants.COUCHBASE_CONNECTION_FAILED_MESSAGE));
		}
	}

	public static Result mortgageBackPage4() {

		Logger.info("Inside (.) mortgageBackPage4 method of Mortgage Controller.");
		MortgageApplicationPageService mService = new MortgageApplicationPageService();
		CouchbaseServiceHelper couchbaseServiceHelper = new CouchbaseServiceHelper();
		MortgageApplicationPageFourService mPg4Service = new MortgageApplicationPageFourService();

		String mortgageType = "";
		String mortgageTerm = "";
		String lookingFor = "";
		String ammortizeYear = "";
		Session session = Http.Context.current().session();
		try {
			Opportunity opportunity = couchbaseServiceHelper
					.getCouhbaseDataByKey(mService.parseLeadId(session));
			mortgageType = opportunity.getDesiredMortgageTypeCB();
			mortgageTerm = opportunity.getDesiredTermCB();
			lookingFor = mPg4Service.getDesiredMortgitionValueForm(opportunity
					.getDesired_amortization());
			if (opportunity.getDesiredAmortizationCB() != null && opportunity.getDesiredAmortizationCB().equalsIgnoreCase("Other")) {
				ammortizeYear = opportunity.getAmortizeYear() + "";
			}
			Logger.debug("testing-------> 4 page ");
			Logger.debug("mortgageType : -->"+mortgageType+ "mortgageTerm-->"+mortgageTerm +"lookingFor "+lookingFor+ "ammortizeYear "+ammortizeYear);

			return ok(mortgagePage4.render("", mortgageType, mortgageTerm,
					lookingFor, ammortizeYear));
		} catch (MortgageApplicationPageServiceException | NullPointerException e) {
			Logger.error("Error in mortgageBackPage4 method of Mortgage...", e);
			return ok(leadsucess
					.render(Constants.COUCHBASE_CONNECTION_FAILED_MESSAGE));
		}
	}

	public static Result mortgageBackPage5a() {
		Logger.info("Inside (.) mortgageBackPage5a method of Mortgage Controller.");
		MortgageApplicationPageService mService = new MortgageApplicationPageService();
		CouchbaseServiceHelper couchbaseServiceHelper = new CouchbaseServiceHelper();
		Session session = Http.Context.current().session();
		try{
		Opportunity opportunity = couchbaseServiceHelper.getCouhbaseDataByKey(mService .parseLeadId(session));
		
		String incomedown1 = opportunity.getIncomeDecreasedWorried();
		String largerfamily1 = opportunity.getFutureFamily();
		String buyingnewvechile1 = opportunity.getBuyNewVehicle();
		String Planninglifestyle1 = opportunity.getLifestyleChange();
		String financialrisk1 = opportunity.getFinancialRiskTaker();
		Logger.debug("incomedecreased "+incomedown1+"\n Future family "+largerfamily1+"\n buyingnewvechile1 "+buyingnewvechile1
				+"\n Lifestyle change"+Planninglifestyle1+"\n financialrisk1 "+financialrisk1);
		return ok(mortgagePage5a.render("", incomedown1, largerfamily1,
				buyingnewvechile1, Planninglifestyle1, financialrisk1));
		}catch (MortgageApplicationPageServiceException | NullPointerException e ) {
			Logger.error("Error in (.) mortgageBackPage4 method of Mortgage...", e);
			return ok(leadsucess.render(Constants.COUCHBASE_CONNECTION_FAILED_MESSAGE));
		}}

	public static Result mortgageBackPage5b() {

		Logger.info("Inside (.) mortgageBackPage5a method of Mortgage Controller.");
		MortgageApplicationPageService mService = new MortgageApplicationPageService();
		CouchbaseServiceHelper couchbaseServiceHelper = new CouchbaseServiceHelper();
		Session session = Http.Context.current().session();
		try {
			Opportunity opportunity = couchbaseServiceHelper.getCouhbaseDataByKey(mService .parseLeadId(session));
			
			String thinkproperty1 = opportunity.getPropertyLessThen5Years();
			String imaginesamejob1 = opportunity.getJob5Years();
			
			String	incomeraise1 = opportunity.getIncomeRaise();
			String rentalproperty1 = opportunity.getRentalProperty();
			
			return ok(mortgagePage5b.render("", thinkproperty1, imaginesamejob1,
					incomeraise1, rentalproperty1));
		} catch (MortgageApplicationPageServiceException | NullPointerException e ) {
			Logger.error("Error in mortgageBackPage5a method of Mortgage...", e);
			return ok(leadsucess.render(Constants.COUCHBASE_CONNECTION_FAILED_MESSAGE));
		}

	}

	public static Result mortgageBackPage6() {

		Logger.info(" Inside (.) mortgagePage6  Method ");
		CouchbaseServiceHelper couchbaseServiceHelper= new CouchbaseServiceHelper();
		Session session = Http.Context.current().session();
		MortgageApplicationPageService mService=new MortgageApplicationPageService();

		try {
				Opportunity opportunity = couchbaseServiceHelper.getCouhbaseDataByKey(mService.parseLeadId(session));
				String applicantName = opportunity.getApplicants().get(0).getApplicant_name();
				String additionalApplicants = opportunity.getIsAdditionalApplicantExist();
				String workPhone = opportunity.getApplicants().get(0).getWork();
				String appDob  = opportunity.getApplicants().get(0).getBirthDate();
				String appSin = opportunity.getApplicants().get(0).getSin();
				String appDependant =opportunity.getApplicants().get(0).getDependant();
				Logger.debug("Co Appliacant details "+"\n ApplicantName "+applicantName+"\n AppWorkPhone "+workPhone 
						+"\n  appDob "+appDob+"\n AppSin "+appSin+"\n AppDependant "+appDependant);
				
				String coApplicantName = "";
				String coAppWorkPhone = "";
				String coAppDob = "";
				String coAppSin = "";
				String coAppDependant = "";
				if(additionalApplicants.equalsIgnoreCase("yes")){
					coApplicantName = opportunity.getApplicants().get(1).getApplicant_name();
					coAppWorkPhone =  opportunity.getApplicants().get(1).getWork();
					coAppDob = opportunity.getApplicants().get(1).getBirthDate();
					coAppSin = opportunity.getApplicants().get(1).getSin();
					coAppDependant = opportunity.getApplicants().get(1).getDependant();
					Logger.debug("Co Appliacant details "+"\n coApplicantName "+coApplicantName+"\n coAppWorkPhone "+coAppWorkPhone 
							+"\n  coAppDob "+coAppDob+"\n coAppSin "+coAppSin+"\n coAppDependant "+coAppDependant);
				}
				return ok(mortgagePage6.render(additionalApplicants, applicantName,coApplicantName,"delMobNo", workPhone, "delHomPhNo", appDob,
						appSin, "delRelSts", appDependant, "delNonRes","delmovCand", "delCoMobNo",coAppWorkPhone, "delHmPhon",coAppDob , coAppSin,
						"delRelSts",coAppDependant, "delcoAppRes", "delCoAppMov"));
		} catch (MortgageApplicationPageServiceException e) {
			Logger.error("Error in mortgagePage6", e);
			return ok(leadsucess.render(Constants.COUCHBASE_CONNECTION_FAILED_MESSAGE));
		}
	
	}

	public static Result mortgageBackPage7() {
		
		Logger.info("Inside  (.) mortgagePage7");
		DynamicForm dynamicForm = form().bindFromRequest();
		Session session = Http.Context.current().session();
		MortgageApplicationPageSevenService mortAppPage7Service = new  MortgageApplicationPageSevenService();
		CouchbaseServiceHelper couchbaseServiceHelper= new CouchbaseServiceHelper();
		MortgageApplicationPageService mService=new MortgageApplicationPageService();
		ApplicantAddressParameter7 applAddressParam = null;
		CoApplicantAddressParameter7 coApplAddressParam = null;
		 
		try {
			 
			Opportunity opportunity = couchbaseServiceHelper.getCouhbaseDataByKey(mService.parseLeadId(session));
			String additionalApplicants = opportunity.getIsAdditionalApplicantExist(); 
			 
			

			boolean isMobile = opportunity.getForms().isMobile();
			if (isMobile) {
				if (additionalApplicants != null && additionalApplicants.equalsIgnoreCase("yes")) {
					applAddressParam = mortAppPage7Service.getApplicantAddresses(opportunity);
					return ok(mortgagePage7b.render(coApplAddressParam));
				} else {
					coApplAddressParam = mortAppPage7Service.getCoApplicantAddressParameter7(opportunity);
					return ok(mortgagePage7a.render(applAddressParam));
				}
			}else{
				String applName = opportunity.getApplicants().get(0).getApplicant_name();
				String coApplName = "";
				if(additionalApplicants != null && additionalApplicants.equalsIgnoreCase("yes")){
					coApplName = opportunity.getApplicants().get(1).getApplicant_name();
					coApplAddressParam = mortAppPage7Service.getCoApplicantAddressParameter7(opportunity);

				}
				applAddressParam = mortAppPage7Service.getApplicantAddresses(opportunity);
				return ok(mortgagePage7Address.render(additionalApplicants, applName, coApplName, applAddressParam, coApplAddressParam));	
			}
			
		} catch (MortgageApplicationPageServiceException mortAppException) {
			Logger.error("Error in (.) mortgagePage7" , mortAppException);
			return ok(leadsucess.render(Constants.COUCHBASE_CONNECTION_FAILED_MESSAGE));
		}
		}
	public static Result mortgageBackSuplementaryIncomeA() {
		Logger.info("*** inside (.) mortgageBackPage8Suplementary ***");
		CouchbaseServiceHelper couchbaseServiceHelper= new CouchbaseServiceHelper();
		Session session = Http.Context.current().session();
		MortgageApplicationPageService mService=new MortgageApplicationPageService();
		SupplementaryIncomeAService supplIncASrvc = new SupplementaryIncomeAService();
		try{
			Opportunity opportunity = couchbaseServiceHelper.getCouhbaseDataByKey(mService.parseLeadId(session));
			//supplIncASrvc.removeData(opportunity);
			Applicant applicant = opportunity.getApplicants().get(0);
			Logger.debug("applicant [[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[["+applicant);
			return ok(supplementary_income.render(applicant));
		}catch (MortgageApplicationPageServiceException e) {
			Logger.error("Error in mortgageBackSuplementaryIncomeA ", e);
			return ok(leadsucess.render(Constants.COUCHBASE_CONNECTION_FAILED_MESSAGE));
		}
	}
	public static Result mortgageBackPage89() {
		return ok("need to be change");
	}

	public static Result mortgageBackPage8() {
		return ok("Need to be change");
	}

	public static Result mortgageBackPage9() {
		return ok("Need to be change");
	}

	public static Result mortgageBackPage10() {
		TotalAssets toaAssets = new TotalAssets();
		MortgageApplicationPageService mService = new MortgageApplicationPageService();
		Session session = Http.Context.current().session();
		MortgageApplicationPage10BackButtonService mService2 = new MortgageApplicationPage10BackButtonService();
		Opportunity opportunity = null;
		try {
			opportunity = mService2.getOpportunity(mService
					.parseLeadId(session));
			toaAssets = mService2.getTotalAssets(opportunity);
			if (toaAssets == null)
				toaAssets = new TotalAssets();
			Applicant applicant = opportunity.getApplicants().get(0);
			String coApplicantName = "";
			if (opportunity.getIsAdditionalApplicantExist()!=null && opportunity.getIsAdditionalApplicantExist().equalsIgnoreCase(
					"Yes")) {
				coApplicantName = opportunity.getApplicants().get(1)
						.getApplicant_name();
			}

			return ok(mortgagePage10Assets.render("",
					applicant.getApplicant_name(), coApplicantName, toaAssets));
		} catch (MortgageApplicationPageServiceException e) {
			Logger.error(
					"Error when retriving assets Deatails from Couchbase for Opportunity="
							+ opportunity, e);
		}
		// #TODO displaying error need to change
		return ok("Need to be change");
	}

	public static Result mortgageBackPage11() {
		MortgageApplicationPageService mService = new MortgageApplicationPageService();
		Session session = Http.Context.current().session();
		MortgageApplication11BackButtonService mService2 = new MortgageApplication11BackButtonService();
		Opportunity opportunity = null;
		try {
			opportunity = mService2.getOpportunity(mService
					.parseLeadId(session));
			List<Property> listOfProperty = mService2
					.getListOfProperties(opportunity);
			String howManyProperties="";
			if(listOfProperty.size()==1)
				howManyProperties="none";

			if(listOfProperty.size()==1)
				howManyProperties="one";
			if(listOfProperty.size()==2)
				howManyProperties="two";
			if(listOfProperty.size()==3)
				howManyProperties="three";
			if(listOfProperty.size()==4)
				howManyProperties="four";
			if(listOfProperty.size()>4)
				howManyProperties="more";

			Applicant applicant = opportunity.getApplicants().get(0);
			String coApplicantName = "";
			if (opportunity.getIsAdditionalApplicantExist().equalsIgnoreCase(
					"Yes")) {
				coApplicantName = opportunity.getApplicants().get(1)
						.getApplicant_name();
			}

			return ok(	mortgagePage11Properties.render(howManyProperties,applicant.getApplicant_name(),coApplicantName,listOfProperty));
		} catch (MortgageApplicationPageServiceException e) {
			Logger.error("Error in getting opporunity data from couchbase " + e);
		}
		return ok("Neded to be change");
	}

	public static Result mortgageBackPage1a() {
		MortgageApplicationPageService mService = new MortgageApplicationPageService();
		Session session = Http.Context.current().session();
		MortgageApplicationPageOneBackButtonService mButtonService = new MortgageApplicationPageOneBackButtonService();
		try {
			Opportunity opportunity = mButtonService.getOpporunity(mService
					.parseLeadId(session));
			Applicant applicant = mButtonService.getApplicant(opportunity);
			return ok(mortgagePage1a.render(opportunity, applicant));

		} catch (MortgageApplicationPageServiceException e) {
			Logger.error("Error in get data for BackButton Page 1A ", e);
			/*
			 * #TODO Need to change error message
			 */
			return ok("Error in render backpage 1a ");
		}
	}

	public static Result mortgageBackPage1b() {
		MortgageApplicationPageService mService = new MortgageApplicationPageService();
		Session session = Http.Context.current().session();
		MortgageApplicationPageOneBBackButtonService mButtonService = new MortgageApplicationPageOneBBackButtonService();
		try {
			Opportunity opportunity = mButtonService.getOpporunity(mService
					.parseLeadId(session));
			Applicant applicant = mButtonService.getCoApplicant(opportunity);
			return ok(mortgagePage1a.render(opportunity, applicant));

		} catch (MortgageApplicationPageServiceException e) {
			Logger.error("Error in get data for BackButton Page 1A ", e);
			/*
			 * #TODO Need to change error message
			 */
			return ok("Error in render backpage 1a ");
		}
	}

	public static Result mortgageBackPage6a() {
		
		Logger.debug("*** inside mortgageBackPage6a ***");
		CouchbaseServiceHelper couchbaseServiceHelper= new CouchbaseServiceHelper();
		Session session = Http.Context.current().session();
		MortgageApplicationPageService mService=new MortgageApplicationPageService();
		MortgageApplicationPageSixAService  mPageSixService=new MortgageApplicationPageSixAService();
		try{
			Opportunity opportunity=couchbaseServiceHelper.getCouhbaseDataByKey(mService.parseLeadId(session));
			PersonalInfo personalInfo = mPageSixService.getPersonalInfo(opportunity);
			Logger.debug("\n Work phone " + personalInfo.getWorkPhone()
					+ "\n bityhday " + personalInfo.getBirthDay()
					+ "\n Social insurance: " + personalInfo.getSocialInsurance()
					+ "\n dependant " + personalInfo.getDependents()
					+ "\n are you " + personalInfo.getAreUCanadianRes());
					 
			return ok(mortgagePage6a.render(personalInfo));
		}catch (MortgageApplicationPageServiceException e) {
			Logger.error("Error in mortgagePage6a", e);
			return ok(leadsucess.render(Constants.COUCHBASE_CONNECTION_FAILED_MESSAGE));
		}
		
	}

	public static Result mortgageBackPage6b() {
		Logger.debug("*** inside mortgageBackPage6b ***");

		CouchbaseServiceHelper couchbaseServiceHelper= new CouchbaseServiceHelper();
		Session session = Http.Context.current().session();
		MortgageApplicationPageService mService=new MortgageApplicationPageService();
		MortgageApplicationPageSixBService  mPageSixService=new MortgageApplicationPageSixBService();
		
		try{
			Opportunity opportunity=couchbaseServiceHelper.getCouhbaseDataByKey(mService.parseLeadId(session));
			PersonalInfo personalInfo = mPageSixService.getPersonalInfo(opportunity);
			Logger.debug("\n Work phone " + personalInfo.getCoWorkPhone()
					+ "\n home phone " + personalInfo.getCoHomePhone()
					+ "\n bityhday " + personalInfo.getCoBirthDay()
					+ "\n Social insurance: " + personalInfo.getCoSocialInsurance()
					+ "\n dependant " + personalInfo.getCoDependents()
					+ "\n are you " + personalInfo.getCoAreUCanadianRes());
			return ok(mortgagePage6b.render(personalInfo));
		}catch (MortgageApplicationPageServiceException e) {
			Logger.error("Error in mortgagePage6a", e);
			return ok(leadsucess.render(Constants.COUCHBASE_CONNECTION_FAILED_MESSAGE));
		}
		
	}

	public static Result mortgageBackPage7a() {
		
		Logger.debug("*** Inside (.) mortgageBackPage7a of Mortgage CTRL***");
		CouchbaseServiceHelper couchbaseServiceHelper= new CouchbaseServiceHelper();
		Session session = Http.Context.current().session();
		MortgageApplicationPageService mService=new MortgageApplicationPageService();
		MortgageApplicationPageSevenAService  mPage7AService=new MortgageApplicationPageSevenAService();
		
		try{
			Opportunity opportunity=couchbaseServiceHelper.getCouhbaseDataByKey(mService.parseLeadId(session));
			ApplicantAddressParameter7 addresses = mPage7AService.getApplicantAddresses(opportunity);
			
			return ok(mortgagePage7a.render(addresses));
		}catch (MortgageApplicationPageServiceException e) {
			Logger.error("Error in mortgagePage6a", e);
			return ok(leadsucess.render(Constants.COUCHBASE_CONNECTION_FAILED_MESSAGE));
		}
	}

	public static Result mortgageBackPage7b() {
		Logger.debug("*** Inside (.) mortgageBackPage7a of Mortgage CTRL***");
		String coApplicantID = "";
		Session session = Http.Context.current().session();
		try {
			coApplicantID = session.get("applicantID2");
		} catch (Exception e) {
			Logger.error("Error in getting values from session " + e);
		}
		ApplicantAddressParameter7 addresses = new ApplicantAddressParameter7();

		return ok(mortgagePage7a.render(addresses));
	}

	public static Result privacyPolice() {
		return ok(privacypolicy.render(""));
	}

}
