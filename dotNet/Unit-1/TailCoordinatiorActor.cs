using System;
using Akka.Actor;

namespace WinTail
{
    class TailCoordinatiorActor : UntypedActor
    {
        public class StartTail
        {
            public StartTail(string filePath, IActorRef reportActor)
            {
                FilePath = filePath;
                ReportActor = reportActor;
            }

            public string FilePath { get; private set; }
            public IActorRef ReportActor { get; private set; }
        }

        public class StopTail
        {
            public StopTail(string filePath)
            {
                FilePath = filePath;
            }

            public string FilePath { get; set; }
        }

        protected override void OnReceive(object message)
        {
            if(message is StartTail)
            {
                var msg = message as StartTail;
                Context.ActorOf(Props.Create<TailActor>(msg.ReportActor, msg.FilePath));
            }
        }

        protected override SupervisorStrategy SupervisorStrategy()
        {
            return new OneForOneStrategy(10, TimeSpan.FromSeconds(10),
                x =>
                {
                    if (x is ArithmeticException) return Directive.Resume;
                    else if (x is NotSupportedException) return Directive.Stop;
                    else return Directive.Restart;
                });
        }
    }
}
