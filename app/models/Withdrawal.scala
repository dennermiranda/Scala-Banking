package models

import org.joda.time.DateTime

/**
  * Created by Dener on 18/04/2017.
  */
case class Withdrawal(value: Double, date: DateTime) extends Operation
