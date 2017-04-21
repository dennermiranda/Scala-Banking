package controllers

import models.Operation
import org.joda.time.DateTime
import play.api.Logger

import scala.collection.immutable.TreeMap

/**
  * Created by Dener on 18/04/2017.
  */
class BalancesDAO {
  var accountsMap = Map[Int, TreeMap[DateTime, List[Operation]]]()
  implicit def ord: Ordering[DateTime] = Ordering.fromLessThan(_ isBefore _)

  def addOperation(operation: Operation) : TreeMap[DateTime, List[Operation]] = {

    accountsMap.get(operation.accountNumber) match {
      case Some(dates) => {
//        Logger.debug("There are operations: " + operations.size.toString)
//        accountsMap = accountsMap - operation.accountNumber
//        val uOperation1 = TreeMap[DateTime, Operation](operation.date -> operation)
//        val uOperation2 = operations ++ uOperation1
//        accountsMap = accountsMap + (operation.accountNumber -> uOperation2)
//        Logger.debug("There are operations: " + uOperation2.size.toString)
//        return uOperation2
        dates.get(operation.date) match {
          case Some(date) =>{
            val newDate = operation :: date
            val newDates = dates + (operation.date -> newDate)
            accountsMap = accountsMap + (operation.accountNumber -> newDates)
            return newDates
          }
          case None =>{
            val operations = List[Operation](operation)
            val date = TreeMap[DateTime, List[Operation]](operation.date -> operations)
            val newDates = dates ++ date
            accountsMap = accountsMap + (operation.accountNumber -> newDates)
            return dates
          }
        }
      }
      case None => {
        Logger.debug("First operation")
        val operations = List[Operation](operation)
        val dates = TreeMap[DateTime, List[Operation]](operation.date -> operations)
        accountsMap = accountsMap + (operation.accountNumber -> dates)
        return dates
      }
    }
  }

  def getOperations(accountNumber: Int) : Option[TreeMap[DateTime, List[Operation]]] = {
    accountsMap.get(accountNumber)
  }

}
