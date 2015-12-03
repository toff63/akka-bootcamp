using System.IO;
using Akka.Actor;

namespace WinTail
{
    class FileValidatorActor : UntypedActor
    {
        private readonly IActorRef _consoleWriterActor;
        private readonly IActorRef _tailCoordinatiorActor;

        public FileValidatorActor(IActorRef consoleWriterActor, IActorRef tailCoordinationActor)
        {
            _consoleWriterActor = consoleWriterActor;
            _tailCoordinatiorActor = tailCoordinationActor;
        }

        protected override void OnReceive(object message)
        {
            var msg = message as string;
            if (string.IsNullOrEmpty(msg))
            {
                _consoleWriterActor.Tell(new Message.NullInputError("Input was blank. Please try again\n"));
                Sender.Tell(new Message.ContinueProcessing());
            }
            else
            {
                if(isFileURI(msg))
                {
                    _consoleWriterActor.Tell(new Message.InputSuccess(string.Format("Starting processing {0}", msg)));
                    _tailCoordinatiorActor.Tell(new TailCoordinatiorActor.StartTail(msg, _consoleWriterActor));
                }
                else
                {
                    _consoleWriterActor.Tell(new Message.ValidationError(string.Format("{0} is not an existing URI on disk", msg)));
                    Sender.Tell(new Message.ContinueProcessing());
                }
            }
        }

        private bool isFileURI(string path)
        {
            return File.Exists(path);
        }
    }
}
