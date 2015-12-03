using System;
using System.IO;
using System.Text;
using Akka.Actor;

namespace WinTail
{
    class TailActor : UntypedActor
    {
        public class FileWrite
        {
            public string FileName { get; set; }
            public FileWrite(string fileName)
            {
                FileName = fileName;
            }
        }

        public class FileError
        {
            public string FileName { get; set; }
            public string Reason { get; set; }
            public FileError(string fileName, string Reason)
            {
                this.FileName = fileName;
                this.Reason = Reason;
            }
        }

        public class InitialRead
        {
            public string FileName { get; set; }
            public string Text { get; set; }
            public InitialRead(string FileName, string Text)
            {
                this.FileName = FileName;
                this.Text = Text;
            }
        }

        private readonly string filePath;
        private readonly IActorRef reporterActor;
        private readonly FileObserver observer;
        private readonly Stream fileStream;
        private readonly StreamReader fileStreamReader;

        public TailActor(IActorRef reporterActor, string filePath)
        {
            this.reporterActor = reporterActor;
            this.filePath = filePath;
            observer = new FileObserver(Self, Path.GetFullPath(filePath));
            observer.Start();
            fileStream = new FileStream(Path.GetFullPath(filePath), FileMode.Open, FileAccess.Read, FileShare.ReadWrite);
            fileStreamReader = new StreamReader(filePath, Encoding.UTF8);
            Self.Tell(new InitialRead(filePath, fileStreamReader.ReadToEnd()));
        }

        protected override void OnReceive(object message)
        {
            if(message is FileWrite)
            {
                var text = fileStreamReader.ReadToEnd();
                if(!string.IsNullOrEmpty(text))
                {
                    reporterActor.Tell(text);
                }
            }
            else if(message is FileError)
            {
                var fileError = message as FileError;
                reporterActor.Tell(string.Format("Tail error: {0}", fileError.Reason));
            }
            else if (message is InitialRead)
            {
                var text = message as InitialRead;
                reporterActor.Tell(text.Text);
            }
        }

    }
}
