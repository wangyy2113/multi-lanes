package com.wangyy.multilanes.core.infra.finagle

import com.twitter.finagle.context.Contexts
import com.twitter.io.Buf
import com.twitter.util.{Return, Try}

case class MLFinagleFeatureTagContext(featureTag: String)

object MLFinagleFeatureTagContext extends Contexts.broadcast.Key[MLFinagleFeatureTagContext]("com.wangyy.multilanes.core.infra.finagle.MLFinagleFeatureTagContext") {

  override def marshal(context: MLFinagleFeatureTagContext): Buf = {
    Buf.ByteArray.Owned(context.featureTag.getBytes("UTF-8"))
  }

  override def tryUnmarshal(buf: Buf): Try[MLFinagleFeatureTagContext] = {
    val bytes = Buf.ByteArray.Owned.extract(buf)
    val ft = new String(bytes, "UTF-8")
    Return(MLFinagleFeatureTagContext(ft))
  }
}


