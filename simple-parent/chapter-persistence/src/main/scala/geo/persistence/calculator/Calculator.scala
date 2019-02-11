package geo.persistence.calculator

import akka.actor.{Props}
import akka.persistence.PersistentActor
import akka.actor.ActorLogging
import akka.persistence.RecoveryCompleted

object Calculator {
  def props = Props(new Calculator)
  def name = "my-calculator"
  
  /**
   * sealed trait:
   * allows compiler to check for missing cases in a case statement
   */
  sealed trait Command
  
  case object Clear extends Command
  case class Add(value: Double) extends Command
  case class Subtract(value: Double) extends Command
  case class Divide(value: Double) extends Command 
  case class Multiply(value: Double) extends Command 
  case object PrintResult extends Command 
  case object GetResult extends Command 
  
  sealed trait Event 
  case object Reset extends Event 
  case class Added(value: Double) extends Event 
  case class Subtracted(value: Double) extends Event 
  case class Divided(value: Double) extends Event 
  case class Multiplied(value: Double) extends Event 
  
  case class CalculationResult(result: Double = 0) {
    def reset = copy(result = 0)
    def add(value: Double) = copy(result = this.result + value)
    def substract(value: Double) = copy(result = this.result - value)
    def divide(value: Double) = copy(result = this.result / value )
    def multiply(value: Double) = copy(result = this.result * value)
  }
}

  class Calculator extends PersistentActor with ActorLogging {
    import Calculator._
    
    def persistenceId = Calculator.name
    var state = CalculationResult()
    // more code to follow ... 
    
    /**
     * the receiveRecover function is called during recovery, or 
     * startup of the actor to re-read all the commands. 
     */
    val receiveRecover: Receive = {
      case event:Event => 
        log.info(">>>recovering {}", event)
        updateState(event)
      case RecoveryCompleted => 
        log.info("Calculator recovery completed")
    }
    
    /**
     * The receiveCommand is called for every client action.
     */
    val receiveCommand: Receive = {
      case Add(value) => 
        log.info("<<<receiveCommand Add {}",value)
        persist(Added(value))(updateState)
      case Subtract(value) => 
        log.info("<<<receiveCommand Subtract {}",value)
        persist(Subtracted(value))(updateState)
      case Divide(value) => 
        log.info("<<<receiveCommand Divide {}",value)
        if ( value != 0) persist(Divided(value))(updateState)
      case Multiply(value) => 
        log.info("<<<receiveCommand Multiply {}",value)
        persist(Multiplied(value))(updateState)
      case PrintResult => 
        log.info("<<<receiveCommand PrintResult")
        println(s"the result is ${state.result}")
      case GetResult => 
         log.info("<<<receiveCommand GetResult")
        sender() ! state.result
      case Clear => 
        log.info("<<<receiveComand Clear")
        persist(Reset)(updateState)
    }
    
    val updateState: Event => Unit = {
      case Reset => state = state.reset
      case Added(value) => state = state.add(value)
      case Subtracted(value) => state = state.substract(value)
      case Divided(value) => state = state.divide(value)
      case Multiplied(value) => state = state.multiply(value)
    }
  }
