package controllers

import javax.inject.{Inject, Singleton}

import models._
import play.api._
import play.api.libs.json.{JsError, Json}
import play.api.mvc._

/**
  * Created by Dener on 18/04/2017.
  */

@Singleton
class BalancesController @Inject() (dao: BalancesDAO) extends Controller{

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
