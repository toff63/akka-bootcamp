using System;
using System.Collections.Generic;
using System.Windows.Forms;
using System.Windows.Forms.DataVisualization.Charting;
using Akka.Actor;
using Akka.Util.Internal;
using ChartApp.Actors;
using System.Linq;

namespace ChartApp
{
    public partial class Main : Form
    {
        private IActorRef _chartActor;
        private readonly AtomicCounter _seriesCounter = new AtomicCounter(1);
        private IActorRef _coordinatorAcotr;
        private Dictionary<CounterType, IActorRef> _toggleActors = new Dictionary<CounterType, IActorRef>();
        private Dictionary<CounterType, Button> buttons = new Dictionary<CounterType, Button>();
        public Main()
        {
            InitializeComponent();
        }

        #region Initialization


        private void Main_Load(object sender, EventArgs e)
        {
            _chartActor = Program.ChartActors.ActorOf(Props.Create(() => new ChartingActor(sysChart)), "charting");
            _chartActor.Tell(new ChartingActor.InitializeChart(null));

            _coordinatorAcotr = Program.ChartActors.ActorOf(Props.Create(() => new PerformanceCounterCoordinatorActor(_chartActor)), "counters");
            buttons.Add(CounterType.Cpu, btnCpu);
            buttons.Add(CounterType.Memory, btnMemory);
            buttons.Add(CounterType.Disk, btnDisk);

            var values = Enum.GetValues(typeof(CounterType)).Cast<CounterType>();
            _toggleActors = values.ToDictionary(counterType => counterType, counterType => Program.ChartActors.ActorOf(
                Props.Create(() => new ButtonToggleActor(_coordinatorAcotr, buttons[counterType], counterType, false))
                     .WithDispatcher("akka.actor.synchronized-dispatcher")));

            _toggleActors[CounterType.Cpu].Tell(new ButtonToggleActor.Toggle());
        }

        private void Main_FormClosing(object sender, FormClosingEventArgs e)
        {
            //shut down the charting actor
            _chartActor.Tell(PoisonPill.Instance);

            //shut down the ActorSystem
            Program.ChartActors.Shutdown();
        }


        #endregion

        private void btnCpu_Click(object sender, EventArgs e)
        {
            _toggleActors[CounterType.Cpu].Tell(new ButtonToggleActor.Toggle());
        }

        private void btnMemory_Click(object sender, EventArgs e)
        {
            _toggleActors[CounterType.Memory].Tell(new ButtonToggleActor.Toggle());
        }

        private void btnDisk_Click(object sender, EventArgs e)
        {
            _toggleActors[CounterType.Disk].Tell(new ButtonToggleActor.Toggle());
        }
    }
}
