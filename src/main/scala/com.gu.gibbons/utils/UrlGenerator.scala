package com.gu.gibbons.utils

import com.gu.gibbons.config.Settings
import com.gu.gibbons.model.{Key, User, UserId}

import java.security.{MessageDigest, Security}
import org.bouncycastle.jce.provider.BouncyCastleProvider

abstract class HashGenerator(md: MessageDigest) {
  def keyParams(key: Key, salt: String): String =
    s"h=${hash(key.consumerId, key.remindedAt.get, salt)}"

  def hash(id: String, when: Long, salt: String): String = {
    val hash = id + when.toString + salt
    md.digest(hash.getBytes).map("%02X".format(_)).mkString
  }
}

class UrlGenerator(settings: Settings, md: MessageDigest) extends HashGenerator(md) {
  private def keyUrl(action: String, key: Key) =
    s"${settings.bonoboUrl}/user/${key.consumerId}/${action}?${keyParams(key, settings.salt)}"

  private def userUrl(action: String, user: User) =
    s"${settings.bonoboUrl}/user/${UserId.unwrap(user.id)}/${action}"

  def extendKey(key: Key): String = keyUrl("extend", key)

  def deleteKey(key: Key): String = keyUrl("delete", key)

  def deleteUserAndKey(user: User): String = userUrl("unverified", user)

}

object UrlGenerator {
  def apply(settings: Settings) = {
    Security.addProvider(new BouncyCastleProvider())
    val md = MessageDigest.getInstance("SHA3-512")
    new UrlGenerator(settings, md)
  }
}
