import akka.stream.Materializer
import controllers.{BalancesController, BalancesDAO}
import models._
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.scalatestplus.play._
import org.scalatestplus.play.guice.{GuiceOneAppPerSuite, GuiceOneServerPerSuite}
import play.api.libs.json.Json
import play.api.mvc.EssentialAction
import play.api.test.FakeRequest
import play.api.test.Helpers._

/**
  * Created by Dener on 24/04/2017.
  */
class BalancesSpec extends PlaySpec with GuiceOneAppPerSuite{
  val dateFormat = "yyyy-MM-dd"
  val deposit = Deposit(8312, 20, DateTime.parse("2017-04-20", DateTimeFormat.forPattern(dateFormat)), "Deposit test")
  val withdrawal = Withdrawal(8312, 15.2, DateTime.parse("2017-04-5", DateTimeFormat.forPattern(dateFormat)), "Withdrawal test")
  val purchase = Purchase(8312, 10.2, DateTime.parse("2017-04-12", DateTimeFormat.forPattern(dateFormat)), "Purchase test")
  val purchaseS = Purchase(8313, 10.2, DateTime.parse("2017-04-12", DateTimeFormat.forPattern(dateFormat)), "Purchase test")
  val withdrawalS = Withdrawal(8313, 15.2, DateTime.parse("2017-04-5", DateTimeFormat.forPattern(dateFormat)), "Withdrawal test")
  val depositS = Deposit(8313, 20, DateTime.parse("2017-04-20", DateTimeFormat.forPattern(dateFormat)), "Deposit test")
  "A Balance DAO" must{
    "Add Operations to list" in {
      var balanceDao = new BalancesDAO
      val operationAdded = balanceDao.addOperation(deposit)
      operationAdded.last._2.length mustBe 1
      operationAdded.last._2.head mustBe deposit
    }

    "Get all days with operations" in {
      var balanceDao = new BalancesDAO
      balanceDao.addOperation(deposit)
      balanceDao.addOperation(depositS)
      balanceDao.addOperation(purchase)
      balanceDao.getOperations(8312) match {
        case Some(x) => x.size mustBe 2
        case None =>
      }
    }
  }

  "The Balance Controller" should {
    "Add deposit via JSON" in {
      implicit lazy val materializer: Materializer = app.materializer
      var balanceDao = new BalancesDAO
      val balanceController = new BalancesController(balanceDao)
      val action: EssentialAction = balanceController.deposit
      val request = FakeRequest(POST, "/deposit").withJsonBody(Json.parse("""{"accountNumber":8312,"value":20.5,"date":"2017-04-25","description":"Test deposit"}"""))
      val result = call(action, request)
      status(result) mustEqual OK
      contentAsString(result) must include ("8312")
    }

    "Add withdrawal via JSON" in {
      implicit lazy val materializer: Materializer = app.materializer
      var balanceDao = new BalancesDAO
      val balanceController = new BalancesController(balanceDao)
      val action: EssentialAction = balanceController.deposit
      val request = FakeRequest(POST, "/withdrawal").withJsonBody(Json.parse("""{"accountNumber":8312,"value":10.5,"date":"2017-04-20","description":"Test withdrawal"}"""))
      val result = call(action, request)
      status(result) mustEqual OK
      contentAsString(result) must include ("8312")
    }
    "Add purchase via JSON" in {
      implicit lazy val materializer: Materializer = app.materializer
      var balanceDao = new BalancesDAO
      val balanceController = new BalancesController(balanceDao)
      val action: EssentialAction = balanceController.deposit
      val request = FakeRequest(POST, "/purchase").withJsonBody(Json.parse("""{"accountNumber":8312,"value":10.5,"date":"2017-04-20","description":"Test purchase on Amazon"}"""))
      val result = call(action, request)
      status(result) mustEqual OK
      contentAsString(result) must include ("8312")
    }

  }
}
