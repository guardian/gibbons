package com.gu.gibbons
package lambdas

import monix.eval.Task
import monix.execution.Scheduler.Implicits.global

class UnverifiedUserLambda extends GenericLambda(new UnverifiedUser(_, _, _, _))
