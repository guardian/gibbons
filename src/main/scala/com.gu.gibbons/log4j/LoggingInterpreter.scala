package com.gu.gibbons.log4j

import monix.eval.Task
import org.apache.logging.log4j.{Logger, LogManager}

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
    def apply(): Task[LoggingInterpreter] = Task.evalOnce {
        val logger = LogManager.getLogger(classOf[LoggingInterpreter])
        new LoggingInterpreter(logger)
    }
}