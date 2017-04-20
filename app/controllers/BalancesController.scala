package controllers

import javax.inject.{Inject, Singleton}
import play.api._
import play.api.mvc._

/**
  * Created by Dener on 18/04/2017.
  */

@Singleton
class BalancesController @Inject() (dao: BalancesDAO) extends Controller{

  def deposit = Action{ request =>
    Ok
  }

  def purchase = Action{ request =>
    Ok
  }

  def withdrawal = Action{ request =>
    Ok
  }
}
