package com.gu.gibbons.utils

import com.gu.gibbons.config.Settings
import com.gu.gibbons.model.User
import java.time.Instant
import java.security.{ MessageDigest, Security }
import org.bouncycastle.jce.provider.BouncyCastleProvider


abstract class HashGenerator(md: MessageDigest) {
  def params(user: User, salt: String): String = {
    s"h=${hash(user.id.id, user.remindedAt.get, salt)}"
  }

  def hash(id: String, when: Instant, salt: String): String = {
    val hash = id + when.toEpochMilli.toString + salt
    md.digest(hash.getBytes).map("%02X".format(_)).mkString
  }
}

class UrlGenerator(settings: Settings, md: MessageDigest) extends HashGenerator(md) {
  private def url(action: String, user: User) =
    s"${settings.bonoboUrl}/user/${user.id.id}/${action}?${params(user, settings.salt)}"

  def extend(user: User): String = url("extend", user)

  def delete(user: User): String = url("delete", user)
}

object UrlGenerator {
  def apply(settings: Settings) = {
    Security.addProvider(new BouncyCastleProvider())
    val md = MessageDigest.getInstance("SHA3-512")
    new UrlGenerator(settings, md)
  }
}