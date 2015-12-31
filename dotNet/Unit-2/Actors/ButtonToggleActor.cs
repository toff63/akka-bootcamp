using System;
using System.Windows.Forms;
using Akka.Actor;

namespace ChartApp.Actors
{
    class ButtonToggleActor : UntypedActor
    {
        public class Toggle { }

        private readonly CounterType myCounterType;
        private bool isToggledOn;
        private readonly Button myButton;
        private readonly IActorRef coordinatorActor;

        public ButtonToggleActor(IActorRef coordinator, Button myButton, CounterType myCounterType, bool isToggledOn = false)
        {
            this.coordinatorActor = coordinator;
            this.myButton = myButton;
            this.myCounterType = myCounterType;
            this.isToggledOn = isToggledOn;
        }

        protected override void OnReceive(object message)
        {
            if (message is Toggle && isToggledOn)
            {
                coordinatorActor.Tell(new PerformanceCounterCoordinatorActor.Unwatch(myCounterType));
                FlipToggle();
            }
            else if (message is Toggle && !isToggledOn)
            {
                coordinatorActor.Tell(new PerformanceCounterCoordinatorActor.Watch(myCounterType));
                FlipToggle();
            }
            else
            {
                Unhandled(message);
            }
        }

        private void FlipToggle()
        {
            this.isToggledOn = !this.isToggledOn;
            myButton.Text = string.Format("{0} ({1})", myCounterType.ToString().ToUpperInvariant(), isToggledOn ? "ON" : "FALSE");
        }
    }
}
