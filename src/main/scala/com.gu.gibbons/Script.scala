package com.gu.gibbons

import java.time.OffsetDateTime
import model._

trait Script[F[_]] {
  def run(now: OffsetDateTime, dryRun: Boolean): F[Map[UserId, Option[EmailResult]]]
}