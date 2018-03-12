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
import uk.gov.hmrc.softdrinksindustrylevystub.models.internal.CreateFormat.subscriptionReads
import uk.gov.hmrc.softdrinksindustrylevystub.models.internal.Subscription
import uk.gov.hmrc.softdrinksindustrylevystub.models.{Return, ReturnFailureResponse, ReturnSuccessResponse, returnSuccessResponseFormat}
import uk.gov.hmrc.softdrinksindustrylevystub.services.DesSubmissionService

class ReturnControllerSpec extends PlaySpec with MockitoSugar with GuiceOneAppPerSuite with BeforeAndAfterEach {

  val mockDesSubmissionService: DesSubmissionService = mock[DesSubmissionService]
  val mockReturnController = new ReturnController(mockDesSubmissionService)
  val utr = "9024987803"
  val sdilRef = "XVSDIL000987654"
  val invalidSdilRef = "XVSDIL000987654WTF"
  val periodKey = "17C4"
  val authHeader: (String, String) = "Authorization" -> "auth"
  val envHeader: (String, String) = "Environment" -> "clone"
  val sdilIdType = "sdil"
  val subscription: Subscription = Json.fromJson[Subscription](validCreateSubscriptionRequestInput).get

  override def beforeEach() {
    reset(mockDesSubmissionService)
  }

  "ReturnController" should {

    "return 403 forbidden for no business partner record for given SDIL ref number " in {
      when(mockDesSubmissionService
        .retrieveSubscriptionDetails(sdilIdType, sdilRef)).thenReturn(None)

      val response = mockReturnController.createReturn(sdilRef)(FakeRequest("POST", "/soft-drinks/return/")
        .withBody(validReturnPayload)
        .withHeaders(envHeader, authHeader))

      status(response) mustBe FORBIDDEN

      Json.fromJson[ReturnFailureResponse](contentAsJson(response))
        .getOrElse(ReturnFailureResponse("UNKNOWN", "this didn't work")) mustBe ReturnFailureResponse.noBpKey

    }

    "return 400 bad request for malformed sdilRef parameter " in {

      when(mockDesSubmissionService.retrieveSubscriptionDetails(sdilIdType, sdilRef)).thenReturn(Some(subscription))

      val response = mockReturnController.createReturn(invalidSdilRef)(FakeRequest("POST", "/soft-drinks/return/")
        .withBody(validReturnPayload)
        .withHeaders(envHeader, authHeader))

      status(response) mustBe BAD_REQUEST
      Json.fromJson[ReturnFailureResponse](contentAsJson(response))
        .getOrElse(ReturnFailureResponse("UNKNOWN", "this didn't work")) mustBe ReturnFailureResponse.invalidSdilRef
    }

    "return 400 bad request for malformed periodKey in json payload " in {

      when(mockDesSubmissionService.retrieveSubscriptionDetails(sdilIdType, sdilRef)).thenReturn(Some(subscription))

      val response = mockReturnController.createReturn(sdilRef)(FakeRequest("POST", "/soft-drinks/return/")
        .withBody(invalidPeriodKeyReturnPayload)
        .withHeaders(envHeader, authHeader))

      status(response) mustBe BAD_REQUEST
      Json.fromJson[ReturnFailureResponse](contentAsJson(response))
        .getOrElse(ReturnFailureResponse("UNKNOWN", "this didn't work")) mustBe ReturnFailureResponse.invalidPeriodKey
    }

    "return 409 conflict for already filed return " in {
      when(mockDesSubmissionService.retrieveSubscriptionDetails(sdilIdType, sdilRef)).thenReturn(Some(subscription))
      when(mockDesSubmissionService.checkForExistingReturn(sdilRef + periodKey)).thenReturn(true)

      val response = mockReturnController.createReturn(sdilRef)(FakeRequest("POST", "/soft-drinks/return/")
        .withBody(validReturnPayload)
        .withHeaders(envHeader, authHeader))

      status(response) mustBe CONFLICT
      Json.fromJson[ReturnFailureResponse](contentAsJson(response))
        .getOrElse(ReturnFailureResponse("UNKNOWN", "this didn't work")) mustBe ReturnFailureResponse.obligationFilled
    }

    "return 200 for successful return " in {

      val ret = Json.fromJson[Return](validReturnPayload).get
      val res = Json.fromJson[ReturnSuccessResponse](validReturnResponse).get

      when(mockDesSubmissionService.retrieveSubscriptionDetails(sdilIdType, sdilRef)).thenReturn(Some(subscription))
      when(mockDesSubmissionService.createReturnResponse(ret, sdilRef)).thenReturn(res)

      val response = mockReturnController.createReturn(sdilRef)(FakeRequest("POST", "/soft-drinks/return/")
        .withBody(validReturnPayload)
        .withHeaders(envHeader, authHeader))

      status(response) mustBe OK
      Json.fromJson[ReturnSuccessResponse](contentAsJson(response))
        .getOrElse(Nil) mustBe res

    }

    "return 400 bad request when sending invalid return  payload " in {

      when(mockDesSubmissionService.retrieveSubscriptionDetails(sdilIdType, sdilRef)).thenReturn(Some(subscription))

      val response = mockReturnController.createReturn(sdilRef)(FakeRequest("POST", "/soft-drinks/return/")
        .withBody(invalidReturnPayload)
        .withHeaders(envHeader, authHeader))

      status(response) mustBe BAD_REQUEST
      Json.fromJson[ReturnFailureResponse](contentAsJson(response))
        .getOrElse(ReturnFailureResponse("UNKNOWN", "this didn't work")) mustBe ReturnFailureResponse.invalidPayload

    }
  }
}
