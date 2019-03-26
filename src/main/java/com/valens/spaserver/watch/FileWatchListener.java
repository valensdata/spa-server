package com.valens.spaserver.watch;

import java.nio.file.WatchEvent;
import java.util.List;

@FunctionalInterface
public interface FileWatchListener {

    void filesChanged(List<WatchEvent<?>> watchEvents);

}
