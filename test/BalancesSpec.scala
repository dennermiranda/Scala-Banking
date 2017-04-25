import controllers.{BalancesController, BalancesDAO}
import models._
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.scalatestplus.play._
import org.scalatestplus.play.guice.{GuiceOneAppPerSuite, GuiceOneServerPerSuite}
import play.api.mvc.EssentialAction

/**
  * Created by Dener on 24/04/2017.
  */
class BalancesSpec extends PlaySpec with GuiceOneServerPerSuite{
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
    "Add operations via JSON" in {
      var balanceDao = new BalancesDAO
      val balanceController = new BalancesController(balanceDao)
      val action: EssentialAction = balanceController.deposit

    }

  }
}
