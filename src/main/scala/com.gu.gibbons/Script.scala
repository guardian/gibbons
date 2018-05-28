package com.gu.gibbons

import model.Result

trait Script[F[_]] {
  def run(dryRun: Boolean): F[Result]
}