package com.gu.gibbons

import cats.data.State
import config.Settings
import java.time.{Instant, OffsetDateTime, ZoneOffset}
import java.time.temporal.ChronoUnit
import model._

package object services {
  type UserRepo = Map[UserId, User]
  type EmailRepo = Set[Email]
  type KeyRepo = Map[String, Key]
  type Repo = (UserRepo, EmailRepo, KeyRepo)
  type TestProgram[A] = State[Repo, A]
}

package object fixtures {
    val today = OffsetDateTime.of(2018, 4, 25, 10, 15, 30, 0, ZoneOffset.UTC)
    val todayInstant = today.toInstant

    val keys: Map[String, Key] = Map(Seq(
        Key.create("user0", "rangeKey0", "consumerId0", "Developer", "2012-04-25T10:15:30.00Z"),
        Key.create("user1", "rangeKey1", "consumerId1", "Developer", "2015-08-25T10:15:30.00Z", Some("2015-08-25T10:15:30.00Z")),
        Key.create("user2", "rangeKey2", "consumerId2", "Developer", "2012-04-25T10:15:30.00Z"),
        Key.create("user3", "rangeKey3","consumerId3", "Developer", "2003-02-25T10:15:30.00Z", Some("2017-08-25T10:15:30.00Z"), Some("2018-04-01T10:15:30.00Z")),
        Key.create("user4",  "rangeKey4", "consumerId4", "Developer", "2012-04-25T10:15:30.00Z"),
        Key.create("user5", "rangeKey5","consumerId5", "Developer", "2012-04-25T10:15:30.00Z", Some("2015-08-25T10:15:30.00Z"), Some("2015-08-25T10:15:30.00Z")),
        Key.create("user6", "rangeKey6", "consumerId6", "Developer",  "2012-04-25T10:15:30.00Z", Some("2017-08-25T10:15:30.00Z")),
        Key.create("user7", "rangeKey7","consumerId7", "Developer",  "2012-04-25T10:15:30.00Z", Some("2015-08-25T10:15:30.00Z"), Some("2018-04-25T10:15:30.00Z")),
        Key.create("user8", "rangeKey8","consumerId8", "Developer", "2012-04-25T10:15:30.00Z"),
        Key.create("user9", "rangeKey9","consumerId9", "Developer",  "2012-04-25T10:15:30.00Z", Some("2015-08-25T10:15:30.00Z"), Some("2018-04-01T10:15:30.00Z"))
    ).map(key => key.consumerId -> key): _*)

    val users: Map[UserId, User] = Map(Seq(
        User.create("user0", "Florence Bowen", "florence.bowen@domain.com","2012-04-25T10:15:30.00Z"),
        User.create("user1", "Margaret Woolley", "margaret.woolley@domain.com","2015-08-25T10:15:30.00Z", Some("2015-08-25T10:15:30.00Z"), Some("false")),
        User.create("user2", "Ruth Clay", "ruth.clay@domain.com","2012-04-25T10:15:30.00Z"),
        User.create("user3", "Anna Derrick", "anna.derrick@domain.com", "2003-02-25T10:15:30.00Z", Some("2017-08-25T10:15:30.00Z"), Some("false")),
        User.create("user4", "Frances Li", "frances.li@domain.com","2012-04-25T10:15:30.00Z"),
        User.create("user5", "Mildred Blundell", "mildred.blundell@domain.com","2012-04-25T10:15:30.00Z", Some("2015-08-25T10:15:30.00Z"), Some("false")),
        User.create("user6", "Elizabeth Blaese", "elizabeth.blaese@domain.com","2012-04-25T10:15:30.00Z", Some("2017-08-25T10:15:30.00Z"), Some("false")),
        User.create("user7", "Marie Wolfe", "marie.wolfe@domain.com","2012-04-25T10:15:30.00Z", Some("2015-08-25T10:15:30.00Z"), Some("false")),
        User.create("user8", "Dorothy Robbins", "dorothy.robbins@domain.com","2012-04-25T10:15:30.00Z"),
        User.create("user9", "Mary Allen", "mary.allen@domain.com","2012-04-25T10:15:30.00Z", Some("2015-08-25T10:15:30.00Z"), Some("false"))
    ).map(user => user.id -> user): _*)
}