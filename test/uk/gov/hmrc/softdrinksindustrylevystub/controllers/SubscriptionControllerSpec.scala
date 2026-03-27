/*
 * Copyright 2024 HM Revenue & Customs
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

import java.time.{LocalDate, LocalDateTime, OffsetDateTime, ZoneOffset}
import org.mockito.ArgumentMatchers.any
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.softdrinksindustrylevystub.models.CreateSubscriptionResponse
import uk.gov.hmrc.softdrinksindustrylevystub.models.internal.*
import uk.gov.hmrc.softdrinksindustrylevystub.services.DesSubmissionService
import uk.gov.hmrc.softdrinksindustrylevystub.models.createSubscriptionResponseFormat

import scala.concurrent.ExecutionContext.Implicits.global
import org.scalatestplus.mockito.MockitoSugar.mock
import org.mockito.Mockito.{reset, times, verify, when}

class SubscriptionControllerSpec extends PlaySpec with GuiceOneAppPerSuite with BeforeAndAfterEach {
  val mockDesSubmissionService: DesSubmissionService = mock[DesSubmissionService]
  val cc = stubControllerComponents()
  val authorisedFilterAction = new AuthorisedFilterAction(cc)
  val environmentAction = new EnvironmentFilterAction()
  val extraActions = new ExtraActions(authorisedFilterAction, environmentAction)
  val mockSubscriptionController = new SubscriptionController(mockDesSubmissionService, cc, extraActions)
  implicit val hc: HeaderCarrier = new HeaderCarrier
  val utr = "1111111111"
  val sdilNumber = "XMSDIL000830000"
  val utrIdType = "utr"
  val sdilIdType = "sdil"
  val now: OffsetDateTime = LocalDateTime.now.atOffset(ZoneOffset.UTC)
  val authHeader: (String, String) = "Authorization" -> "auth"
  val envHeader: (String, String) = "Environment"    -> "clone"
  val badEnvHeader: (String, String) = "Environment" -> "test"

  override def beforeEach(): Unit =
    reset(mockDesSubmissionService)

  "SubscriptionController" should {

    "return Status: OK Body: CreateSubscriptionResponse for successful valid CreateSubscriptionRequest" in {
      when(
        mockDesSubmissionService
          .createSubscriptionResponse(any(), any())
      ).thenReturn(CreateSubscriptionResponse(now, "bar"))

      val response = mockSubscriptionController
        .createSubscription(utrIdType, utr)(
          FakeRequest("POST", "/soft-drinks/subscription")
            .withBody(validCreateSubscriptionRequestInput)
            .withHeaders(envHeader, authHeader)
        )

      status(response) mustBe OK
      verify(mockDesSubmissionService, times(1)).createSubscriptionResponse(any(), any())
      Json
        .fromJson[CreateSubscriptionResponse](contentAsJson(response))
        .getOrElse(CreateSubscriptionResponse(now, "foo")) mustBe CreateSubscriptionResponse(now, "bar")
    }

    "return Status: OK Body: CreateSubscriptionResponse for successful valid CreateSubscriptionRequest " +
      "without all optional data" in {
        when(
          mockDesSubmissionService
            .createSubscriptionResponse(any(), any())
        ).thenReturn(CreateSubscriptionResponse(now, "bar"))

        val response = mockSubscriptionController
          .createSubscription(utrIdType, utr)(
            FakeRequest("POST", "/soft-drinks/subscription")
              .withBody(validCreateSubscriptionRequestInputWithoutOptionals)
              .withHeaders(envHeader, authHeader)
          )

        status(response) mustBe OK
        verify(mockDesSubmissionService, times(1)).createSubscriptionResponse(any(), any())
        Json
          .fromJson[CreateSubscriptionResponse](contentAsJson(response))
          .getOrElse(CreateSubscriptionResponse(now, "foo")) mustBe CreateSubscriptionResponse(now, "bar")
      }

    "return Status: 400 Body: nondescript error message for submission for invalid CreateSubscriptionRequest" in {
      val response = mockSubscriptionController
        .createSubscription(utrIdType, utr)(
          FakeRequest("POST", "/soft-drinks/subscription")
            .withBody(invalidCreationInput)
            .withHeaders(envHeader, authHeader)
        )

      status(response) mustBe BAD_REQUEST
      verify(mockDesSubmissionService, times(0)).createSubscriptionResponse(any(), any())
    }

    "return Status: 401 for submission without auth header" in {
      val response = mockSubscriptionController
        .createSubscription(utrIdType, utr)(
          FakeRequest("POST", "/soft-drinks/subscription")
            .withBody(invalidCreationInput)
            .withHeaders(envHeader)
        )

      status(response) mustBe UNAUTHORIZED
      verify(mockDesSubmissionService, times(0)).createSubscriptionResponse(any(), any())
    }

    "return Status: 403 for submission without environment header" in {
      val response = mockSubscriptionController
        .createSubscription(utrIdType, utr)(
          FakeRequest("POST", "/soft-drinks/subscription")
            .withBody(invalidCreationInput)
            .withHeaders(authHeader)
        )

      status(response) mustBe FORBIDDEN
      verify(mockDesSubmissionService, times(0)).createSubscriptionResponse(any(), any())
    }

    "return Status: 403 for submission with bad environment header" in {
      val response = mockSubscriptionController
        .createSubscription(utrIdType, utr)(
          FakeRequest("POST", "/soft-drinks/subscription")
            .withBody(invalidCreationInput)
            .withHeaders(authHeader, badEnvHeader)
        )

      status(response) mustBe FORBIDDEN
      verify(mockDesSubmissionService, times(0)).createSubscriptionResponse(any(), any())
    }
  }
}
