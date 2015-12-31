package com.example.actors.future

case class RetryContext[A](request:A, error:Throwable, numberOfPreviousFailure:Int)

trait RetryPolicy {
  def shouldRetry[A](retryContext:RetryContext[A]):Boolean = ???
}

trait NoRetry extends RetryPolicy{
    override def shouldRetry[A](retryContext:RetryContext[A]):Boolean = false
}
