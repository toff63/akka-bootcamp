package com.example.scalafx

import scalafx.application.JFXApp
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Side
import scalafx.scene.Scene
import scalafx.scene.chart.{ NumberAxis, XYChart, LineChart }
import com.example.MonitoringMain
import akka.actor.Props
import com.example.actors.ChartActor
import java.util.concurrent.atomic.AtomicLong
import scalafx.scene.layout.HBox
import scalafx.scene.control._
import scalafx.geometry.{ Insets, Pos }
import scalafx.scene.layout.Priority
import scalafx.Includes._
import scalafx.scene.layout.StackPane

class Main extends JFXApp {
  private var counter: AtomicLong = new AtomicLong

  val chart = ObservableBuffer[javafx.scene.chart.XYChart.Series[Number, Number]]()
  
  stage = new JFXApp.PrimaryStage {
    title.value = "Line Chart Sample"
    scene = new Scene {
      root = new StackPane {
        children = Seq(lineChart(chart), addSeriesButton)
        margin = Insets(137)
      }
    }
  }
  
  def addSeriesButton() = new Button("Add series") {
    onAction = handle {
      chartActor ! ChartActor.Messages.AddSeries(ChartDataHelper.serie("FakeSeries-" + counter.incrementAndGet()))
    }
    alignmentInParent = Pos.BOTTOM_RIGHT
//    hgrow = Priority.Always
//    maxWidth = Double.MaxValue
    padding = Insets(7)
  }
  
  def lineChart(chart: ObservableBuffer[javafx.scene.chart.XYChart.Series[Number, Number]]) =
    new LineChart(NumberAxis("Number of Month"), NumberAxis("")) {
      title = "Stock Monitoring, 2010"
      legendSide = Side.RIGHT
      data = chart
    }

  val chartActor = MonitoringMain.system.actorOf(ChartActor.props(chart), "charting")
  chartActor ! ChartActor.Messages.InitializeChart(ChartDataHelper("FakeSeries-" + counter.incrementAndGet()))
}

object ChartDataHelper {
  def apply(title: String, points: Int = 100): Map[String, Seq[(Int, Int)]] = Map(title -> randomPoints(points))
  
  def serie(title: String, points: Int = 100): (String, Seq[(Int, Int)]) = (title -> randomPoints(points))

  private def randomPoints(numberOfPoint: Int = 100): Seq[(Int, Int)] = {
    val random = scala.util.Random
    for (i <- 0 to numberOfPoint) yield (i, random.nextInt())
  }
}