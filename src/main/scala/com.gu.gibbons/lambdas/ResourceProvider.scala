package com.gu.gibbons.lambdas

import com.amazonaws.services.dynamodbv2.{AmazonDynamoDBAsyncClientBuilder, AmazonDynamoDBAsync}
import com.amazonaws.services.simpleemail.{AmazonSimpleEmailServiceAsyncClientBuilder, AmazonSimpleEmailServiceAsync}
import com.gu.gibbons.config.Settings
import com.gu.gibbons.services.interpreters.LoggingInterpreter
import java.util.concurrent.TimeUnit
import monix.eval.Task
import okhttp3.{OkHttpClient, ConnectionPool}


trait ResourceProvider {
  def resources(settings: Settings, logger: LoggingInterpreter): Task[Resources] = Task.evalOnce {
    val emailClient = AmazonSimpleEmailServiceAsyncClientBuilder.standard()
      .withRegion(settings.region)
      .build()

    val dynamoClient = AmazonDynamoDBAsyncClientBuilder.standard()
      .withRegion(settings.region)
      .build()

    val httpClient = new OkHttpClient.Builder()
      .connectTimeout(1, TimeUnit.SECONDS)
      .readTimeout(1, TimeUnit.SECONDS)
      .connectionPool(new ConnectionPool(5, 10, TimeUnit.SECONDS))
      .build

    Resources(emailClient, dynamoClient, httpClient)
  }.attempt.flatMap {
    case Left(error) => 
      logger.error(s"Failed to initialize resources: $error").flatMap(_ => Task.raiseError(error))
    case Right(success) => Task.now(success)
  }

  def cleanup(res: Resources): Task[Unit] = Task.eval {
    res.email.shutdown()
    res.dynamo.shutdown()
    res.http.dispatcher.executorService.shutdown()
  }

  case class Resources(email: AmazonSimpleEmailServiceAsync, dynamo: AmazonDynamoDBAsync, http: OkHttpClient)
}