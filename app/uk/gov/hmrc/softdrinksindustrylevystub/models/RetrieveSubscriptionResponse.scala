package uk.gov.hmrc.softdrinksindustrylevystub.models

import java.time.{LocalDate => Date}

case class GetSubscriptionResponse(
                                    safeid: Option[String],
                                    nino: Option[String],
                                    utr: Option[String],
                                    changeableIndicator: String,
                                    subscriptionDetails: SubscriptionDetails,
                                    addressDetails: Option[AddressDetails],
                                    relationshipDetails: RelationshipDetails,
                                    bankDetails: Option[BankDetails],
                                    sites: List[SiteDetails]
                                  )

case class SubscriptionDetails(
                                taxObligationStartDate: Date,
                                taxObligationEndDate: Date,
                                tradingName: Option[String],
                                deregistrationDate: Option[Date],
                                voluntaryRegistration: Option[String],
                                smallProducer: Boolean,
                                largeProducer: Boolean,
                                contractPacker: Boolean,
                                importer: Boolean,
                                primaryContactName: Option[String],
                                positionInCompany: Option[String],
                                telephone: Option[String],
                                mobile: Option[String],
                                email: Option[String]
                              )

/**
  * isomorphic to createsub.Address
  */
case class AddressDetails(
                           businessAddressLine1: Option[String],
                           businessAddressLine2: Option[String],
                           businessAddressLine3: Option[String],
                           businessAddressLine4: Option[String],
                           businessCountryKey: Option[String],
                           businessPostalCode: Option[String],
                           telephone: Option[String],
                           mobile: Option[String],
                           email: Option[String],
                           fax: Option[String]
                         )

/**
  * Replace internally with List[String]?
  */
case class RelationshipDetails(
                                partnerName1: Option[String],
                                partnerName2: Option[String],
                                partnerName3: Option[String],
                                partnerName4: Option[String]
                              )

/**
  * Perhaps these should all be mandatory as bankDetails field is
  * optional and presumably these fields are required in conjunction
  */
case class BankDetails(
                        bankKey: Option[String],
                        bankAccount: Option[String],
                        reference: Option[String],
                        accountHolder: Option[String],
                        accountName: Option[String]
                      )

/**
  * No normalisation of structure present with AddressDetails, nor
  * standardisation with the Site in create subscription
  */
case class SiteDetails(
                        siteReference: Option[String],
                        tradingName: Option[String],
                        addressLine1: Option[String],
                        addressLine2: Option[String],
                        addressLine3: Option[String],
                        addressLine4: Option[String],
                        postCode: Option[String],
                        telephone: Option[String],
                        mobile: Option[String],
                        emailAddress: Option[String],
                        faxNumber: Option[String],
                        nonUk: Boolean,
                        closureDate: Option[Date],
                        validFrom: Option[Date],
                        validTo: Option[Date],
                        siteType: Option[String] // presumably the same code type as with
                        // create subscription, but not given in EPIDDS
                      )