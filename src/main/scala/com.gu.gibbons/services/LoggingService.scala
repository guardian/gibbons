package com.gu.gibbons
package services

import monix.eval.Task
import org.apache.logging.log4j.LogManager

trait LoggingService[F[_]] {
    def info(msg: String): F[Unit]
    def warn(msg: String): F[Unit]
    def error(msg: String): F[Unit]
}

class LambdaInterpreter extends LoggingService[Task] {
    final val logger = LogManager.getLogger(classOf[LambdaInterpreter]);

    final def info(msg: String) = Task {
        logger.info(msg)
    }

    final def warn(msg: String) = Task {
        logger.warn(msg)
    }

    final def error(msg: String) = Task {
        logger.error(msg)
    }
}