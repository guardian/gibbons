package com.gu.gibbons
package model

import java.security.MessageDigest

import config._

class HashGenerator {
  def params(user: User, nonce: String): String = {
    s"u=user.id.id&h=${hash(user.id.id, nonce)}"
  }

  def params(key: Key, nonce: String): String = {
    s"u=key.keyValue&h=${hash(key.keyValue, nonce)}"
  }

  def hash(id: String, nonce: String): String = {
    val hash = id + nonce
    md5.digest(hash.getBytes).map("%02X".format(_)).mkString
  }

  private val md5 = MessageDigest.getInstance("MD5")
}

class UrlGenerator(settings: ScheduledSettings) extends HashGenerator {
  def url(user: User): String = settings.bonoboListUrl + s"?${params(user, settings.nonce)}"

  def url(key: Key): String = settings.bonoboDeleteUrl + s"?${params(key, settings.nonce)}"
}