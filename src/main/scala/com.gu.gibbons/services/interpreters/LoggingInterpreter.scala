package com.gu.gibbons.services.interpreters

import monix.eval.Task
import org.apache.logging.log4j.{ LogManager, Logger }

import com.gu.gibbons.services.LoggingService

class LoggingInterpreter(logger: Logger) extends LoggingService[Task] {
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

object LoggingInterpreter {
  def apply(owner: String): Task[LoggingInterpreter] = Task.evalOnce {
    val logger = LogManager.getLogger(owner)
    new LoggingInterpreter(logger)
  }
}
