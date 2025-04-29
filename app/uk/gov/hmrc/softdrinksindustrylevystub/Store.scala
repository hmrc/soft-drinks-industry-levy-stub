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

package uk.gov.hmrc.softdrinksindustrylevystub

import java.time.LocalDate
import org.scalacheck.Gen
import uk.gov.hmrc.smartstub.*
import uk.gov.hmrc.softdrinksindustrylevystub.models.internal.*
import uk.gov.hmrc.softdrinksindustrylevystub.services.*
import sdil.models.des.FinancialTransactionResponse
import cats.implicits.*
import scala.collection.concurrent
import scala.collection.mutable

import scala.collection.concurrent.TrieMap

object Store {

  implicit val enumString: Enumerable[String] = SdilNumberTransformer.sdilRefEnum

  /** Compute-once cache: on first lookup, runs f(k) and stores it. */
  private def seededCache[K, V](f: K => V): mutable.Map[K, V] =
    new mutable.AbstractMap[K, V] with mutable.Map[K, V] {
      private val underlying = TrieMap.empty[K, V]
      override def apply(key: K): V =
        underlying.getOrElseUpdate(key, f(key))
      override def get(key: K): Option[V] = underlying.get(key)
      override def iterator: Iterator[(K, V)] = underlying.iterator
      override def addOne(kv: (K, V)): this.type = { underlying.put(kv._1, kv._2); this }
      override def subtractOne(key: K): this.type = { underlying.remove(key); this }
    }

  // ————— UTR → List[SDIL] —————
  val utrToSdil: mutable.Map[String, List[String]] =
    seededCache(utr => SdilNumberTransformer.utrToSdil(utr).toList)

  // ————— SDIL → Subscription —————
  val _store: mutable.Map[String, Option[Subscription]] =
    seededCache { sdil =>
      def generate(pred: Subscription => Boolean) =
        SubscriptionGenerator.genSubscription.retryUntil(pred).seeded(sdil)

      val maybeSub: Option[Subscription] = (sdil.init.last, sdil.last) match {
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
        case (_, '6') =>
          generate(_ => true)
            .map(
              _.copy(
                deregDate = Some(LocalDate.now.minusMonths(3)),
                liabilityDate = LocalDate.now.minusMonths(3)
              )
            )
        case _ => SubscriptionGenerator.genSubscription.seeded(sdil)
      }

      maybeSub.map { sub =>
        sub.copy(
          utr = SdilNumberTransformer.sdilToUtr(sdil).getOrElse(""),
          sdilRef = sdil
        )
      }
    }

  def fromSdilRef(in: String): Option[Subscription] = _store(in)

  def fromUtr(in: String): Option[Subscription] =
    utrToSdil(in).headOption
      .flatMap(sdil => _store(sdil))

  def add(in: Subscription): Unit = {
    val existing = utrToSdil.getOrElse(in.utr, Nil)
    utrToSdil.addOne(in.utr  -> (in.sdilRef :: existing).distinct)
    _store.addOne(in.sdilRef -> Some(in))
  }

  def sdilToUtr(sdil: String): Option[String] =
    utrToSdil
      .collectFirst { case (utr, all) if all.contains(sdil) => utr }
      .orElse(SdilNumberTransformer.sdilToUtr(sdil))

  def unusedSdilRefs: Iterable[String] = {
    val overridden = utrToSdil.values.flatten.toSet
    (0 to 99999).iterator
      .map(x => SdilNumberTransformer.sdilRefEnum(x * 10L))
      .filterNot(overridden.contains)
      .toList
  }

  def clear(): Unit = {
    utrToSdil.clear()
    _store.clear()
  }

  def financialHistory(sdilRef: String): FinancialTransactionResponse =
    CannedFinancialData.canned
      .map { case (_, v) => v.toTry }
      .sequence
      .map(xs => Gen.oneOf(xs).seeded(sdilRef).get)
      .get
}
