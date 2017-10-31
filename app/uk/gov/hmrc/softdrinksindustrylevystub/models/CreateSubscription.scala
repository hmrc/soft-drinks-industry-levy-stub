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

package uk.gov.hmrc.softdrinksindustrylevystub.models.etmp.createsub

import java.time.{LocalDateTime, LocalDate => Date}

import uk.gov.hmrc.softdrinksindustrylevystub.models.EnumUtils

case class Address(
                    notUKAddress: Boolean,
                    line1: String,
                    line2: String,
                    line3: Option[String],
                    line4: Option[String],
                    postCode: Option[String],
                    country: Option[String]
                  )

case class ContactDetails(
                           telephone: String,
                           mobile: Option[String],
                           fax: Option[String],
                           email: String
                         )

case class BusinessContact(
                          addressDetails: Address,
                          contactDetails: ContactDetails
                          )

case class CorrespondenceContact(
                                addressDetails: Address,
                                contactDetails: ContactDetails,
                                differentAddress: Option[Boolean]
                                )

case class PrimaryPersonContact(
                                 name: String,
                                 positionInCompany: Option[String],
                                 telephone: String,
                                 mobile: Option[String],
                                 fax: Option[String],
                                 email: String  // TODO ask LT/NK if nice way to avoid dupe of ContactDetails
                               )

case class LitresProduced(
                           litresProducedUKHigher: Option[Int],
                           litresProducedUKLower: Option[Int],
                           litresImportedUKHigher: Option[Int],
                           litresImportedUKLower: Option[Int],
                           litresPackagedUKHigher: Option[Int],
                           litresPackagedUKLower: Option[Int]
                         )

object ProducerClassification extends Enumeration {
  type ProducerClassification = Value
  val Unknown, Large, Small = Value
  implicit val producerClassificationFormat = EnumUtils.enumFormat(ProducerClassification)
}

object OrganisationType extends Enumeration {
  type OrganisationType = Value
  val SoleProprietor, LimitedCompany, LLP, UnincorporatedBody, Partnership = Value
  implicit val organisationTypeFormat = EnumUtils.enumFormat(OrganisationType)
}

object ActionType extends Enumeration {
  val Unknown, Add, Amend, Remove = Value
  implicit val actionTypeFormat = EnumUtils.enumFormat(ActionType)
}

object EntityType extends Enumeration {
  val Unknown, GroupMember, GroupRepresentativeMember, ControllingBody, Partner = Value
  implicit val entityTypeFormat = EnumUtils.enumFormat(EntityType)
}

object ActivityType extends Enumeration {
  val Unknown, Producer, Importer, ContractPacker = Value
  implicit val activityTypeFormat = EnumUtils.enumFormat(ActivityType)
}

case class ProducerDetails(
                            produceMillionLitres: Boolean,
                            producerClassification: String,
                            smallProducerExemption: Option[Boolean],
                            useContractPacker: Option[Boolean],
                            voluntarilyRegistered: Option[Boolean]
                          )
case class Details(
                  producer: Boolean,
                  producerDetails: Option[ProducerDetails],
                  importer: Boolean,
                  contractPacker: Boolean
                  )

object SiteAction extends Enumeration {
  val Unknown, NewSite, AmendSite, CloseSite, TransferSite = Value
  implicit val siteActionFormat = EnumUtils.enumFormat(SiteAction)
}

object SiteType extends Enumeration {
  val Unknown, Warehouse, ProductionSite = Value
  implicit val siteTypeFormat = EnumUtils.enumFormat(SiteType)
}

case class Site(
                 action: String,
                 tradingName: String,
                 newSiteRef: String,
                 siteAddress: BusinessContact,
                 siteType: String
               )

case class Registration(
                          organisationType: String,
                          applicationDate: Date,
                          taxStartDate: Date,
                          cin: String,
                          tradingName: String,
                          businessContact: BusinessContact,
                          correspondenceContact: CorrespondenceContact,
                          primaryPersonContact: PrimaryPersonContact,
                          details: Details,
                          activityQuestions: LitresProduced,
                          estimatedTaxAmount: Option[BigDecimal],
                          taxObligationStartDate: Date
                        )


case class EntityAction(
                  action: String,
                  entityType: String,
                  organisationType: String,
                  cin: String,
                  tradingName: String,
                  businessContact: BusinessContact
                )

case class CreateSubscriptionRequest(
                                      registration: Registration,
                                      sites: List[Site],
                                      entityAction: List[EntityAction]
                                    )

case class CreateSubscriptionResponse(
                                       processingDate: LocalDateTime,
                                       formBundleNumber: String
                                     )
