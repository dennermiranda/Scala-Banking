package controllers

import javax.inject.{Inject, Singleton}

import models._
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import play.api.libs.json._
import play.api.mvc._
import play.api.libs.functional.syntax._

import scala.annotation.tailrec

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
      (JsPath \ "value").read[BigDecimal] and
      (JsPath \ "date").read[DateTime](jodaDateReads) and
      (JsPath \ "description").read[String]
    )(Deposit.apply _)

  implicit val purchaseReads: Reads[Purchase] = (
    (JsPath \ "accountNumber").read[Int] and
      (JsPath \ "value").read[BigDecimal] and
      (JsPath \ "date").read[DateTime](jodaDateReads) and
      (JsPath \ "description").read[String]
    )(Purchase.apply _)

  implicit val withdrawalReads: Reads[Withdrawal] = (
    (JsPath \ "accountNumber").read[Int] and
      (JsPath \ "value").read[BigDecimal] and
      (JsPath \ "date").read[DateTime](jodaDateReads) and
      (JsPath \ "description").read[String]
    )(Withdrawal.apply _)

  implicit val depositWrites: Writes[Deposit] = (
    (JsPath \ "accountNumber").write[Int] and
      (JsPath \ "value").write[BigDecimal] and
      (JsPath \ "date").write[DateTime](jodaDateWrites) and
      (JsPath \ "description").write[String]
    )(unlift(Deposit.unapply))

  implicit val purchaseWrites: Writes[Purchase] = (
    (JsPath \ "accountNumber").write[Int] and
      (JsPath \ "value").write[BigDecimal] and
      (JsPath \ "date").write[DateTime](jodaDateWrites) and
      (JsPath \ "description").write[String]
    )(unlift(Purchase.unapply))

  implicit val withdrawalWrites: Writes[Withdrawal] = (
    (JsPath \ "accountNumber").write[Int] and
      (JsPath \ "value").write[BigDecimal] and
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
    operationsOption match {
      case Some(dates) =>
        val balanceAmount = dates.map(operations => getBalanceAmountR(0.0, operations._2)).sum
        Ok(Json.obj("status" ->"OK", "message" -> ("Balance; '"+balanceAmount), "balance" -> balanceAmount ))
      case None =>
        BadRequest(Json.obj("status" ->"KO", "message" -> "The account doesn't exist or doesn't have operations"))
    }
  }

  def statement(accountNumber: Int, from: String, to: String) = Action {
    val fromDate = DateTime.parse(from, DateTimeFormat.forPattern(dateFormat))
    val toDate = DateTime.parse(to, DateTimeFormat.forPattern(dateFormat))
    var responseArray = List[JsObject]()
    var balanceAmount: BigDecimal = 0
    dao.getOperations(accountNumber) match {
      case Some(dates) =>
        dates.foreach(operations => {
          balanceAmount = getBalanceAmountR(balanceAmount, operations._2)
          if(checkRange(fromDate, toDate, operations)){
            var operationsJson = List[JsObject]()
            operations._2.foreach(operation => {
              val operationJson: JsObject = getOperationJson(operation)
              operationsJson = operationJson :: operationsJson
            })
            val jsonDate = Json.obj("date" -> operations._1.toString(dateFormat), "operations" -> Json.toJsFieldJsValueWrapper(operationsJson), "balance" -> balanceAmount)
            responseArray = jsonDate :: responseArray
          }
        })
        Ok(Json.obj("dates" -> Json.toJsFieldJsValueWrapper(responseArray.reverse)))
      case None =>
        BadRequest(Json.obj("status" ->"KO", "message" -> "The account doesn't exist or doesn't have operations"))
    }
  }

  def debtPeriods(accountNumber: Int) = Action {
    var currentDebt: BigDecimal = 0
    var currentStart:DateTime = null
    var responseArray = List[JsObject]()
    var balanceAmount: BigDecimal = 0
    dao.getOperations(accountNumber) match {
      case Some(dates) =>
        dates.foreach(operations => {
          balanceAmount = getBalanceAmountR(balanceAmount, operations._2)
          if(balanceAmount<0.0){
            if(currentStart!= null){
              if(balanceAmount!=currentDebt){
                val debt = getDebtJson(currentDebt, currentStart, operations)
                responseArray = debt :: responseArray
                currentDebt = balanceAmount
                currentStart = operations._1
              }
            }else{
              currentDebt = balanceAmount
              currentStart = operations._1
            }
          }else{
            if(currentStart!=null){
              val debt = getDebtJson(currentDebt, currentStart, operations)
              responseArray = debt :: responseArray
              currentDebt = 0
              currentStart = null
            }
          }
        })
        if(currentStart!=null){
          val debt = Json.obj("start" -> currentStart.toString(dateFormat),"principal"-> currentDebt)
          responseArray = debt :: responseArray
        }
        Ok(Json.obj("dates" -> Json.toJsFieldJsValueWrapper(responseArray.reverse)))

      case None =>
        BadRequest(Json.obj("status" ->"KO", "message" -> "The account doesn't exist or doesn't have operations"))
    }
  }

  private def getDebtJson(currentDebt: BigDecimal, currentStart: DateTime, operations: (DateTime, List[Operation])): JsObject = {
    Json.obj("start" -> currentStart.toString(dateFormat), "end" -> operations._1.minusDays(1).toString(dateFormat), "principal" -> currentDebt)
  }

  def getOperationJson(operation: Operation): JsObject = {
    var operationType = ""
    operation match {
      case Purchase(_, _, _, _) => operationType = "purchase"
      case Deposit(_, _, _, _) => operationType = "deposit"
      case Withdrawal(_, _, _, _) => operationType = "Withdrawal"
    }
    Json.obj("type" -> operationType, "description" -> operation.description, "value" -> operation.value)
  }

  //Calculates the balance of that day
  @tailrec
  private def getBalanceAmountR(valAmount: BigDecimal, operations: List[Operation]): BigDecimal = operations match{
    case Purchase(_, value, _, _):: tail =>getBalanceAmountR(valAmount - value, tail)
    case Deposit(_, value, _, _):: tail =>getBalanceAmountR(valAmount + value, tail)
    case Withdrawal(_, value, _, _)::tail =>getBalanceAmountR(valAmount - value, tail)
    case _ => valAmount
  }

  private def checkRange(fromDate: DateTime, toDate: DateTime, operations: (DateTime, List[Operation])): Boolean = {
    (operations._1.isAfter(fromDate) || operations._1.isEqual(fromDate)) && (operations._1.isBefore(toDate) || operations._1.isEqual(toDate))
  }
}
