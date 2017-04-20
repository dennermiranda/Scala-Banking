package models

import org.joda.time.DateTime
import play.api.libs.json._
import play.api.libs.functional.syntax._

/**
  * Created by Dener on 18/04/2017.
  */
case class Withdrawal(accountNumber: Int, value: Double, date: DateTime, description: String) extends Operation()
