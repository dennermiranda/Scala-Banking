package controllers

import models.Operation
import org.joda.time.DateTime

import scala.collection.immutable.TreeMap

/**
  * Created by Dener on 18/04/2017.
  */
class BalancesDAO {
  var accountsMap = Map[Int, TreeMap[DateTime, Operation]]()
  implicit def ord: Ordering[DateTime] = Ordering.fromLessThan(_ isBefore _)

  def addOperation(operation: Operation) : TreeMap[DateTime, Operation] = {

    accountsMap.get(operation.accountNumber) match {
      case Some(operations) => {
        accountsMap = accountsMap - operation.accountNumber
        val uOperation = operations +(operation.date -> operation)
        accountsMap = accountsMap + (operation.accountNumber -> uOperation)
        return uOperation
      }
      case None => {
        val uOperation = TreeMap[DateTime, Operation](operation.date -> operation)
        accountsMap = accountsMap + (operation.accountNumber -> uOperation)
        return uOperation
      }
    }
  }

  def getOperations(accountNumber: Int) : Option[TreeMap[DateTime, Operation]] = {
    accountsMap.get(accountNumber)
  }

}
