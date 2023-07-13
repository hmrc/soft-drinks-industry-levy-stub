/*
 * Copyright 2023 HM Revenue & Customs
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

import java.time.LocalDate
import org.scalacheck.Gen
import uk.gov.hmrc.smartstub._
import uk.gov.hmrc.softdrinksindustrylevystub.models.internal._
import uk.gov.hmrc.softdrinksindustrylevystub.services._
import sdil.models.des.FinancialTransactionResponse
import cats.implicits._

object Store {

  implicit val enum = SdilNumberTransformer.sdilRefEnum

  def mutable[K, V](f: K => V) =
    collection.concurrent.TrieMap.empty[K, V].withDefault(f)

  import SubscriptionGenerator.genSubscription

  def clear() = {
    _store.clear
    utrToSdil.clear
  }

  val _store = mutable { sdil: String =>
    def generate(pred: Subscription => Boolean) =
      genSubscription.retryUntil(pred).seeded(sdil)

    val seeded = (sdil.init.last, sdil.last) match {
      case (_, '0')   => None
      case ('1', '1') => generate(_.activity.isSmallProducer).map(_.copy(warehouseSites = Nil))
      case (_, '1')   => generate(_.activity.isSmallProducer)
      case ('2', '2') => generate(_.activity.isLarge).map(_.copy(warehouseSites = Nil))
      case ('3', '2') => generate(_.activity.isLargeNoImports).map(_.copy(warehouseSites = Nil))
      case (_, '2')   => generate(_.activity.isLarge)
      case ('3', '3') => generate(_.activity.isLargeImportCopacker)
      case (_, '3')   => generate(_.activity.isImporter)
      case (_, '4')   => generate(_.activity.isContractPacker)
      case (_, '5')   => generate(_.activity.isVoluntaryRegistration)
      case (_, '6')   => generate(_ => true).map(_.copy(deregDate = Some(LocalDate.now().minusMonths(3))))
      case _          => genSubscription.seeded(sdil)
    }

    seeded.map {
      _.copy(
        utr = sdilToUtr(sdil).getOrElse(""),
        sdilRef = sdil
      )
    }
  }

  def fromSdilRef(in: String) = _store(in)
  def fromUtr(in: String) = _store(utrToSdil(in).head)
  def add(in: Subscription): Unit = {
    utrToSdil(in.utr) = { in.sdilRef :: utrToSdil(in.utr) }.distinct
    _store(in.sdilRef) = Some(in)
  }

  val utrToSdil = mutable { utr: String =>
    SdilNumberTransformer.utrToSdil(utr).toList
  }

  def sdilToUtr(sdil: String): Option[String] =
    utrToSdil.toSeq
      .collectFirst { case (utr, allSdils) if allSdils.contains(sdil) => utr.some }
      .getOrElse(SdilNumberTransformer.sdilToUtr(sdil))

  def unusedSdilRefs: Iterable[String] = {
    val overriddenUtrs = utrToSdil.toList.flatMap { _._2 };
    { 0 to 99999 }
      .map { x =>
        SdilNumberTransformer.sdilRefEnum { x * 10L }
      }
      .filterNot { overriddenUtrs.contains }
  }

  def financialHistory(sdilRef: String): FinancialTransactionResponse =
    // if one of them fails I'd rather the whole thing fail
    // so I know there is a parse error
    CannedFinancialData.canned
      .map { case (_, v) => v.toTry }
      .sequence
      .map(x => Gen.oneOf(x).seeded(sdilRef).get)
      .get
}
