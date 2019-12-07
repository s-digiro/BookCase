package edu.temple.bookcase;

import android.content.Context;
import android.widget.Toast;

public class Debug {
    private static Context c = null;

    public static void print(String message) {
        Toast.makeText(c, message, Toast.LENGTH_SHORT).show();
    }

    public static void setContext(Context c) {
        Debug.c = c;
    }
}
