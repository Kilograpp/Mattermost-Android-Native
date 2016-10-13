package com.kilogramm.mattermost.tools;

/**
 * Created by Evgeny on 13.10.2016.
 */
public class FileUtil {
    private static FileUtil ourInstance = new FileUtil();

    public static FileUtil getInstance() {
        return ourInstance;
    }

    private FileUtil() {
    }
}
