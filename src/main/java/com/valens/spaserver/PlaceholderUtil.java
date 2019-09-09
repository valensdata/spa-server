package com.valens.spaserver;

import com.valens.spaserver.constants.ServerParams;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

class PlaceholderUtil {

    static Optional<Map<String, String>> getPlaceholders() throws IOException {
        HashMap<String, String> serverParamsMap = HttpStaticFileServer.serverParamsMap;

        HashMap<String, String> placeholdersMap = new HashMap<>();

        String placeholderProperties = serverParamsMap.get(ServerParams.PLACEHOLDER_PROPERTIES);
        if (placeholderProperties != null) {
            Properties properties = new Properties();
            properties.load(new FileInputStream(new File(placeholderProperties)));
            if (properties.size() > 0) {
                properties.forEach((key, value) -> placeholdersMap.put(key.toString(), value.toString()));
            }
        }

        String placeholderPrefix = serverParamsMap.get(ServerParams.PLACEHOLDER_PREFIX);
        if (placeholderPrefix != null) {
            System.getenv().entrySet().stream().filter(entry -> entry.getKey()
                    .startsWith(placeholderPrefix)).forEach(entry -> placeholdersMap.put(entry.getKey(), entry.getValue()));
        }

        if (placeholdersMap.size() > 0) {
            return Optional.of(placeholdersMap);
        } else {
            return Optional.empty();
        }
    }

}
