package com.gu.gibbons

import cats.data.State
import config.Settings
import java.time.{Instant, OffsetDateTime, ZoneOffset}
import java.time.temporal.ChronoUnit
import model._

package object services {
  type UserRepo = Map[UserId, User]
  type EmailRepo = Set[Email]
  type Repo = (UserRepo, EmailRepo)
  type TestProgram[A] = State[Repo, A]
}

package object fixtures {
    val today = OffsetDateTime.of(2018, 4, 25, 10, 15, 30, 0, ZoneOffset.UTC)
    val todayInstant = today.toInstant

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

    val keys: Map[KeyId, Key] = Map(Seq(
        Key.create("key01", "user0", "2012-04-25T10:15:30.00Z"),
        Key.create("key02", "user0", "2013-04-25T10:15:30.00Z"),
        Key.create("key03", "user0", "2017-04-25T10:15:30.00Z"),
        Key.create("key04", "user3", "2012-04-25T10:15:30.00Z"),
        Key.create("key05", "user4", "2017-08-25T10:15:30.00Z"),
        Key.create("key06", "user6", "2015-08-25T10:15:30.00Z", Some("2015-08-25T10:15:30.00Z")),
        Key.create("key07", "user6", "2003-02-25T10:15:30.00Z", Some("2017-08-25T10:15:30.00Z"), Some("2018-04-01T10:15:30.00Z")),
        Key.create("key08", "user7", "2012-04-25T10:15:30.00Z"),
        Key.create("key09", "user7", "2012-04-25T10:15:30.00Z", Some("2015-08-25T10:15:30.00Z")),
        Key.create("key10", "user7", "2012-04-25T10:15:30.00Z", Some("2015-08-25T10:15:30.00Z"), Some("2015-08-25T10:15:30.00Z")),
        Key.create("key11", "user8", "2012-04-25T10:15:30.00Z"),
        Key.create("key12", "user8", "2012-04-25T10:15:30.00Z", Some("2015-08-25T10:15:30.00Z")),
        Key.create("key13", "user8", "2012-04-25T10:15:30.00Z", Some("2017-08-25T10:15:30.00Z")),
        Key.create("key14", "user8", "2012-04-25T10:15:30.00Z", Some("2015-08-25T10:15:30.00Z"), Some("2018-04-25T10:15:30.00Z")),
        Key.create("key15", "user9", "2012-04-25T10:15:30.00Z"),
        Key.create("key16", "user9", "2012-04-25T10:15:30.00Z", Some("2015-08-25T10:15:30.00Z"), Some("2018-04-01T10:15:30.00Z")),
        Key.create("key17", "user9", "2012-04-25T10:15:30.00Z", Some("2015-08-25T10:15:30.00Z"), Some("2018-04-01T10:15:30.00Z")),
        Key.create("key18", "user9", "2012-04-25T10:15:30.00Z", Some("2017-08-25T10:15:30.00Z"), Some("2018-04-25T10:15:30.00Z")),
        Key.create("key19", "user9", "2012-04-25T10:15:30.00Z", Some("2017-08-25T10:15:30.00Z"), Some("2018-04-25T10:15:30.00Z"))
    ).map(key => key.rangeKey -> key): _*)
}