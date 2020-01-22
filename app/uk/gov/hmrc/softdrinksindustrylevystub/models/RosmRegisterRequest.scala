/*
 * Copyright 2020 HM Revenue & Customs
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

import java.time.LocalDate

case class RosmRegisterRequest(
  regime: String,
  requiresNameMatch: Boolean,
  isAnAgent: Boolean,
  individual: Option[Individual] = None,
  organisation: Option[OrganisationRequest] = None
)

case class Individual(
  firstName: String,
  middleName: Option[String],
  lastName: String,
  dateOfBirth: Option[LocalDate]
)

case class OrganisationRequest(
  organisationName: String,
  organisationType: RosmOrganisationType.Value
)

object RosmOrganisationType extends Enumeration {
  val Partnership, LLP = Value
  val CorporateBody = Value("Corporate body")
  val UnincorporatedBody = Value("Unincorporated body")
  val Unknown = Value("Not Specified")
}
