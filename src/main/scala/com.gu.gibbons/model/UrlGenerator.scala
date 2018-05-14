package com.gu.gibbons
package model

import java.time.Instant
import java.security.MessageDigest

import config._

class HashGenerator {
  def params(user: User, salt: String): String = {
    s"h=${hash(user.id.id, user.remindedAt.get, salt)}"
  }

  def hash(id: String, when: Instant, salt: String): String = {
    val hash = id + when.toEpochMilli.toString + salt
    md5.digest(hash.getBytes).map("%02X".format(_)).mkString
  }

  private val md5 = MessageDigest.getInstance("MD5")
}

class UrlGenerator(settings: Settings) extends HashGenerator {
  private def url(action: String, user: User) =
    s"${settings.bonoboUrl}/user/${user.id.id}/${action}?${params(user, settings.salt)}"

  def extend(user: User): String = url("extend", user)

  def delete(user: User): String = url("delete", user)
}