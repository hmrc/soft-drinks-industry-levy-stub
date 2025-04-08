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

import org.scalacheck.Gen
import uk.gov.hmrc.smartstub.{Enumerable, ToLong}

object HeadersGenerator {

//  implicit val sdilToLong: Enumerable[String] = SdilNumberTransformer.sdilRefEnum
//  given ToLong[String] = SdilNumberTransformer.sdilRefEnum
  import uk.gov.hmrc.softdrinksindustrylevystub.services.SdilNumberTransformer.toLongFromSdilRefEnum

  def genCorrelationIdHeader: Gen[String] =
    Gen
      .listOfN[Char](
        36,
        Gen.frequency(
          (3, Gen.alphaUpperChar),
          (3, Gen.alphaLowerChar),
          (3, Gen.numChar),
          (1, Gen.const[Char]("-".charAt(0)))
        )
      )
      .map(_.mkString) // correlationId
}
