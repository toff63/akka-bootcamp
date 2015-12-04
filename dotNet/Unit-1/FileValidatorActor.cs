﻿using System.IO;
using Akka.Actor;

namespace WinTail
{
    class FileValidatorActor : UntypedActor
    {
        private readonly IActorRef _consoleWriterActor;

        public FileValidatorActor(IActorRef consoleWriterActor)
        {
            _consoleWriterActor = consoleWriterActor;
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
                    Context.ActorSelection("akka://myActorSystem/user/tailCoordinatorActor").Tell(new TailCoordinatiorActor.StartTail(msg, _consoleWriterActor));
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
