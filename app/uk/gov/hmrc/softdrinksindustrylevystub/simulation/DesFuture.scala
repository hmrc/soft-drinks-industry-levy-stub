/*
 * Copyright 2018 HM Revenue & Customs
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

package des

import scala.concurrent._
import scala.concurrent.duration._
import scala.io.Source

case class DesFuture[A](inner: Future[A]) extends AnyVal {
  import DesFuture._
  def slow(
    minDuration: Duration,
    maxDuration: Duration
  )(implicit ec: ExecutionContext): Future[A] = {
    inner.map { x =>
      Thread.sleep(minDuration.toUnit(MILLISECONDS).toLong)
      x
    }
  }

  def slow(implicit ec: ExecutionContext): Future[A] =
    slow(15 seconds, 45 seconds)

  def unreliable(
    failureChance: Float
  )(implicit ec: ExecutionContext): Future[A] = {
    inner.flatMap { x =>
      if (random.nextFloat < failureChance) {
        val excuse = DesFuture.excuses(random.nextInt(DesFuture.excuses.size))
        Future.failed(new RuntimeException(excuse))
      } else {
        Future.successful(x)
      }
    }
  }

  def unreliable(implicit ec: ExecutionContext): Future[A] =
    unreliable(0.66f)

  def desify(
    id: String
  )(implicit ec: ExecutionContext): Future[A] = id match {
    case sevens if sevens.endsWith("777") => inner.slow
    case eights if eights.endsWith("888") => inner.unreliable
    case nines  if nines.endsWith("999")  => inner.slow.unreliable
    case _                              => inner
  }

}

object DesFuture {
  val random = new java.util.Random

  // val's just don't get much lazier than this
  lazy val excuses =
    Source.fromURL("http://pages.cs.wisc.edu/~ballard/bofh/excuses")
      .getLines.toList

}
