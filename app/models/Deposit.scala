package models

import org.joda.time.DateTime

/**
  * Created by Dener on 18/04/2017.
  */
case class Deposit(accountNumber: Int, value: Double, date: DateTime, description: String) extends Operation(accountNumber, value, date, description)
