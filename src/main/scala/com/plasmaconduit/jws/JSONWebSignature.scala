package com.plasmaconduit.jws

import com.plasmaconduit.json._
import com.plasmaconduit.jwa._
import sun.misc.{BASE64Decoder, BASE64Encoder}

import scala.util.control.NoStackTrace
import scala.util.{Try, Success, Failure}

final case class JSONWebSignature(alg: DigitalSignatureOrMAC,
                                  payload: Array[Byte],
                                  //jku: Option[String], // TODO
                                  //jwk: Option[String], // TODO
                                  //kid: Option[String], // TODO
                                  //x5u: Option[String], // TODO
                                  //x5c: Option[String], // TODO
                                  //x5t: Option[String], // TODO
                                  //x5ts256: Option[String], // TODO
                                  //crit: Option[Seq[String]], // TODO
                                  typ: Option[String] = None,
                                  cty: Option[String] = None)
{

  def sign(secretOrKey: Array[Byte]): String = {
    val header    = headerString
    val payload   = payloadString
    val signature = JSONWebSignature.signatureString(alg, secretOrKey, header, payload)
    s"$header.$payload.$signature"
  }

  def sign(secretOrKey: String): String = {
    sign(secretOrKey.getBytes("UTF-8"))
  }

  def headerString: String = {
    JSONWebSignature.encoded(JsObject(
      "alg" -> JsString(alg.jwa.key),
      "typ" -> JsString("JWT")
    ).toString().getBytes("UTF-8"))
  }

  def payloadString: String = {
    JSONWebSignature.encoded(payload)
  }

}

object JWSInvalidFormatException extends NoStackTrace
object JWSMissingAlgorithmKeyException extends NoStackTrace

object JSONWebSignature {

  def verify(secretOrKey: Array[Byte], signed: String): Try[JSONWebSignature] = {
    signed.split('.') match {
      case Array(header, payload, signature , _*) => for {
        json     <- JsonParser.parse(new String(decoded(header), "UTF-8"))
        map      <- json.as[Map[String, String]]
        key      <- map.get("alg").map(n => Success(n)).getOrElse(new Failure(JWSMissingAlgorithmKeyException))
        alg      <- DigitalSignatureOrMAC.fromString(key)
        verified <- alg.verify(secretOrKey, s"$header.$payload".getBytes("UTF-8"), decoded(signature))
      } yield JSONWebSignature(
        alg     = alg,
        typ     = map.get("typ"),
        cty     = map.get("cty"),
        payload = decoded(payload)
      )
      case n => Failure(JWSInvalidFormatException)
    }
  }

  def verify(secretOrKey: String, signed: String): Try[JSONWebSignature] = {
    verify(secretOrKey.getBytes("UTF-8"), signed)
  }

  def signatureString(alg: DigitalSignatureOrMAC,
                      secretOrKey: Array[Byte],
                      header: String,
                      payload: String): String =
  {
    JSONWebSignature.encoded(alg.sign(secretOrKey, s"$header.$payload".getBytes("UTF-8")).get)
  }

  def encoded(bytes: Array[Byte]): String = {
    val encoder = new BASE64Encoder()
    encoder.encode(bytes).foldLeft("") {(m, n) =>
      n match {
        case '+'  => m + "-"
        case '/'  => m + "_"
        case '='  => m
        case '\n' => m
        case  c   => m + c
      }
    }
  }

  def decoded(string: String): Array[Byte] = {
    val decoder = new BASE64Decoder()
    val diff    = string.length % 4
    val padded  = if (diff == 0) string else string + ("=" * (4 - diff))
    val swapped = padded.foldLeft("") {(m, n) =>
      n match {
        case '-' => m + "+"
        case '_' => m + "/"
        case  c  => m + c
      }
    }
    decoder.decodeBuffer(swapped)
  }

}