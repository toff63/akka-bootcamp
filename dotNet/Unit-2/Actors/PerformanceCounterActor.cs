using System;
using System.Collections.Generic;
using System.Diagnostics;
using Akka.Actor;

namespace ChartApp.Actors
{
    class PerformanceCounterActor : UntypedActor
    {
        private readonly string seriesName;
        private readonly Func<PerformanceCounter> performanceCounterGenerator;
        private PerformanceCounter counter;

        private HashSet<IActorRef> subscriptions;
        private ICancelable cancelPublish;

        public PerformanceCounterActor(string seriesName, Func<PerformanceCounter> performanceCounterGenerator)
        {
            this.seriesName = seriesName;
            this.performanceCounterGenerator = performanceCounterGenerator;
            subscriptions = new HashSet<IActorRef>();
            cancelPublish = new Cancelable(Context.System.Scheduler);
        }

        protected override void PreStart()
        {
            counter = performanceCounterGenerator();
            Context.System.Scheduler.ScheduleTellRepeatedly(
                TimeSpan.FromMilliseconds(250), 
                TimeSpan.FromMilliseconds(250), 
                Self, 
                new GatherMetrics(), 
                Self,
                cancelPublish );
        }

        protected override void PostStop()
        {
            try
            {
                cancelPublish.Cancel();
                counter.Dispose();
            } catch
            {
                // Don't care about dispose exceptions
            } finally
            {
                base.PostStop();
            }
        }

        protected override void OnReceive(object message)
        {
            if(message is GatherMetrics)
            {
                var metric = new Metric(seriesName, counter.NextValue());
                foreach( var sub in subscriptions)
                {
                    sub.Tell(metric);
                }
            } else if(message is SubscribeCounter)
            {
                var subscribeMessage = message as SubscribeCounter;
                subscriptions.Add(subscribeMessage.Subscriber);
            } else if(message is UnsubscribeCounter)
            {
                var unsubscribeMessage = message as UnsubscribeCounter;
                subscriptions.Remove(unsubscribeMessage.Subscriber);
            }
        }
    }
}
