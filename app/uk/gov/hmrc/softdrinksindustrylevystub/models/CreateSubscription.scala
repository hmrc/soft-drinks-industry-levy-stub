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
                  ) {
  def isValid: Boolean = {
    val linePattern: String = "^[A-Za-z0-9 \\-,.&'\\/]{1,35}$"
    Seq(
      line1.matches(linePattern),
      line2.matches(linePattern),
      line3.getOrElse("v").matches(linePattern),
      line4.getOrElse("v").matches(linePattern),
      postCode.getOrElse("AA11AA").matches("^[A-Z]{1,2}[0-9][0-9A-Z]?\\s?[0-9][A-Z]{2}$|BFPO\\s?[0-9]{1,5}$"),
      country.getOrElse("AA").length <= 2
    ).reduce(_ && _)
  }
}

case class ContactDetails(
                           telephone: String,
                           mobile: Option[String],
                           fax: Option[String],
                           email: String
                         ) {
  def isValid: Boolean = {
    Validation.isValidContactDetails(this)
  }
}

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
                                 email: String
                               ) {
  def isValid: Boolean = {
    val cd = ContactDetails(telephone,mobile,fax,email)
    Seq(
      Validation.isValidContactDetails(cd),
      positionInCompany.getOrElse("a").length <= 155,
      name.length <= 40
    ) reduce (_ && _)
  }
}

case class LitresProduced(
                           litresProducedUKHigher: Option[Long],
                           litresProducedUKLower: Option[Long],
                           litresImportedUKHigher: Option[Long],
                           litresImportedUKLower: Option[Long],
                           litresPackagedUKHigher: Option[Long],
                           litresPackagedUKLower: Option[Long]
                         ) {
  def isValid: Boolean = {
    val max = 9999999999999L
    Seq(
      litresProducedUKHigher.getOrElse(0L) <= max,
      litresProducedUKLower.getOrElse(0L) <= max,
      litresImportedUKHigher.getOrElse(0L) <= max,
      litresImportedUKLower.getOrElse(0L) <= max,
      litresPackagedUKHigher.getOrElse(0L) <= max,
      litresPackagedUKLower.getOrElse(0L) <= max
    ) reduce (_ && _)
  }
}

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
                  ) {
  def isValid: Boolean = {
    producerDetails match {
      case Some(a) => a.producerClassification.matches("^[0-1]{1}$")
      case _ => true
    }
  }
}

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
                ) {
  def isValid: Boolean = {
    Seq(
      action.matches("^[1]{1}$"),
      entityType.matches("^4$"),
      Validation.isValidOrganisationType(organisationType),
      Validation.isValidTradingName(tradingName),
      businessContact.addressDetails.isValid,
      businessContact.contactDetails.isValid,
      Validation.isValidUtr(cin)
    ) reduce(_ && _)
  }
}

case class CreateSubscriptionRequest(
                                      registration: Registration,
                                      sites: List[Site],
                                      entityAction: List[EntityAction]
                                    ) {
  def isValid: Boolean = {
    Seq(
      Validation.isValidUtr(registration.cin),
      registration.businessContact.addressDetails.isValid,
      registration.businessContact.contactDetails.isValid,
      registration.correspondenceContact.addressDetails.isValid,
      registration.correspondenceContact.contactDetails.isValid,
      registration.primaryPersonContact.isValid,
      Validation.isValidSites(sites),
      Validation.isValidTradingName(registration.tradingName),
      Validation.isValidOrganisationType(registration.organisationType),
      registration.details.isValid,
      registration.activityQuestions.isValid,
      registration.estimatedTaxAmount.getOrElse(BigDecimal(0)) <= BigDecimal(99999999999.99)
    ) reduce (_ && _)
  }
}

case class CreateSubscriptionResponse(
                                       processingDate: LocalDateTime,
                                       formBundleNumber: String
                                     )

object Validation {

  def isValidIdNumber(idNumber: String): Option[FailureMessage] = {
    idNumber match {
      case a if !isValidUtr(a) =>
        Some(FailureMessage("INVALID_UTR", "Submission has not passed validation, invalid UTR."))
      case _ => None
    }
  }

  def isValidUtr(utr: String): Boolean = {
    utr.matches("^\\d{5}[3-9]\\d{4}$")
  }

  def isValidIdType(idType: String): Option[FailureMessage] = {
    idType match {
      case a if a != "utr" =>
        Some(FailureMessage("INVALID_IDTYPE", s"Submission has not passed validation, invalid idType."))
      case _ => None
    }
  }


  def isValidSites(sites: List[Site]): Boolean = {
    sites.map(s =>
      s.siteAddress.addressDetails.isValid &&
        s.siteAddress.contactDetails.isValid
    ) reduce (_ && _)
  }

  def isValidContactDetails(cd: ContactDetails): Boolean = {
    val phonePattern: String = "^[0-9 ()+--]{1,24}$"
    Seq(
      cd.telephone.matches(phonePattern),
      cd.mobile.getOrElse("1").matches(phonePattern),
      cd.fax.getOrElse("1").matches(phonePattern),
      cd.email.length <= 132
    ) reduce(_ && _)
  }

  def isValidTradingName(tradingName: String): Boolean = {
    tradingName.length <= 160
  }

  def isValidOrganisationType(organisationType: String): Boolean = {
    organisationType.matches("^[1-5]{1}$")
  }

  def checkParams(idType: String, idNumber: String): List[FailureMessage] = {
    List(
      isValidIdType(idType),
      isValidIdNumber(idNumber)
    ) filter (_.isDefined) map (x => x.get)
  }

}

case class FailureMessage(
                    code: String,
                    reason: String
                  )

case class FailureResponse(
                          failures: List[FailureMessage]
                          )