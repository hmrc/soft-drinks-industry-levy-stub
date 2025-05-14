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

package uk.gov.hmrc.softdrinksindustrylevystub.models.internal

object ActivityType extends Enumeration {
  val ProducedOwnBrand, Imported, CopackerAll, CopackerSmall, Copackee = Value
}

sealed trait Activity {
  def isProducer: Boolean
  def isLarge: Boolean
  def isContractPacker: Boolean
  def isImporter: Boolean
  def isVoluntaryRegistration: Boolean = isProducer && !isLarge && !isImporter && !isContractPacker
  def isSmallProducer: Boolean = isProducer && !isLarge
  def isSmallNoImports: Boolean = isProducer && !isLarge && !isImporter
  def isSmallImportsNoCopacker: Boolean = isProducer && !isLarge && isImporter && !isContractPacker
//  def isSmallNoImportsNoCopacker: Boolean = isProducer && !isLarge && !isImporter && !isContractPacker
  def isLargeNoImports: Boolean = isProducer && isLarge && !isImporter
  def isLargeImportCopacker: Boolean = isProducer && isLarge && isImporter && isContractPacker
//  def isSmallProducerContractPacker: Boolean = isProducer && isContractPacker && !isLarge
//  def isSmallContractPacker: Boolean = !isProducer && isContractPacker && !isLarge
}

case class InternalActivity(activity: Map[ActivityType.Value, LitreBands], isLarge: Boolean) extends Activity {

  import ActivityType._

  lazy val empty: LitreBands = (0, 0)

  val lowerRate: Long = 18
  val upperRate: Long = 24

  val add: (Litres, Litres) = activity
    .filter(x => List(ProducedOwnBrand, CopackerAll, Imported).contains(x._1))
    .values
    .foldLeft((0L, 0L)) { case ((aL, aH), (pL, pH)) =>
      (aL + pL, aH + pH)
    }

  def sumOfLiableLitreRates: LitreBands =
    activity.get(CopackerSmall).fold(add) { subtract =>
      (add._1 - subtract._1, add._2 - subtract._2)
    }

  def isProducer: Boolean =
    activity.get(ProducedOwnBrand).exists(_ != empty) || activity.get(Copackee).exists(_ != empty) || isLarge

  def isContractPacker: Boolean = activity.get(CopackerAll).exists(_ != empty)

  def isImporter: Boolean = activity.get(Imported).exists(_ != empty)
}
