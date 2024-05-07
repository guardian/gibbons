# Gibbons

THIS HAS BEEN ARCHIVED AS IT HAS BEEN REPLACED BY [HOUSEKEYPER](https://github.com/guardian/capi-housekeyper)

#### Because monkeys are somehow related to keys: Bonobo, Gibbons ¯\_(ツ)_/¯

## Overview
Gibbons is comprised of two lambdas that perform API key age monitoring and cleansing.

The `UserReminderLambda`, executes on a daily schedule and scans the database of developer tier API keys looking for any that are sufficiently old to fall under GDPR data deletion rules. The lambda sends emails to users that it finds asking if they wish to retain their key(s).

The `UserDidNotAnswerLambda`, also scheduled for daily execution, returns to keys that the first lambda emailed users about but who haven't responded within two weeks, and deletes them.

Both lambda's schedules are specified in `cloudformation.yaml`

You can run Gibbons in dry mode by setting the environment variable DRY_RUN to true in the lambda configurations in AWS.


