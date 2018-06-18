package com.gu.gibbons
package lambdas

import monix.execution.Scheduler.Implicits.global

class UserReminderLambda extends GenericLambda(new UserReminder(_, _, _, _))
