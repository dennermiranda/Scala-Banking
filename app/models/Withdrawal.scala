package models

import org.joda.time.DateTime
import play.api.libs.json._
import play.api.libs.functional.syntax._

/**
  * Created by Dener on 18/04/2017.
  */
case class Withdrawal(accountNumber: Int, value: Double, date: DateTime, description: String) extends Operation(){
  implicit val withdrawalReads: Reads[Withdrawal] = (
    (JsPath \ "accountNumber").read[Int] and
      (JsPath \ "value").read[Double] and
      (JsPath \ "date").read[DateTime] and
      (JsPath \ "description").read[String]
    )(Withdrawal.apply _)

  implicit val withdrawalWrites: Writes[Withdrawal] = (
    (JsPath \ "accountNumber").write[Int] and
      (JsPath \ "value").write[Double] and
      (JsPath \ "date").write[DateTime] and
      (JsPath \ "description").write[String]
    )(unlift(Withdrawal.unapply))
}
