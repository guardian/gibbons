package com.gu.gibbons.utils

import com.gu.gibbons.config.Settings
import com.gu.gibbons.model.{ UserId , Key}
import java.security.{ MessageDigest, Security }
import org.bouncycastle.jce.provider.BouncyCastleProvider

abstract class HashGenerator(md: MessageDigest) {
  def params(key: Key, salt: String): String =
    s"h=${hash(key.consumerId, key.remindedAt.get, salt)}"

  def hash(id: String, when: Long, salt: String): String = {
    val hash = id + when.toString + salt
    md.digest(hash.getBytes).map("%02X".format(_)).mkString
  }
}

class UrlGenerator(settings: Settings, md: MessageDigest) extends HashGenerator(md) {
  private def url(action: String, key: Key) =
    s"${settings.bonoboUrl}/user/${key.consumerId}/${action}?${params(key, settings.salt)}"

  def extendKey(key: Key): String = url("extend", key)

  def deleteKey(key: Key): String = url("delete", key)

}

object UrlGenerator {
  def apply(settings: Settings) = {
    Security.addProvider(new BouncyCastleProvider())
    val md = MessageDigest.getInstance("SHA3-512")
    new UrlGenerator(settings, md)
  }
}
