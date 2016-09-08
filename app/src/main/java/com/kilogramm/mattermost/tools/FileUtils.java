package com.kilogramm.mattermost.tools;

/**
 * Created by Evgeny on 01.09.2016.
 */
public class FileUtils {

    public static String getFileType(String uri){
        String filenameArray[] = uri.split("\\.");
        String extension = filenameArray[filenameArray.length-1];
        return extension;
    }

}
