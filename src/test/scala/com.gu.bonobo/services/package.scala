package com.gu.gibbons

import cats.data.State
import config.Settings
import java.time.{Instant, OffsetDateTime, ZoneOffset}
import java.time.temporal.ChronoUnit
import model._

package object services {
  type UserRepo = Map[UserId, User]
  type EmailRepo = Set[Email]
  type KeyRepo = Set[Key]
  type Repo = (UserRepo, EmailRepo, KeyRepo)
  type TestProgram[A] = State[Repo, A]
}

package object fixtures {
    val today = OffsetDateTime.of(2018, 4, 25, 10, 15, 30, 0, ZoneOffset.UTC)
    val todayInstant = today.toInstant

    val keys: Set[Key] = Set(
        Key.create("user0", "fake-consumer-id", "Developer", "2012-04-25T10:15:30.00Z"),
        Key.create("user1", "fake-consumer-id", "Developer", "2015-08-25T10:15:30.00Z", Some("2015-08-25T10:15:30.00Z")),
        Key.create("user2", "fake-consumer-id", "Developer", "2012-04-25T10:15:30.00Z"),
        Key.create("user3", "fake-consumer-id", "Developer", "2003-02-25T10:15:30.00Z", Some("2017-08-25T10:15:30.00Z"), Some("2018-04-01T10:15:30.00Z")),
        Key.create("user4", "fake-consumer-id", "Developer", "2012-04-25T10:15:30.00Z"),
        Key.create("user5", "fake-consumer-id", "Developer", "2012-04-25T10:15:30.00Z", Some("2015-08-25T10:15:30.00Z"), Some("2015-08-25T10:15:30.00Z")),
        Key.create("user6", "fake-consumer-id", "Developer",  "2012-04-25T10:15:30.00Z", Some("2017-08-25T10:15:30.00Z")),
        Key.create("user7", "fake-consumer-id", "Developer",  "2012-04-25T10:15:30.00Z", Some("2015-08-25T10:15:30.00Z"), Some("2018-04-25T10:15:30.00Z")),
        Key.create("user8", "fake-consumer-id", "Developer", "2012-04-25T10:15:30.00Z"),
        Key.create("user9", "fake-consumer-id", "Developer",  "2012-04-25T10:15:30.00Z", Some("2015-08-25T10:15:30.00Z"), Some("2018-04-01T10:15:30.00Z"))
    )

    val users: Map[UserId, User] = Map(Seq(
        User.create("user0", "Florence Bowen", "florence.bowen@domain.com"),
        User.create("user1", "Margaret Woolley", "margaret.woolley@domain.com"),
        User.create("user2", "Ruth Clay", "ruth.clay@domain.com"),
        User.create("user3", "Anna Derrick", "anna.derrick@domain.com"),
        User.create("user4", "Frances Li", "frances.li@domain.com"),
        User.create("user5", "Mildred Blundell", "mildred.blundell@domain.com"),
        User.create("user6", "Elizabeth Blaese", "elizabeth.blaese@domain.com"),
        User.create("user7", "Marie Wolfe", "marie.wolfe@domain.com"),
        User.create("user8", "Dorothy Robbins", "dorothy.robbins@domain.com"),
        User.create("user9", "Mary Allen", "mary.allen@domain.com")
    ).map(user => user.id -> user): _*)
}