package com.gu.gibbons.ses

import java.security.MessageDigest

import com.gu.gibbons.config._
import com.gu.gibbons.model.Key

class HashGenerator {
  def params(key: Key, nonce: String): String = {
    s"k=key.rangeKey&h=${hash(key.rangeKey.id, nonce)}"
  }

  def hash(keyId: String, nonce: String): String = {
    val hash = keyId + nonce
    md5.digest(hash.getBytes).map("%02X".format(_)).mkString
  }

  private val md5 = MessageDigest.getInstance("MD5")
}

class UrlGenerator(settings: ScheduledSettings) extends HashGenerator {
  def keep(key: Key): String = settings.email.lambdaYesUrl + s"?${params(key, settings.nonce)}"

  def delete(key: Key): String = settings.email.lambdaNoUrl + s"?${params(key, settings.nonce)}"
}