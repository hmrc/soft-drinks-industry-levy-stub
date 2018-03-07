/*
 * Copyright 2018 HM Revenue & Customs
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

import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.softdrinksindustrylevystub.models.{Return, ReturnSuccessResponse}
import uk.gov.hmrc.softdrinksindustrylevystub.models.internal.CreateFormat.subscriptionReads
import uk.gov.hmrc.softdrinksindustrylevystub.models.internal.Subscription
import uk.gov.hmrc.softdrinksindustrylevystub.services.DesSubmissionService

class ReturnControllerSpec extends PlaySpec with MockitoSugar with GuiceOneAppPerSuite with BeforeAndAfterEach {

  val mockDesSubmissionService: DesSubmissionService = mock[DesSubmissionService]
  val mockReturnController = new ReturnController(mockDesSubmissionService)
  val utr = "9024987803"
  val sdilNumber = "XVSDIL000987654"
  val periodKey = "18C1"
  val authHeader: (String, String) = "Authorization" -> "auth"
  val envHeader: (String, String) = "Environment" -> "clone"
  val badEnvHeader: (String, String) = "Environment" -> "test"
  val sdilIdType = "sdil"
  val subscription: Subscription = Json.fromJson[Subscription](validCreateSubscriptionRequestInput).get



  override def beforeEach() {
    reset(mockDesSubmissionService)
  }

  "ReturnController" should {

    "return 403 forbidden for no business partner record for given SDIL ref number " in {
      when(mockDesSubmissionService
        .retrieveSubscriptionDetails(sdilIdType, sdilNumber)).thenReturn(None)

      val response = mockReturnController.createReturn(FakeRequest("POST", "/soft-drinks/return/")
        .withBody(validReturnPayload)
        .withHeaders(envHeader, authHeader))

      status(response) mustBe FORBIDDEN
    }

    "return 400 bad request for malformed periodKey in json payload " in {
//      val r: Subscription = Json.fromJson[Subscription](validCreateSubscriptionRequestInput).get

      when(mockDesSubmissionService.retrieveSubscriptionDetails(sdilIdType, sdilNumber)).thenReturn(Some(subscription))

      val response = mockReturnController.createReturn(FakeRequest("POST", "/soft-drinks/return/")
        .withBody(invalidPeriodKeyReturnPayload)
        .withHeaders(envHeader, authHeader))

      status(response) mustBe BAD_REQUEST
    }

    "return 403 forbidden for already filed return " in {

      when(mockDesSubmissionService.retrieveSubscriptionDetails(sdilIdType, sdilNumber)).thenReturn(None)
      when(mockDesSubmissionService.checkForExistingReturn(sdilNumber + periodKey)).thenReturn(true)

      val response = mockReturnController.createReturn(FakeRequest("POST", "/soft-drinks/return/")
        .withBody(validReturnPayload)
        .withHeaders(envHeader, authHeader))

      status(response) mustBe FORBIDDEN
    }

    "return 200 for successful return " in {

//      val r: Subscription = Json.fromJson[Subscription](validCreateSubscriptionRequestInput).get
      val ret = Json.fromJson[Return](validReturnPayload).get
      val res = Json.fromJson[ReturnSuccessResponse](validReturnResponse).get

      when(mockDesSubmissionService.retrieveSubscriptionDetails(sdilIdType, sdilNumber)).thenReturn(Some(subscription))
      when(mockDesSubmissionService.createReturnResponse(ret)).thenReturn(res)

      val response = mockReturnController.createReturn(FakeRequest("POST", "/soft-drinks/return/")
        .withBody(validReturnPayload)
        .withHeaders(envHeader, authHeader))

      status(response) mustBe OK
    }

  }
}
