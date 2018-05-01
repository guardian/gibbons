package com.gu.gibbons
package services

trait LoggingService[F[_]] {
    def info(msg: String): F[Unit]
    def warn(msg: String): F[Unit]
    def error(msg: String): F[Unit]
}

