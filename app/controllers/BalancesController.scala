package controllers

import javax.inject.{Inject, Singleton}

import models._
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
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
        BadRequest(Json.obj("status" ->"KO", "message" -> JsError.toJson(errors)))
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
        BadRequest(Json.obj("status" ->"KO", "message" -> JsError.toJson(errors)))
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
      case Some(dates) =>
        dates.foreach(operations => {
          amount = getBalanceAmount(amount, operations)
        })
        Ok(Json.obj("status" ->"OK", "message" -> ("Balance; '"+amount+"' saved."), "balance" -> amount ))
      case None =>
        BadRequest(Json.obj("status" ->"KO", "message" -> "The account doesn't exist or doesn't have operations"))
    }
  }

  def statement(accountNumber: Int, from: String, to: String) = Action {
    val fromDate = DateTime.parse(from, DateTimeFormat.forPattern(dateFormat))
    val toDate = DateTime.parse(to, DateTimeFormat.forPattern(dateFormat))
    var amount = 0.0
    var responseArray = List[JsObject]()
    dao.getOperations(accountNumber) match {
      case Some(dates) =>
        dates.foreach(operations => {
          amount = getBalanceAmount(amount, operations)
          if(checkRange(fromDate, toDate, operations)){
            var operationsJson = List[JsObject]()
            operations._2.foreach(operation => {
              val operationJson: JsObject = getOperationJson(operation)
              operationsJson = operationJson :: operationsJson
            })
            val jsonDate = Json.obj("date" -> operations._1.toString(dateFormat), "operations" -> Json.toJsFieldJsValueWrapper(operationsJson), "balance" -> amount)
            responseArray = jsonDate :: responseArray
          }
        })
        Ok(Json.obj("dates" -> Json.toJsFieldJsValueWrapper(responseArray.reverse)))
      case None =>
        BadRequest(Json.obj("status" ->"KO", "message" -> "The account doesn't exist or doesn't have operations"))
    }
  }

  def debtPeriods(accountNumber: Int) = Action {
    var amount = 0.0
    var currentDebt = 0.0
    var currentStart:DateTime = null
    var responseArray = List[JsObject]()
    dao.getOperations(accountNumber) match {
      case Some(dates) =>
        val previousAmount = amount
        dates.foreach(operations => {
          amount = getBalanceAmount(amount, operations)
          if(amount<0.0){
            if(currentStart!= null){
              if(amount!=currentDebt){
                val debt = Json.obj("start" -> currentStart, "end" -> operations._1.minusDays(1).toString(dateFormat),"principal"-> currentDebt)
                responseArray = debt :: responseArray
                currentDebt = amount
                currentStart = operations._1
              }
            }else{
              currentDebt = amount
              currentStart = operations._1
            }
          }else{
            if(currentStart!=null){
              val debt = Json.obj("start" -> currentStart, "end" -> operations._1.minusDays(1).toString(dateFormat),"principal"-> currentDebt)
              responseArray = debt :: responseArray
              currentDebt = 0.0
              currentStart = null
            }
          }
        })
        if(currentStart!=null){
          val debt = Json.obj("start" -> currentStart,"principal"-> currentDebt)
          responseArray = debt :: responseArray
        }
        Ok(Json.obj("dates" -> Json.toJsFieldJsValueWrapper(responseArray.reverse)))

      case None =>
        BadRequest(Json.obj("status" ->"KO", "message" -> "The account doesn't exist or doesn't have operations"))
    }
  }

  def getOperationJson(operation: Operation): JsObject = {
    var operationType = ""
    operation match {
      case Purchase(_, _, _, _) => operationType = "purchase"
      case Deposit(_, _, _, _) => operationType = "deposit"
      case Withdrawal(_, _, _, _) => operationType = "Withdrawal"
    }
    val operationJson = Json.obj("type" -> operationType, "description" -> operation.description, "value" -> operation.value)
    operationJson
  }
  private def getBalanceAmount(valAmount: Double, operations: (DateTime, List[Operation])): Double = {
    var amount = valAmount
    operations._2.foreach {
      case Purchase(_, value, _, _) => amount -= value
      case Deposit(_, value, _, _) => amount += value
      case Withdrawal(_, value, _, _) => amount -= value
    }
    amount
  }

  private def checkRange(fromDate: DateTime, toDate: DateTime, operations: (DateTime, List[Operation])): Boolean = {
    (operations._1.isAfter(fromDate) || operations._1.isEqual(fromDate)) && (operations._1.isBefore(toDate) || operations._1.isEqual(toDate))
  }
}
