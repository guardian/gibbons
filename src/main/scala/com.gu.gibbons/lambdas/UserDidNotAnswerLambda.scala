package com.gu.gibbons
package lambdas

import monix.execution.Scheduler.Implicits.global

class UserDidNotAnswerLambda extends GenericLambda(new UserDidNotAnswer(_, _, _, _))
