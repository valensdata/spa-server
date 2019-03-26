package com.valens.spaserver.watch;

public class FileWatcher extends Thread {

    private FileWatchService fileWatchService;

    public FileWatcher(FileWatchService fileWatchService) {
        this.fileWatchService = fileWatchService;
    }

    @Override
    public void run() {
        fileWatchService.watch();
    }

}
