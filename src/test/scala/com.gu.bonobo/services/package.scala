package com.gu.gibbons

import cats.{Applicative, Monad, Parallel}
import cats.arrow.FunctionK
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

  implicit val parallelProgram = new Parallel[TestProgram, TestProgram] {
      def parallel = FunctionK.id
      def sequential = FunctionK.id
      def applicative = Applicative[TestProgram]
      def monad = Monad[TestProgram]
  }
}

package object fixtures {
    val today = OffsetDateTime.of(2018, 4, 25, 10, 15, 30, 0, ZoneOffset.UTC)
    val todayInstant = today.toInstant

    val keys: Set[Key] = Set(
        Key(UserId("user0"), "Developer"),
        Key(UserId("user1"), "Developer"),
        Key(UserId("user2"), "Developer"),
        Key(UserId("user3"), "Developer"),
        Key(UserId("user4"), "Developer"),
        Key(UserId("user5"), "Developer"),
        Key(UserId("user6"), "Developer"),
        Key(UserId("user7"), "Developer"),
        Key(UserId("user8"), "Developer"),
        Key(UserId("user9"), "Developer")
    )

    val users: Map[UserId, User] = Map(Seq(
        User.create("user0", "Florence Bowen", "florence.bowen@domain.com", "2012-04-25T10:15:30.00Z"),
        User.create("user1", "Margaret Woolley", "margaret.woolley@domain.com", "2015-08-25T10:15:30.00Z", Some("2015-08-25T10:15:30.00Z")),
        User.create("user2", "Ruth Clay", "ruth.clay@domain.com", "2012-04-25T10:15:30.00Z"),
        User.create("user3", "Anna Derrick", "anna.derrick@domain.com", "2003-02-25T10:15:30.00Z", Some("2017-08-25T10:15:30.00Z"), Some("2018-04-01T10:15:30.00Z")),
        User.create("user4", "Frances Li", "frances.li@domain.com", "2012-04-25T10:15:30.00Z"),
        User.create("user5", "Mildred Blundell", "mildred.blundell@domain.com", "2012-04-25T10:15:30.00Z", Some("2015-08-25T10:15:30.00Z"), Some("2015-08-25T10:15:30.00Z")),
        User.create("user6", "Elizabeth Blaese", "elizabeth.blaese@domain.com", "2012-04-25T10:15:30.00Z", Some("2017-08-25T10:15:30.00Z")),
        User.create("user7", "Marie Wolfe", "marie.wolfe@domain.com", "2012-04-25T10:15:30.00Z", Some("2015-08-25T10:15:30.00Z"), Some("2018-04-25T10:15:30.00Z")),
        User.create("user8", "Dorothy Robbins", "dorothy.robbins@domain.com", "2012-04-25T10:15:30.00Z"),
        User.create("user9", "Mary Allen", "mary.allen@domain.com", "2012-04-25T10:15:30.00Z", Some("2015-08-25T10:15:30.00Z"), Some("2018-04-01T10:15:30.00Z"))
    ).map(user => user.id -> user): _*)
}