using System;
using System.IO;
using Akka.Actor;

namespace WinTail
{
    class FileObserver : IDisposable
    {
        private readonly IActorRef tailActor;
        private FileSystemWatcher watcher;
        private readonly string fileDir;
        private readonly string fileNameOnly;

        public FileObserver(IActorRef tailActor, string absoluteFilePath)
        {
            this.tailActor = tailActor;
            this.fileDir = Path.GetDirectoryName(absoluteFilePath);
            this.fileNameOnly = Path.GetFileName(absoluteFilePath);
        }

        public void Start()
        {
            this.watcher = new FileSystemWatcher(fileDir, fileNameOnly);
            watcher.NotifyFilter = NotifyFilters.FileName | NotifyFilters.LastAccess;
            watcher.Changed += OnFileChanged;
            watcher.Error += OnFileError;
            watcher.EnableRaisingEvents = true;
        }

        public void Dispose()
        {
            watcher.Dispose();
        }

        void OnFileError(object sender, ErrorEventArgs errorArgs)
        {
            tailActor.Tell(new TailActor.FileError(fileNameOnly, errorArgs.GetException().Message), ActorRefs.NoSender);
        }

        void OnFileChanged(object sender, FileSystemEventArgs  eventArgs)
        {
            if(eventArgs.ChangeType == WatcherChangeTypes.Changed)
            {
                tailActor.Tell(new TailActor.FileWrite(eventArgs.Name), ActorRefs.NoSender);
            }
        }
    }
}
