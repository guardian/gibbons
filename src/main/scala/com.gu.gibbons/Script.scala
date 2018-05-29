package com.gu.gibbons

import java.time.OffsetDateTime
import model.Result

trait Script[F[_]] {
  def run(now: OffsetDateTime, dryRun: Boolean): F[Result]
}