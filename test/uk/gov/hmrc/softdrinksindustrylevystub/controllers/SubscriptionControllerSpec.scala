/*
 * Copyright 2017 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.softdrinksindustrylevystub.controllers

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.softdrinksindustrylevystub.models.etmp.createsub.{CreateSubscriptionRequest, CreateSubscriptionResponse}
import uk.gov.hmrc.softdrinksindustrylevystub.services.DesSubmissionService

class SubscriptionControllerSpec extends PlaySpec with MockitoSugar with GuiceOneAppPerSuite with BeforeAndAfterEach {
  val mockDesSubmissionService: DesSubmissionService = mock[DesSubmissionService]
  val mockSubscriptionController = new SubscriptionController(mockDesSubmissionService)
  implicit val hc: HeaderCarrier = new HeaderCarrier

  override def beforeEach() {
    reset(mockDesSubmissionService)
  }

  "SubscriptionController" should {

    "return Status: 404 Body: CreateSubscriptionRequest for a unsuccessful retrieve request" in {
      val utr = "1097172565"

      when(mockDesSubmissionService.retrieveSubscriptionDetails(utr)).thenReturn(None)
      val response = mockSubscriptionController.retrieveSubscriptionDetails(utr)(FakeRequest("GET", "/retrieve-subscription-details/"))

      status(response) mustBe NOT_FOUND
    }

    "return Status: OK Body: CreateSubscriptionRequest for a successful retrieve request" in {
      val output = Json.parse("""{
                      "organisationType" : "LimitedCompany",
                      "action" : "Add",
                      "typeOfEntity" : "GroupMember",
                      "dateOfApplication" : "2017-09-12",
                      "taxStartDate" : "2017-09-12",
                      "joiningDate" : "2017-09-12",
                      "leavingDate" : "2017-09-12",
                      "customerIdentificationNumber" : "1097172564",
                      "tradingName" : "some trading name",
                      "businessContactDetails" : {
                        "addressNotInUk" : true,
                        "addressLine1" : "line1",
                        "addressLine2" : "line2",
                        "addressLine3" : "line3",
                        "addressLine4" : "line4",
                        "postcode" : "XYZ123",
                        "nonUkCountry" : "Ukraine",
                        "telephoneNumber" : "123123123",
                        "mobileNumber": "123123123",
                        "emailAddress" : "foo@bar.com",
                        "faxNumber": "123123123"
                      },
                      "correspondenceAddressDiffers" : true,
                      "correspondenceAddress" : {
                        "addressNotInUk" : true,
                        "addressLine1" : "line1",
                        "addressLine2" : "line2",
                        "addressLine3" : "line3",
                        "addressLine4" : "line4",
                        "postcode" : "XYZ123",
                        "nonUkCountry" : "Uganda",
                        "telephoneNumber" : "123123123",
                        "mobileNumber" : "123123123",
                        "emailAddress" : "foo@bar.com",
                        "faxNumber": "123123123"
                      },
                      "primaryPerson" : {
                        "name" : "foo",
                        "positionInCompany" : "Boss",
                        "telephoneNumber" : "123123123",
                        "mobileNumber" : "123123123",
                        "emailAddress" : "foo@bar.com"
                      },
                      "softDrinksIndustryLevyDetails" : {
                        "activities" : "ContractPacker",
                        "lessThanMillion" : true,
                        "producerClassification" : "Large",
                        "smallProducerExemption" : false,
                        "usesCopacker" : false,
                        "voluntarilyRegistered" : false
                      },
                      "sdilActivity" : {
                        "ProducedLower" : 1,
                        "ProducedHigher" : 2,
                        "ImportedLower" : 3,
                        "ImportedHigher" : 4,
                        "PackagedLower" : 5,
                        "PackagedHigher" : 6
                      },
                      "estimatedAmountOfTaxInTheNext12Months" : 5000,
                      "taxObligationStartDate" : "2017-09-12",
                      "bankDetails" : {
                        "directDebit" : false,
                        "accountName" : "some account name",
                        "accountNumber" : "some account number",
                        "sortCode" : "some sort code",
                        "buildingSocietyRollNumber" : "building society roll number"
                      },
                      "sites" : [ {
                        "action" : "NewSite",
                        "siteReference" : "foo",
                        "dateOfClosure" : "2017-09-12",
                        "siteClosureReason" : "rats",
                        "tradingName" : "cats",
                        "newSiteReference" : "foobar",
                        "address" : {
                          "addressNotInUk" : true,
                          "addressLine1" : "line1",
                          "addressLine2" : "line2",
                          "addressLine3" : "line3",
                          "addressLine4" : "line4",
                          "postcode" : "XYZ123",
                          "nonUkCountry" : "Uganda",
                          "telephoneNumber" : "123123123",
                          "mobileNumber": "123123123",
                          "emailAddress" : "foo@bar.com",
                          "faxNumber": "123123123"
                        },
                        "typeOfSite" : "Warehouse"
                      } ]
                    }
                    """)
      val utr = "1097172565"
      val r = Json.fromJson[CreateSubscriptionRequest](output)

      when(mockDesSubmissionService.retrieveSubscriptionDetails(utr)).thenReturn(Some(r.get))
      val response = mockSubscriptionController.retrieveSubscriptionDetails(utr)(FakeRequest("GET", "/retrieve-subscription-details/"))

      status(response) mustBe OK
      verify(mockDesSubmissionService, times(1)).retrieveSubscriptionDetails(any())
      contentAsJson(response).mustBe(output)
    }

    "return Status: OK Body: CreateSubscriptionResponse for successful valid CreateSubscriptionRequest with all optional data" in {
      val input = Json.parse("""{
                      "organisationType" : "LimitedCompany",
                      "action" : "Add",
                      "typeOfEntity" : "GroupMember",
                      "dateOfApplication" : "2017-09-12",
                      "taxStartDate" : "2017-09-12",
                      "joiningDate" : "2017-09-12",
                      "leavingDate" : "2017-09-12",
                      "customerIdentificationNumber" : "1097172564",
                      "tradingName" : "some trading name",
                      "businessContactDetails" : {
                        "addressNotInUk" : true,
                        "addressLine1" : "line1",
                        "addressLine2" : "line2",
                        "addressLine3" : "line3",
                        "addressLine4" : "line4",
                        "postcode" : "XYZ123",
                        "nonUkCountry" : "Ukraine",
                        "telephoneNumber" : "123123123",
                        "mobileNumber": "123123123",
                        "emailAddress" : "foo@bar.com",
                        "faxNumber": "123123123"
                      },
                      "correspondenceAddressDiffers" : true,
                      "correspondenceAddress" : {
                        "addressNotInUk" : true,
                        "addressLine1" : "line1",
                        "addressLine2" : "line2",
                        "addressLine3" : "line3",
                        "addressLine4" : "line4",
                        "postcode" : "XYZ123",
                        "nonUkCountry" : "Uganda",
                        "telephoneNumber" : "123123123",
                        "mobileNumber" : "123123123",
                        "emailAddress" : "foo@bar.com",
                        "faxNumber": "123123123"
                      },
                      "primaryPerson" : {
                        "name" : "foo",
                        "positionInCompany" : "Boss",
                        "telephoneNumber" : "123123123",
                        "mobileNumber" : "123123123",
                        "emailAddress" : "foo@bar.com"
                      },
                      "softDrinksIndustryLevyDetails" : {
                        "activities" : "ContractPacker",
                        "lessThanMillion" : true,
                        "producerClassification" : "Large",
                        "smallProducerExemption" : false,
                        "usesCopacker" : false,
                        "voluntarilyRegistered" : false
                      },
                      "sdilActivity" : {
                        "ProducedLower" : 1,
                        "ProducedHigher" : 2,
                        "ImportedLower" : 3,
                        "ImportedHigher" : 4,
                        "PackagedLower" : 5,
                        "PackagedHigher" : 6
                      },
                      "estimatedAmountOfTaxInTheNext12Months" : 5000,
                      "taxObligationStartDate" : "2017-09-12",
                      "bankDetails" : {
                        "directDebit" : false,
                        "accountName" : "some account name",
                        "accountNumber" : "some account number",
                        "sortCode" : "some sort code",
                        "buildingSocietyRollNumber" : "building society roll number"
                      },
                      "sites" : [ {
                        "action" : "NewSite",
                        "siteReference" : "foo",
                        "dateOfClosure" : "2017-09-12",
                        "siteClosureReason" : "rats",
                        "tradingName" : "cats",
                        "newSiteReference" : "foobar",
                        "address" : {
                          "addressNotInUk" : true,
                          "addressLine1" : "line1",
                          "addressLine2" : "line2",
                          "addressLine3" : "line3",
                          "addressLine4" : "line4",
                          "postcode" : "XYZ123",
                          "nonUkCountry" : "Uganda",
                          "telephoneNumber" : "123123123",
                          "mobileNumber": "123123123",
                          "emailAddress" : "foo@bar.com",
                          "faxNumber": "123123123"
                        },
                        "typeOfSite" : "Warehouse"
                      } ]
                    }
                    """)

      when(mockDesSubmissionService.createSubscriptionResponse(any())).thenReturn(CreateSubscriptionResponse("foo", "bar"))
      val response = mockSubscriptionController.createSubscription()(FakeRequest("POST", "/create-subscription").withBody(input))

      status(response) mustBe OK
      verify(mockDesSubmissionService, times(1)).createSubscriptionResponse(any())
      Json.fromJson[CreateSubscriptionResponse](contentAsJson(response)).getOrElse(CreateSubscriptionResponse("bar", "foo")) mustBe CreateSubscriptionResponse("foo", "bar")
    }

    "return Status: OK Body: CreateSubscriptionResponse for successful valid CreateSubscriptionRequest without all optional data" in {
      val input = Json.parse("""{
                      "organisationType" : "LimitedCompany",
                      "dateOfApplication" : "2017-09-12",
                      "taxStartDate" : "2017-09-12",
                      "customerIdentificationNumber" : "1097172564",
                      "tradingName" : "some trading name",
                      "businessContactDetails" : {
                        "addressNotInUk" : false,
                        "addressLine1" : "line1",
                        "addressLine2" : "line2",
                        "postcode" : "ABC123",
                        "telephoneNumber" : "123123123",
                        "emailAddress" : "foo@bar.com"
                      },
                      "correspondenceAddressDiffers" : false,
                      "primaryPerson" : {
                        "name" : "foo",
                        "telephoneNumber" : "123123123",
                        "emailAddress" : "foo@bar.com"
                      },
                      "softDrinksIndustryLevyDetails" : {
                        "activities" : "ContractPacker",
                        "lessThanMillion" : true,
                        "smallProducerExemption" : false,
                        "usesCopacker" : false,
                        "voluntarilyRegistered" : false
                      },
                      "sdilActivity" : {
                      },
                      "taxObligationStartDate" : "2017-09-12",
                      "bankDetails" : {
                        "directDebit" : false
                      },
                      "sites" : [ {
                        "address" : {
                          "addressNotInUk" : false,
                          "addressLine1" : "line1",
                          "addressLine2" : "line2",
                          "postcode" : "ABC123",
                          "telephoneNumber" : "123123123",
                          "emailAddress" : "foo@bar.com"
                        }
                      } ]
                    }
                    """)

      when(mockDesSubmissionService.createSubscriptionResponse(any())).thenReturn(CreateSubscriptionResponse("foo", "bar"))
      val response = mockSubscriptionController.createSubscription()(FakeRequest("POST", "/create-subscription").withBody(input))

      status(response) mustBe OK
      verify(mockDesSubmissionService, times(1)).createSubscriptionResponse(any())
      Json.fromJson[CreateSubscriptionResponse](contentAsJson(response)).getOrElse(CreateSubscriptionResponse("bar", "foo")) mustBe CreateSubscriptionResponse("foo", "bar")
    }

    "return Status: 400 Body: CreateSubscriptionResponse for invalid CreateSubscriptionRequest" in {
      val input = Json.parse("""{
                      "organisationType" : "LimitedCompany BORKED",
                      "dateOfApplication" : "2017-09-12",
                      "taxStartDate" : "2017-09-12",
                      "customerIdentificationNumber" : "1097172564",
                      "tradingName" : "some trading name",
                      "businessContactDetails" : {
                        "addressNotInUk" : false,
                        "addressLine1" : "line1",
                        "addressLine2" : "line2",
                        "postcode" : "ABC123",
                        "telephoneNumber" : "123123123",
                        "emailAddress" : "foo@bar.com"
                      },
                      "correspondenceAddressDiffers" : false,
                      "primaryPerson" : {
                        "name" : "foo",
                        "telephoneNumber" : "123123123",
                        "emailAddress" : "foo@bar.com"
                      },
                      "softDrinksIndustryLevyDetails" : {
                        "activities" : "ContractPacker",
                        "lessThanMillion" : true,
                        "smallProducerExemption" : false,
                        "usesCopacker" : false,
                        "voluntarilyRegistered" : false
                      },
                      "sdilActivity" : {
                      },
                      "taxObligationStartDate" : "2017-09-12",
                      "bankDetails" : {
                        "directDebit" : false
                      },
                      "sites" : [ {
                        "address" : {
                          "addressNotInUk" : false,
                          "addressLine1" : "line1",
                          "addressLine2" : "line2",
                          "postcode" : "ABC123",
                          "telephoneNumber" : "123123123",
                          "emailAddress" : "foo@bar.com"
                        }
                      } ]
                    }
                    """)

      val response = mockSubscriptionController.createSubscription()(FakeRequest("POST", "/create-subscription").withBody(input))

      status(response) mustBe BAD_REQUEST
      verify(mockDesSubmissionService, times(0)).createSubscriptionResponse(any())
    }

  }
}
