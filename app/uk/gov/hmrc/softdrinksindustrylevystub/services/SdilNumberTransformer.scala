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

package uk.gov.hmrc.softdrinksindustrylevystub.services

import uk.gov.hmrc.smartstub._
import uk.gov.hmrc.domain.Modulus23Check

object ModulusCheck extends Modulus23Check {
  def apply(in: String): Char = calculateCheckCharacter(in)
}

object SdilNumberTransformer {

  val tolerantUtr: Enumerable[String] =
    pattern"9999999999".imap(_.reverse)(_.reverse)

  val sdilRefEnum: Enumerable[String] =
    pattern"999999000".imap { i =>
      val sum = ModulusCheck("SDIL" ++ i.reverse)
      s"X${sum}SDIL${i.reverse}"
    } { b =>
      b.drop(6).reverse
    }

  // Define unambiguous given instances
  given toLongFromTolerantUtr: ToLong[String] = tolerantUtr

  given toLongFromSdilRefEnum: ToLong[String] = sdilRefEnum

  // Scala 3-compatible convertEnum
  def convertEnum[A, B](enumA: Enumerable[A], enumB: Enumerable[B])(input: A)(using ToLong[A]): Option[B] =
    enumB.get(enumA.asLong(input))

  val utrToSdil: String => Option[String] = input =>
    convertEnum(tolerantUtr, sdilRefEnum)(input)(using toLongFromTolerantUtr)

  val sdilToUtr: String => Option[String] = input =>
    convertEnum(sdilRefEnum, tolerantUtr)(input)(using toLongFromSdilRefEnum)

  def showTable(num: Int): Unit = {
    for {
      i <- 1 to num
      _ <- tolerantUtr.get(i.toLong)
      _ <- sdilRefEnum.get(i.toLong)
    } yield ()
    ()
  }
}
