package utils

import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.auth._

object AWSConstants {

  val CredentialsProvider = new AWSCredentialsProviderChain(
    new EnvironmentVariableCredentialsProvider(),
    new SystemPropertiesCredentialsProvider(),
    new ProfileCredentialsProvider("capi"),
    new ProfileCredentialsProvider(),
    new InstanceProfileCredentialsProvider())
}
