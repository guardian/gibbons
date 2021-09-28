package com.gu.gibbons

package object model {
  // see https://failex.blogspot.com/2017/04/the-high-cost-of-anyval-subclasses.html
  sealed abstract class UserIdImpl {
    type T
    def apply(s: String): T
    def unwrap(id: T): String
  }

  val UserId: UserIdImpl = new UserIdImpl {
    type T = String
    override def apply(s: String) = s
    override def unwrap(id: T) = id
  }

  type UserId = UserId.T

}
