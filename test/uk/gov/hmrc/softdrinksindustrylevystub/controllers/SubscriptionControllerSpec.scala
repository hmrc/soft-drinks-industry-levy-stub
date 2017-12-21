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

import java.time.{LocalDate, LocalDateTime, OffsetDateTime, ZoneOffset}

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.softdrinksindustrylevystub.models.CreateSubscriptionResponse
import uk.gov.hmrc.softdrinksindustrylevystub.models.internal._
import uk.gov.hmrc.softdrinksindustrylevystub.services.DesSubmissionService

class SubscriptionControllerSpec extends PlaySpec with MockitoSugar with GuiceOneAppPerSuite with BeforeAndAfterEach {
  val mockDesSubmissionService: DesSubmissionService = mock[DesSubmissionService]
  val mockSubscriptionController = new SubscriptionController(mockDesSubmissionService)
  implicit val hc: HeaderCarrier = new HeaderCarrier
  val utr = "1097172565"
  val idType = "utr"
  val now: OffsetDateTime = LocalDateTime.now.atOffset(ZoneOffset.UTC)
  val authHeader: (String, String) = "Authorization" -> "auth"
  val envHeader: (String, String) = "Environment" -> "clone"
  val badEnvHeader: (String, String) = "Environment" -> "test"

  override def beforeEach() {
    reset(mockDesSubmissionService)
  }

  "SubscriptionController" should {

    "return Status: 404 Body: reason: unknown subscription message for a unsuccessful retrieve request" in {
      when(mockDesSubmissionService
        .retrieveSubscriptionDetails(utr)).thenReturn(None)
      val response = mockSubscriptionController
        .retrieveSubscriptionDetails(idType, utr)(FakeRequest("GET", "/soft-drinks/subscription/")
        .withHeaders(envHeader, authHeader))

      status(response) mustBe NOT_FOUND
    }

    "return OK and a Subscription for a successful retrieve request" in {
      val r: Subscription = Subscription(
        "1097172564",
        "a",
        Some("1"),
        UkAddress(List("Juicey Juices", "Some Street"), "AB012AA"),
        InternalActivity(
          Map(
            ActivityType.ProducedOwnBrand -> ((2L, 2L)),
            ActivityType.Imported -> ((2L, 2L))
          )
        ),
        LocalDate.of(1920, 2, 29),
        List(
          Site(ForeignAddress(List("Juicey Juices", "Juicey Juices"), "FR"), Some("a")),
          Site(ForeignAddress(List("asdasdasd", "asfdsdasd"), "DE"), Some("a"))
        ),
        List(),
        Contact(Some("a"), Some("a"), "+44 1234567890", "a.b@c.com"))

      when(mockDesSubmissionService.retrieveSubscriptionDetails(utr)).thenReturn(Some(r))

      val response = mockSubscriptionController
        .retrieveSubscriptionDetails(idType, utr)(FakeRequest("GET", "/soft-drinks/subscription/")
        .withHeaders(envHeader, authHeader))

      status(response) mustBe OK
      verify(mockDesSubmissionService, times(1)).retrieveSubscriptionDetails(any())
      contentAsJson(response).mustBe(successfulRetrieveOutput)
    }

    "return Status: OK Body: CreateSubscriptionResponse for successful valid CreateSubscriptionRequest" in {
      when(mockDesSubmissionService
        .createSubscriptionResponse(any(),any())).thenReturn(CreateSubscriptionResponse(now, "bar"))

      val response = mockSubscriptionController
        .createSubscription(idType, utr)(FakeRequest("POST", "/soft-drinks/subscription")
          .withBody(validCreateSubscriptionRequestInput)
          .withHeaders(envHeader, authHeader))

      status(response) mustBe OK
      verify(mockDesSubmissionService, times(1)).createSubscriptionResponse(any(), any())
      Json.fromJson[CreateSubscriptionResponse](contentAsJson(response))
        .getOrElse(CreateSubscriptionResponse(now, "foo")) mustBe CreateSubscriptionResponse(now, "bar")
    }

    "return Status: OK Body: CreateSubscriptionResponse for successful valid CreateSubscriptionRequest " +
      "without all optional data" in {
      when(mockDesSubmissionService
        .createSubscriptionResponse(any(),any())).thenReturn(CreateSubscriptionResponse(now, "bar"))

      val response = mockSubscriptionController
        .createSubscription(idType, utr)(FakeRequest("POST", "/soft-drinks/subscription")
          .withBody(validCreateSubscriptionRequestInputWithoutOptionals)
          .withHeaders(envHeader, authHeader))

      status(response) mustBe OK
      verify(mockDesSubmissionService, times(1)).createSubscriptionResponse(any(),any())
      Json.fromJson[CreateSubscriptionResponse](contentAsJson(response))
        .getOrElse(CreateSubscriptionResponse(now, "foo")) mustBe CreateSubscriptionResponse(now, "bar")
    }

    "return Status: 400 Body: nondescript error message for submission for invalid CreateSubscriptionRequest" in {
      val response = mockSubscriptionController
        .createSubscription(idType, utr)(
          FakeRequest("POST", "/soft-drinks/subscription")
            .withBody(invalidCreationInput)
            .withHeaders(envHeader, authHeader))

      status(response) mustBe BAD_REQUEST
      verify(mockDesSubmissionService, times(0)).createSubscriptionResponse(any(),any())
    }

    "return Status: 401 for submission without auth header" in {
      val response = mockSubscriptionController
        .createSubscription(idType, utr)(
          FakeRequest("POST", "/soft-drinks/subscription")
            .withBody(invalidCreationInput)
            .withHeaders(envHeader))

      status(response) mustBe UNAUTHORIZED
      verify(mockDesSubmissionService, times(0)).createSubscriptionResponse(any(),any())
    }

    "return Status: 403 for submission without environment header" in {
      val response = mockSubscriptionController
        .createSubscription(idType, utr)(
          FakeRequest("POST", "/soft-drinks/subscription")
            .withBody(invalidCreationInput)
            .withHeaders(authHeader))

      status(response) mustBe FORBIDDEN
      verify(mockDesSubmissionService, times(0)).createSubscriptionResponse(any(),any())
    }

    "return Status: 403 for submission with bad environment header" in {
      val response = mockSubscriptionController
        .createSubscription(idType, utr)(
          FakeRequest("POST", "/soft-drinks/subscription")
            .withBody(invalidCreationInput)
            .withHeaders(authHeader, badEnvHeader))

      status(response) mustBe FORBIDDEN
      verify(mockDesSubmissionService, times(0)).createSubscriptionResponse(any(),any())
    }

  }
}
