package com.gu.gibbons.ses

import java.security.MessageDigest

import com.gu.gibbons.config._
import com.gu.gibbons.model.User

class HashGenerator {
  def params(user: User, nonce: String): String = {
    s"u=user.id.id&h=${hash(user.id.id, nonce)}"
  }

  def hash(keyId: String, nonce: String): String = {
    val hash = keyId + nonce
    md5.digest(hash.getBytes).map("%02X".format(_)).mkString
  }

  private val md5 = MessageDigest.getInstance("MD5")
}

class UrlGenerator(settings: ScheduledSettings) extends HashGenerator {
  def url(user: User): String = settings.email.bonoboUrl + s"?${params(user, settings.nonce)}"
}