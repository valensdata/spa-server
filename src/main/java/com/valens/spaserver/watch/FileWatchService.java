package com.valens.spaserver.watch;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class FileWatchService {

    private Path basePath;
    private WatchService watchService;
    private List<FileWatchListener> fileWatchListeners = new ArrayList<>();

    public FileWatchService(String basePath) throws IOException {
        Path rawPath = Paths.get(basePath);
        if (rawPath.toFile().isDirectory()) {
            this.basePath = rawPath;
        } else {
            this.basePath = rawPath.getParent();
        }
        this.watchService = this.basePath.getFileSystem().newWatchService();
    }

    public void addListener(FileWatchListener fileWatchListener) {
        fileWatchListeners.add(fileWatchListener);
    }

    void watch() {
        try {
            basePath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
            WatchKey watckKey;
            while (true) {
                watckKey = watchService.take();
                List<WatchEvent<?>> watchEvents = watckKey.pollEvents();
                if (!fileWatchListeners.isEmpty()) {
                    for (FileWatchListener currentListener : fileWatchListeners) {
                        currentListener.filesChanged(watchEvents);
                    }
                }
                watckKey.reset();
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.toString());
        }
    }


}
