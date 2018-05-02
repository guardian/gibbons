package com.gu.gibbons.log4j

import monix.eval.Task
import org.apache.logging.log4j.LogManager

import com.gu.gibbons.services.LoggingService

class LoggingInterpreter extends LoggingService[Task] {
    private val logger = LogManager.getLogger(classOf[LoggingInterpreter]);

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