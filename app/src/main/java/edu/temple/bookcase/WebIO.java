package edu.temple.bookcase;

import android.graphics.drawable.Drawable;
import android.util.JsonReader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class WebIO {
    // Received from stackoverflow
    // https://stackoverflow.com/questions/6407324/how-to-display-image-from-url-on-android
    public static Drawable loadImageFromWebOperations(String url) {
        try {
            InputStream is = (InputStream) new URL(url).getContent();
            return Drawable.createFromStream(is, "src name");
        } catch (Exception e) {
            return null;
        }
    }

    public static List<Book> downloadBooks(String param, Boolean loadOld, File filesDir) {
        List<Book> retval = new ArrayList<>();
        try {
            String path = "https://kamorris.com/lab/audlib/booksearch.php";
            if (loadOld) {
                File file = new File(filesDir, "last.txt");
                if (file.exists()) {
                    try {
                        path = path + "?search=" + new String(Files.readAllBytes(Paths.get(file.getPath())));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else if (param != null) {
                path += "?search=" + param;
            }

            URL api = new URL(path);
            HttpURLConnection conn = (HttpURLConnection)api.openConnection();

            if (conn.getResponseCode() == 200) {
                InputStream responseBody = conn.getInputStream();
                InputStreamReader reader = new InputStreamReader(responseBody, StandardCharsets.UTF_8);

                JsonReader jsonReader = new JsonReader(reader);
                jsonReader.beginArray();

                while (jsonReader.hasNext()) {
                    Book.Builder builder = Book.Builder.newInstance();
                    jsonReader.beginObject();
                    while (jsonReader.hasNext()) {
                        String key = jsonReader.nextName();
                        switch (key) {
                            case "book_id":
                                builder.setId(Integer.parseInt(jsonReader.nextString()));
                                break;
                            case "title":
                                builder.setTitle(jsonReader.nextString());
                                break;
                            case "author":
                                builder.setAuthor(jsonReader.nextString());
                                break;
                            case "published":
                                builder.setPublished(Integer.parseInt(jsonReader.nextString()));
                                break;
                            case "cover_url":
                                builder.setCoverURL(jsonReader.nextString());
                                break;
                            case "duration":
                                builder.setDuration(Integer.parseInt(jsonReader.nextString()));
                                break;
                            default:
                                jsonReader.nextString();
                                break;
                        }
                    }
                    jsonReader.endObject();
                    retval.add(builder.build());
                }
                jsonReader.endArray();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return retval;
    }
}
