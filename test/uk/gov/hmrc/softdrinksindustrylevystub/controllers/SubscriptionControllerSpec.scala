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

import org.mockito.Mockito.reset
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.softdrinksindustrylevystub.services.DesSubmissionService

class SubscriptionControllerSpec extends PlaySpec with MockitoSugar with GuiceOneAppPerSuite with BeforeAndAfterEach {
  val mockDesSubmissionService: DesSubmissionService = mock[DesSubmissionService]
  val mockSubscriptionController = new SubscriptionController(mockDesSubmissionService)

  override def beforeEach() {
    reset(mockDesSubmissionService)
  }

  "SubscriptionController" should {
    "return Status: OK Body: CreateSubscriptionResponse for successful valid CreateSubscriptionRequest" in {
      implicit val hc = new HeaderCarrier

      val input = """{
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
                        "addressNotInUk" : false,
                        "addressLine1" : "line1",
                        "addressLine2" : "line2",
                        "postcode" : "ABC123",
                        "telephoneNumber" : "123123123",
                        "emailAddress" : "foo@bar.com"
                      },
                      "correspondenceAddressDiffers" : true,
                      "correspondenceAddress" : {
                        "addressNotInUk" : false,
                        "addressLine1" : "line3",
                        "addressLine2" : "line4",
                        "postcode" : "XYZ123",
                        "telephoneNumber" : "123123123",
                        "emailAddress" : "foo@bar.com"
                      },
                      "primaryPerson" : {
                        "name" : "foo",
                        "telephoneNumber" : "123123123",
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
                        "address" : {
                          "addressNotInUk" : false,
                          "addressLine1" : "line1",
                          "addressLine2" : "line2",
                          "postcode" : "ABC123",
                          "telephoneNumber" : "123123123",
                          "emailAddress" : "foo@bar.com"
                        },
                        "typeOfSite" : "Warehouse"
                      } ]
                    }
                    """

//      when(mockDesSubmissionService.createSubscriptionResponse(input)).thenReturn(CreateSubscriptionResponse)
      val response = mockSubscriptionController.createSubscription()(FakeRequest("POST", "/create-subscription").withBody(Json.parse(input)))

      status(response) mustBe OK
//      verify(mockDesSubmissionService, times(1)).buildResponse()
//      Json.fromJson[DesSubmissionResult](contentAsJson(response)).getOrElse(DesSubmissionResult(false)) mustBe DesSubmissionResult(true)
    }

//    "return Status: OK Body: DesSubmissionResult(false) for successful invalid submitDesRequest" in {
//      implicit val hc = new HeaderCarrier
//
//      val input = """{ "number": 1 }"""
//
//      when(mockDesSubmissionService.buildResponse()).thenReturn(DesSubmissionResult(false))
//      val response = mockSubscriptionController.submitHello()(FakeRequest("POST", "/des-valid").withBody(Json.parse(input)))
//
//      status(response) mustBe OK
//      verify(mockDesSubmissionService, times(1)).buildResponse()
//      Json.fromJson[DesSubmissionResult](contentAsJson(response)).getOrElse(DesSubmissionResult(true)) mustBe DesSubmissionResult(false)
//    }
  }
}
