using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Akka.Actor;

namespace WinTail
{
    class ValidationActor : UntypedActor
    {
        private IActorRef _consoleWriterActor;

        public ValidationActor(IActorRef consoleWriterActor)
        {
            this._consoleWriterActor = consoleWriterActor;
        }

        protected override void OnReceive(object msg)
        {
            var message = msg as String;
            if (string.IsNullOrEmpty(message))
            {
                // signal that the user needs to supply an input, as previously
                // received input was blank
                Self.Tell(new Message.NullInputError("No input received."));
            }
            else 
            {
                var valid = IsValid(message);
                if (valid)
                {
                    _consoleWriterActor.Tell(new Message.InputSuccess("Thank you! Message was valid."));
                }
                else
                {
                    _consoleWriterActor.Tell(new Message.ValidationError("Invalid: input had odd number of characters."));
                }
            }
            Sender.Tell(new Message.ContinueProcessing());
        }

        /// <summary>
        /// Validates <see cref="message"/>.
        /// Currently says messages are valid if contain even number of characters.
        /// </summary>
        /// <param name="message"></param>
        /// <returns></returns>
        private static bool IsValid(string message)
        {
            var valid = message.Length % 2 == 0;
            return valid;
        }
    }
}
