package com.sun.jersey.samples.mandel

import scala.actors.Actor
import scala.actors.Actor._
import scala.actors.OutputChannel
import scala.collection.mutable.Stack

class JobCoordinator(n: Int) {
    trait Job {
        def execute: Unit
    }

    private case object FinishedProducing

    private case object Completed

    private def worker = actor {
        loop { react { 
            case j: Job => 
                j.execute
                sender ! Completed
            case Completed => exit
        } }
    }

    val coordinator = actor {
        var finisher: OutputChannel[Any] = null
        val workers = new Stack[OutputChannel[Any]]
        val jobs = new Stack[Job]

        for (i <- 0 to n - 1) workers push worker

        loop { react {
            case j: Job => 
                if (!workers.isEmpty) (workers pop) ! j
                else jobs push j
            case FinishedProducing =>
                finisher = sender
            case Completed =>
                if (!jobs.isEmpty) sender ! (jobs pop)
                else workers push sender

                if (workers.size == n && finisher != null) { 
                    workers foreach (_ ! Completed)
                    finisher ! Completed
                    exit 
                }
        }}
    }

    def job (j: => Unit) : Unit = {
        coordinator ! new Job { def execute = j }
    }

    def waitForCompletion: Unit = {
        coordinator !? FinishedProducing match {
            case Completed =>
        }
    }
}