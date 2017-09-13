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

package uk.gov.hmrc.softdrinksindustrylevystub

import play.api.libs.json._

package object models {
  implicit val desSubmissionRequestFormatter: Format[DesSubmissionRequest] = Json.format[DesSubmissionRequest]
  implicit val desSubmissionResultFormatter: Format[DesSubmissionResult] = Json.format[DesSubmissionResult]

  implicit val subscriptionDetailsFormatter: Format[SubscriptionDetails] = Json.format[SubscriptionDetails]
  implicit val addressDetailsFormatter: Format[AddressDetails] = Json.format[AddressDetails]
  implicit val relationshipDetailsFormatter: Format[RelationshipDetails] = Json.format[RelationshipDetails]
  implicit val bankDetailsFormatter: Format[BankDetails] = Json.format[BankDetails]
  implicit val siteDetailsFormatter: Format[SiteDetails] = Json.format[SiteDetails]
  implicit val getSubscriptionResponseFormatter: Format[GetSubscriptionResponse] = Json.format[GetSubscriptionResponse]
}
