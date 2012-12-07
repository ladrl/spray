/*
 * Copyright (C) 2011-2012 spray.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package spray.http

import HttpCharsets._

case class ContentTypeRange(mediaRange: MediaRange, charsetRange: HttpCharsetRange = `*`) {
  def value: String = charsetRange match {
    case `*` => mediaRange.value
    case x: HttpCharset => mediaRange.value + "; charset=" + x.value
  }
  def matches(contentType: ContentType) = {
    mediaRange.matches(contentType.mediaType) &&
            ((charsetRange eq `*`) || contentType.definedCharset.map(charsetRange.matches(_)).getOrElse(false))
  }
  override def toString = "ContentTypeRange(" + value + ')'
}

object ContentTypeRange {
  implicit def fromMediaRange(mediaRange: MediaRange): ContentTypeRange = apply(mediaRange)
}

case class ContentType(mediaType: MediaType, definedCharset: Option[HttpCharset]) {
  def value: String = definedCharset match {
    case Some(cs) => mediaType.value + "; charset=" + cs.value
    case _ => mediaType.value
  }

  def withMediaType(mediaType: MediaType) =
    if (mediaType != this.mediaType) copy(mediaType = mediaType) else this
  def withCharset(charset: HttpCharset) =
    if (noCharsetDefined || charset != definedCharset.get) copy(definedCharset = Some(charset)) else this
  def withoutDefinedCharset =
    if (isCharsetDefined) copy(definedCharset = None) else this

  def isCharsetDefined = definedCharset.isDefined
  def noCharsetDefined = definedCharset.isEmpty

  def charset: HttpCharset = definedCharset.getOrElse(`ISO-8859-1`)
}

object ContentType {
  val `text/plain` = ContentType(MediaTypes.`text/plain`)
  val `application/octet-stream` = ContentType(MediaTypes.`application/octet-stream`)

  // RFC4627 defines JSON to always be UTF encoded, we always render JSON to UTF-8
  val `application/json` = ContentType(MediaTypes.`application/json`, `UTF-8`)

  def apply(mediaType: MediaType, charset: HttpCharset): ContentType = apply(mediaType, Some(charset))
  implicit def apply(mediaType: MediaType): ContentType = apply(mediaType, None)
}
