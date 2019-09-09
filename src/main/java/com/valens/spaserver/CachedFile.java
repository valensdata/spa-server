package com.valens.spaserver;

import com.valens.spaserver.constants.ServerParams;
import com.valens.spaserver.watch.FileWatchListener;
import com.valens.spaserver.watch.FileWatchService;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.WatchEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CachedFile implements FileWatchListener {

    private final HashMap<String, String> serverParamsMap;
    private final String filePath;
    private byte[] fileContent;
    private long fileLastModifiedSeconds;

    CachedFile(HashMap<String, String> serverParamsMap, String filePath, List<FileWatchService> fileWatchServiceList) throws IOException {
       this.serverParamsMap = serverParamsMap;
       this.filePath = filePath;
       this.cacheFile();
       fileWatchServiceList.forEach(f -> f.addListener(this));
    }

    byte[] getFileContent() {
        return fileContent;
    }

    private void cacheFile() throws IOException {
        File cacheFile = new File(serverParamsMap.get(ServerParams.BASE_PATH) + filePath.replace('/', File.separatorChar));
        fileLastModifiedSeconds = Math.max(cacheFile.lastModified() / 1000, PlaceholderUtil.getPlaceholderFileModifiedTime());

        Optional<Map<String, String>> placeholderMapOptional = PlaceholderUtil.getPlaceholders();

        if (placeholderMapOptional.isPresent()) {
            Map<String, String> placeholderMap = placeholderMapOptional.get();

            String contentString = new String(Files.readAllBytes(cacheFile.toPath()), Charset.forName("UTF-8"));

            for (Map.Entry<String, String> entry : placeholderMap.entrySet()) {
                contentString = contentString.replace(entry.getKey(), entry.getValue());
            }

            fileContent = contentString.getBytes(Charset.forName("UTF-8"));
        } else {
            fileContent = Files.readAllBytes(cacheFile.toPath());
        }
    }

    long getFileLastModifiedSeconds() {
        return fileLastModifiedSeconds;
    }

    @Override
    public void filesChanged(List<WatchEvent<?>> watchEvents) {
        try {
            cacheFile();
        } catch (IOException e) {
            System.err.println("Unable to update cached file");
        }
    }

}
