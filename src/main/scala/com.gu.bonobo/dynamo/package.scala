package com.gu.bonobo

import com.gu.scanamo._
import java.time.Instant

package object dynamo {
  implicit val instantFormat = DynamoFormat.coercedXmap[Instant, Long, IllegalArgumentException](Instant.ofEpochMilli(_))(_.toEpochMilli)
}