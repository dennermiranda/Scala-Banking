package controllers

import javax.inject.{Inject, Singleton}

import models._
import org.joda.time.DateTime
import play.api._
import play.api.libs.json._
import play.api.mvc._
import play.api.libs.functional.syntax._

/**
  * Created by Dener on 18/04/2017.
  */

@Singleton
class BalancesController @Inject() (dao: BalancesDAO) extends Controller{
  implicit val depositReads: Reads[Deposit] = (
    (JsPath \ "accountNumber").read[Int] and
      (JsPath \ "value").read[Double] and
      (JsPath \ "date").read[DateTime] and
      (JsPath \ "description").read[String]
    )(Deposit.apply _)

  implicit val purchaseReads: Reads[Purchase] = (
    (JsPath \ "accountNumber").read[Int] and
      (JsPath \ "value").read[Double] and
      (JsPath \ "date").read[DateTime] and
      (JsPath \ "description").read[String]
    )(Purchase.apply _)

  implicit val withdrawalReads: Reads[Withdrawal] = (
    (JsPath \ "accountNumber").read[Int] and
      (JsPath \ "value").read[Double] and
      (JsPath \ "date").read[DateTime] and
      (JsPath \ "description").read[String]
    )(Withdrawal.apply _)

  implicit val depositWrites: Writes[Deposit] = (
    (JsPath \ "accountNumber").write[Int] and
      (JsPath \ "value").write[Double] and
      (JsPath \ "date").write[DateTime] and
      (JsPath \ "description").write[String]
    )(unlift(Deposit.unapply))

  implicit val purchaseWrites: Writes[Purchase] = (
    (JsPath \ "accountNumber").write[Int] and
      (JsPath \ "value").write[Double] and
      (JsPath \ "date").write[DateTime] and
      (JsPath \ "description").write[String]
    )(unlift(Purchase.unapply))

  implicit val withdrawalWrites: Writes[Withdrawal] = (
    (JsPath \ "accountNumber").write[Int] and
      (JsPath \ "value").write[Double] and
      (JsPath \ "date").write[DateTime] and
      (JsPath \ "description").write[String]
    )(unlift(Withdrawal.unapply))

  def deposit = Action(BodyParsers.parse.json){ request =>
    val depositResult = request.body.validate[Deposit]
    depositResult.fold(
      errors =>{
        BadRequest((Json.obj("status" ->"KO", "message" -> JsError.toJson(errors))))
      },
      deposit =>{
        dao.addOperation(deposit)
        Ok(Json.obj("status" ->"OK", "message" -> ("Place '"+deposit.accountNumber+"' saved.") ))
      }
    )
  }

  def purchase = Action(BodyParsers.parse.json){ request =>
    val purchaseResult = request.body.validate[Purchase]
    purchaseResult.fold(
      errors =>{
        BadRequest((Json.obj("status" ->"KO", "message" -> JsError.toJson(errors))))
      },
      purchase =>{
        dao.addOperation(purchase)
        Ok(Json.obj("status" ->"OK", "message" -> ("Place '"+purchase.accountNumber+"' saved.") ))
      }
    )
  }

  def withdrawal = Action(BodyParsers.parse.json){ request =>
    val withdrawalResult = request.body.validate[Withdrawal]
    withdrawalResult.fold(
      errors =>{
        BadRequest((Json.obj("status" ->"KO", "message" -> JsError.toJson(errors))))
      },
      withdrawal =>{
        dao.addOperation(withdrawal)
        Ok(Json.obj("status" ->"OK", "message" -> ("Place '"+withdrawal.accountNumber+"' saved.") ))
      }
    )
  }
}
