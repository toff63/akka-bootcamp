using System;
using System.IO;
using Akka.Actor;

namespace WinTail
{
    /// <summary>
    /// Actor responsible for reading FROM the console. 
    /// Also responsible for calling <see cref="ActorSystem.Shutdown"/>.
    /// </summary>
    class ConsoleReaderActor : UntypedActor
    {
        public const string ExitCommand = "exit";
        public const string StartCommand = "start";
        private IActorRef validationActor;

        public ConsoleReaderActor(IActorRef consoleValidationActor)
        {
            validationActor = consoleValidationActor;
        }

        protected override void OnReceive(object message)
        {
            if (message.Equals(StartCommand))
            {
                DoPrintInstructions();
            }
            GetAndValidateInput();
        }

        private void DoPrintInstructions()
        {
            Console.WriteLine("Please provide the URI of a log file on disk.\n");
            Console.WriteLine(string.Format("Like: {0}\n", Path.GetFullPath(".")));
        }

        /// <summary>
        /// Reads input from console, validates it, then signals appropriate response
        /// (continue processing, error, success, etc.).
        /// </summary>
        private void GetAndValidateInput()
        {
            var message = Console.ReadLine();
            if(isEnd(message))
            {
                // shut down the entire actor system (allows the process to exit)
                Context.System.Shutdown();
            }
            validationActor.Tell(message);
        }

        private bool isEnd(String line)
        {
            return !string.IsNullOrEmpty(line) && String.Equals(line, ExitCommand, StringComparison.OrdinalIgnoreCase);
        }

    }


}