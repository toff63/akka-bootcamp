package com.example.dispatcher


import akka.dispatch.{ DispatcherPrerequisites, ExecutorServiceFactory, ExecutorServiceConfigurator }
import com.typesafe.config.Config
import java.util.concurrent.{ ExecutorService, AbstractExecutorService, ThreadFactory, TimeUnit }
import java.util.Collections
import javafx.application.Platform

// Adapted from https://gist.githubusercontent.com/viktorklang/2422443/raw/412262c68967d5971c9e135e2728cf54859e6362/swingactors.scala
// First we wrap invokeLater as an ExecutorService
object FXExecutorService extends AbstractExecutorService { 
  def execute(command: Runnable) = Platform.runLater(command)
  def shutdown(): Unit = ()
  def shutdownNow() = Collections.emptyList[Runnable]
  def isShutdown = false
  def isTerminated = false
  def awaitTermination(l: Long, timeUnit: TimeUnit) = true
}

// Then we create an ExecutorServiceConfigurator so that Akka can use our FXExecutorService for the dispatchers
class FXEventThreadExecutorServiceConfigurator(config: Config, prerequisites: DispatcherPrerequisites) extends ExecutorServiceConfigurator(config, prerequisites) {
  private val f = new ExecutorServiceFactory { def createExecutorService: ExecutorService = FXExecutorService }
  def createExecutorServiceFactory(id: String, threadFactory: ThreadFactory): ExecutorServiceFactory = f
}