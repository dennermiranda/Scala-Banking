package models

import org.joda.time.DateTime

/**
  * Created by Dener on 18/04/2017.
  */
abstract class Operation(){
  def accountNumber: Int
  def value: Double
  def date: DateTime
  def description: String
}
