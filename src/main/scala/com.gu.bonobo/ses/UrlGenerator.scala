package com.gu.bonobo.ses

import java.security.MessageDigest

import com.gu.bonobo.config.Settings
import com.gu.bonobo.model.Key

class UrlGenerator(settings: Settings) {
  def keep(key: Key): String = settings.email.lambdaYesUrl + s"?${params(key)}"

  def delete(key: Key): String = settings.email.lambdaNoUrl + s"?${params(key)}"

  def params(key: Key): String = {
    val hash = key.rangeKey.id + settings.email.nonce
    s"k=key.rangeKey&h=${md5.digest(hash.getBytes)}"
  }

  private val md5 = MessageDigest.getInstance("MD5")
}