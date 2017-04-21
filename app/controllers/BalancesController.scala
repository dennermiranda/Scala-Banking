package controllers

import javax.inject.{Inject, Singleton}

import models._
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import play.api._
import play.api.libs.json._
import play.api.mvc._
import play.api.libs.functional.syntax._

/**
  * Created by Dener on 18/04/2017.
  */

@Singleton
class BalancesController @Inject() (dao: BalancesDAO) extends Controller{
  //val dateFormat = "yyyy-MM-dd'T'HH:mm:ss"
  val dateFormat = "yyyy-MM-dd"

  val jodaDateReads = Reads[DateTime](js =>
    js.validate[String].map[DateTime](dtString =>
      DateTime.parse(dtString, DateTimeFormat.forPattern(dateFormat))
    )
  )

  val jodaDateWrites: Writes[DateTime] = new Writes[DateTime] {
    def writes(d: DateTime): JsValue = JsString(d.toString())
  }

  implicit val depositReads: Reads[Deposit] = (
    (JsPath \ "accountNumber").read[Int] and
      (JsPath \ "value").read[Double] and
      (JsPath \ "date").read[DateTime](jodaDateReads) and
      (JsPath \ "description").read[String]
    )(Deposit.apply _)

  implicit val purchaseReads: Reads[Purchase] = (
    (JsPath \ "accountNumber").read[Int] and
      (JsPath \ "value").read[Double] and
      (JsPath \ "date").read[DateTime](jodaDateReads) and
      (JsPath \ "description").read[String]
    )(Purchase.apply _)

  implicit val withdrawalReads: Reads[Withdrawal] = (
    (JsPath \ "accountNumber").read[Int] and
      (JsPath \ "value").read[Double] and
      (JsPath \ "date").read[DateTime](jodaDateReads) and
      (JsPath \ "description").read[String]
    )(Withdrawal.apply _)

  implicit val depositWrites: Writes[Deposit] = (
    (JsPath \ "accountNumber").write[Int] and
      (JsPath \ "value").write[Double] and
      (JsPath \ "date").write[DateTime](jodaDateWrites) and
      (JsPath \ "description").write[String]
    )(unlift(Deposit.unapply))

  implicit val purchaseWrites: Writes[Purchase] = (
    (JsPath \ "accountNumber").write[Int] and
      (JsPath \ "value").write[Double] and
      (JsPath \ "date").write[DateTime](jodaDateWrites) and
      (JsPath \ "description").write[String]
    )(unlift(Purchase.unapply))

  implicit val withdrawalWrites: Writes[Withdrawal] = (
    (JsPath \ "accountNumber").write[Int] and
      (JsPath \ "value").write[Double] and
      (JsPath \ "date").write[DateTime](jodaDateWrites) and
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
        Ok(Json.obj("status" ->"OK", "message" -> ("Deposit on account: '"+deposit.accountNumber+"' saved.") ))
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
        Ok(Json.obj("status" ->"OK", "message" -> ("Purchase debbited from: '"+purchase.accountNumber+"' saved.") ))
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
        Ok(Json.obj("status" ->"OK", "message" -> ("Withdrawal from account '"+withdrawal.accountNumber+"' saved.") ))
      }
    )
  }

  def balance(accountNumber: Int) = Action {
    val operationsOption = dao.getOperations(accountNumber)
    var amount = 0.0
    operationsOption match {
      case Some(dates) =>{
        dates.foreach(operations => {
          operations._2.foreach(operation => operation match {
            case Purchase(accountNumber, value, date, description) => amount -= value
            case Deposit(accountNumber, value, date, description) => amount += value
            case Withdrawal(accountNumber, value, date, description) => amount -= value
          })
        })

        Logger.debug(dates.size.toString)
        Ok(Json.obj("status" ->"OK", "message" -> ("Balance; '"+amount+"' saved."), "balance" -> amount ))
      }
      case None =>{
        BadRequest((Json.obj("status" ->"KO", "message" -> "The account doesn't exist or doesn't have operations")))
      }
    }

  }

  def statement(accountNumber: Int, from: String, to: String) = Action {
    val fromDate = DateTime.parse(from, DateTimeFormat.forPattern(dateFormat))
    val toDate = DateTime.parse(to, DateTimeFormat.forPattern(dateFormat))
    var amount = 0.0

    dao.getOperations(accountNumber) match {
      case Some(dates) =>{
        dates.foreach(operations => {
          amount = getBalanceAmount(amount, operations)
          if(checkRange(fromDate, toDate, operations)){

          }
        })
      }
      case None =>
    }


    Ok(Json.obj("status" ->"OK", "message" -> ("From/to; '"+toDate.toString()+"' saved.")))
  }


  private def getBalanceAmount(valAmount: Double, operations: (DateTime, List[Operation])): Double = {
    var amount = valAmount
    operations._2.foreach(operation => operation match {
      case Purchase(accountNumber, value, date, description) => amount -= value
      case Deposit(accountNumber, value, date, description) => amount += value
      case Withdrawal(accountNumber, value, date, description) => amount -= value
    })
    amount
  }

  private def checkRange(fromDate: DateTime, toDate: DateTime, operations: (DateTime, List[Operation])): Boolean = {
    (operations._1.isAfter(fromDate) || operations._1.isEqual(fromDate)) && (operations._1.isBefore(toDate) || operations._1.isEqual(toDate))
  }
}
