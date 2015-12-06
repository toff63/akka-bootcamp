package com.example.actors

import akka.actor.{ Actor, ActorLogging }
import scalafx.collections.ObservableBuffer
import javafx.scene.chart.XYChart
import akka.actor.Props

object ChartActor {
  object Messages {
    case class InitializeChart(series: Map[String, Seq[(Int, Int)]])
    case class AddSeries(series: (String, Seq[(Int, Int)]))
  }
  def props(charts: ObservableBuffer[XYChart.Series[Number, Number]]): Props = Props(new ChartActor(charts))
}

class ChartActor(charts: ObservableBuffer[XYChart.Series[Number, Number]]) extends Actor with ActorLogging {

  def receive = {
    case ChartActor.Messages.InitializeChart(series) =>
      charts.delegate.clear()
      series.foreach { case (name, data) => charts.delegate.add(xyChart(name, data)) }
    case ChartActor.Messages.AddSeries((name, data)) => charts.delegate.add(xyChart(name, data))

  }

  def xyChart(name: String, data: Seq[(Int, Int)]) = scalafx.scene.chart.XYChart.Series[Number, Number](
    name,
    ObservableBuffer(data.map { case (x, y) => scalafx.scene.chart.XYChart.Data[Number, Number](x, y) }))
}
