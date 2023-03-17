package org.hablo.helper;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class PropertiesLoader {

    private static final Map<String, String> mapper = new HashMap<>();

    public PropertiesLoader(String file) {
        try {
            load(file);
        } catch (IOException ignored) {
        }
    }

    public String convert(String s) {
        String rc = mapper.get(s);
        if (rc == null) {
            rc = "";
        }

        return rc;
    }

    @Override
    public String toString() {
        return "PropertiesLoader{" + mapper.toString() + "}";
    }

    private static void load(String base) throws IOException {
        try (InputStream in = loadResourceAsStream(base)) {
            if (in != null) {
                addBundle(new PropertyResourceBundle(in));
            }
        }
    }

    private static InputStream loadResourceAsStream(String name) {
        InputStream in = null;
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        if (contextClassLoader != null) {
            in = contextClassLoader.getResourceAsStream(name);
        }

        if (in == null) {
            in = PropertiesLoader.class.getClassLoader().getResourceAsStream(name);
        }

        return in;
    }

    private static void addBundle(ResourceBundle r) {
        Enumeration en = r.getKeys();
        while (en.hasMoreElements()) {
            String key = (String) en.nextElement();
            String value = r.getString(key);
            if (!key.startsWith("#")) {
                mapper.put(key, value);
            }
        }
    }
}
