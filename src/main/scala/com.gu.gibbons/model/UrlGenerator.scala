package com.gu.gibbons
package model

import java.time.Instant
import java.security.{ MessageDigest, Security }
import org.bouncycastle.jce.provider.BouncyCastleProvider

import config._

class HashGenerator {
  import HashGenerator._

  def params(user: User, salt: String): String = {
    s"h=${hash(user.id.id, user.remindedAt.get, salt)}"
  }

  def hash(id: String, when: Long, salt: String): String = {
    val hash = id + when.toString + salt
    md.digest(hash.getBytes).map("%02X".format(_)).mkString
  }
}

object HashGenerator {
  Security.addProvider(new BouncyCastleProvider())
  private val md = MessageDigest.getInstance("SHA3-512")
}

class UrlGenerator(settings: Settings) extends HashGenerator {
  private def url(action: String, user: User) =
    s"${settings.bonoboUrl}/user/${user.id.id}/${action}?${params(user, settings.salt)}"

  def extend(user: User): String = url("extend", user)

  def delete(user: User): String = url("delete", user)
}