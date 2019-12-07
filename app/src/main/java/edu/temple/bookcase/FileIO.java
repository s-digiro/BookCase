package edu.temple.bookcase;

import android.annotation.SuppressLint;
import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

public class FileIO
{
    public static void saveBookMark(File filesDir, Book book, int progress)
    {
        try {
            File file = new File(filesDir, book.id() + "-bookmark.txt");
            Objects.requireNonNull(file.getParentFile()).mkdirs();
            file.createNewFile();

            OutputStream output = new FileOutputStream(file);
            output.write(Integer.toString(progress).getBytes());

            output.flush();
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int getBookMark(File filesDir, Book book) {
        int retval = 0;

        if (book != null) {
            File file = new File(filesDir, book.id() + "-bookmark.txt");
            if (file.exists()) {
                try {
                    retval = Integer.parseInt(new String(Files.readAllBytes(Paths.get(file.getPath()))));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        retval = retval - 10;
        if (retval < 0) {
            retval = 0;
        }

        return retval;
    }

    public static boolean hasLocalAudio(File filesDir, Book book) {
        return new File(filesDir, book.id() + ".mp3").exists();
    }

    public static void downloadLocalAudio(File filesDir, Book book) {
        if (book != null && !hasLocalAudio(filesDir, book)) {
            String url = "https://kamorris.com/lab/audlib/download.php?id=" + book.id();
            int count;

            try {
                File file = new File(filesDir, book.id() + ".mp3");
                URL api = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) api.openConnection();
                conn.connect();

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    InputStream input = new BufferedInputStream(api.openStream());

                    Objects.requireNonNull(file.getParentFile().mkdirs());
                    file.createNewFile();
                    OutputStream output = new FileOutputStream(file);

                    byte[] data = new byte[1024];
                    while ((count = input.read(data)) != -1) {
                        output.write(data, 0, count);
                    }
                    output.flush();
                    output.close();
                    input.close();
                    conn.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static File getLocalAudio(File filesDir, Book book) {
        File retval = new File(filesDir, book.id() + ".mp3");
        if (!retval.exists()) {
            retval = null;
        }
        return retval;
    }

    public static void deleteLocalAudio(File filesDir, Book book) {
        if (hasLocalAudio(filesDir, book)) {
            getLocalAudio(filesDir, book).delete();
        }
    }
}
