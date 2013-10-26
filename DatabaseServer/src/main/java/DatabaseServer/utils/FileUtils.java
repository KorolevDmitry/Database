package DatabaseServer.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 10/22/13
 * Time: 10:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class FileUtils {
    private static long _timeout = 1000;

    public static void CreateFile(File file) throws IOException {
        int counter = 10;
        IOException exception = null;
        while (counter > 0) {
            try {
                exception = null;
                --counter;
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                fileOutputStream.flush();
                fileOutputStream.close();
            } catch (FileNotFoundException e) {
                exception = e;
            } catch (IOException e) {
                exception = e;
            } finally {
                if (exception != null) {
                    try {
                        Thread.sleep(_timeout);
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
        if(exception != null)
            throw exception;
    }

    public static void DeleteFile(File file) throws IOException {
        int counter = 10;
        boolean result = false;

        while (counter > 0 && !result) {
            --counter;
            try {
                result = file.delete();
            } finally {
                if (!result) {
                    try {
                        Thread.sleep(_timeout);
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
        if(!result)
            throw new IOException();
    }
}
