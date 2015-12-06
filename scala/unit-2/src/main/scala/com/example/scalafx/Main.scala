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

class Main extends JFXApp {
  private var counter: AtomicLong = new AtomicLong

  val chart = ObservableBuffer[javafx.scene.chart.XYChart.Series[Number, Number]]()
  stage = new JFXApp.PrimaryStage {
    title.value = "Line Chart Sample"
    height = 600
    width = 800 
    scene = new Scene {
      root = new LineChart(NumberAxis("Number of Month"), NumberAxis("")) {
        title = "Stock Monitoring, 2010"
        legendSide = Side.RIGHT
        data = chart
      }
    }
  }

  val chartActor = MonitoringMain.system.actorOf(ChartActor.props(chart), "charting")
  chartActor ! ChartActor.Messages.InitializeChart(ChartDataHelper("FakeSeries-" + counter.incrementAndGet()))
}

object ChartDataHelper {
  def apply(title: String, points: Int = 100): Map[String, Seq[(Int, Int)]] = {
    Map(title -> randomPoints(points))
  }

  private def randomPoints(numberOfPoint: Int = 100): Seq[(Int, Int)] = {
    val random = scala.util.Random
    for (i <- 0 to numberOfPoint) yield (i, random.nextInt())
  }
}