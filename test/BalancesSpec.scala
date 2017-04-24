import controllers.BalancesDAO
import models._
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.scalatestplus.play._

/**
  * Created by Dener on 24/04/2017.
  */
class BalancesSpec extends PlaySpec{
  val dateFormat = "yyyy-MM-dd"
  val deposit = Deposit(8312, 20, DateTime.parse("2017-04-20", DateTimeFormat.forPattern(dateFormat)), "Deposit test")
  val withdrawal = Withdrawal(8312, 15.2, DateTime.parse("2017-04-5", DateTimeFormat.forPattern(dateFormat)), "Withdrawal test")
  val purchase = Purchase(8312, 10.2, DateTime.parse("2017-04-12", DateTimeFormat.forPattern(dateFormat)), "Purchase test")
  "A Balance DAO" must{
    "Add Operations to list" in {
      var balanceDao = new BalancesDAO
      val operationAdded = balanceDao.addOperation(deposit)
      operationAdded.last._2.length mustBe 1
      operationAdded.last._2.head mustBe deposit
    }
  }
}
