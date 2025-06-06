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

package uk.gov.hmrc.softdrinksindustrylevystub.models

import play.api.libs.json.{Format, Json}

case class RosmRegisterResponse(
  safeId: String,
  agentReferenceNumber: Option[String],
  isEditable: Boolean,
  isAnAgent: Boolean,
  isAnIndividual: Boolean,
  individual: Option[Individual] = None,
  organisation: Option[OrganisationResponse] = None,
  address: RosmResponseAddress,
  contactDetails: RosmResponseContactDetails
)

object RosmRegisterResponse {
  implicit val format: Format[RosmRegisterResponse] = Json.format[RosmRegisterResponse]
}

case class OrganisationResponse(
  organisationName: String,
  isAGroup: Boolean,
  organisationType: RosmOrganisationType.Value
)

case class RosmResponseAddress(
  addressLine1: String,
  addressLine2: Option[String],
  addressLine3: Option[String],
  addressLine4: Option[String],
  countryCode: String,
  postalCode: String
)

case class RosmResponseContactDetails(
  primaryPhoneNumber: Option[String],
  secondaryPhoneNumber: Option[String],
  faxNumber: Option[String],
  emailAddress: Option[String]
)
