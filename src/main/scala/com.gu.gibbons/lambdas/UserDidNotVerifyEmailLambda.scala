package com.gu.gibbons
package lambdas

import monix.eval.Task
import monix.execution.Scheduler.Implicits.global

class UserDidNotVerifyEmailLambda extends GenericLambda(new UserDidNotVerifyEmail(_, _, _, _))
