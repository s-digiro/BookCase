package edu.temple.bookcase.fragments;

import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import java.io.InputStream;
import java.net.URL;

import edu.temple.bookcase.Book;
import edu.temple.bookcase.R;

public class BookDetailsFragment extends Fragment {
    private TextView bookTitle;
    private TextView bookAuthor;
    private TextView bookCoverURL;
    private ImageView imgHolder;

    public static BookDetailsFragment newInstance() {
        return new BookDetailsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View retval = inflater.inflate(R.layout.fragment_book_details, container, false);
        this.bookTitle = retval.findViewById(R.id.bookTitle);
        this.bookAuthor = retval.findViewById(R.id.bookAuthor);
        this.bookCoverURL = retval.findViewById(R.id.bookCoverURL);
        this.imgHolder = retval.findViewById(R.id.imageView);

        return retval;
    }

    public void displayBook(Book book) {
        this.bookTitle.setText(book.title());
        this.bookAuthor.setText(book.author());
        this.bookCoverURL.setText(book.cover());

        AsyncTask task = new AsyncTask() {
            Drawable d;

            @Override
            protected Object doInBackground(Object[] objects) {
                this.d = BookDetailsFragment.LoadImageFromWebOperations((String) objects[0]);
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                BookDetailsFragment.this.imgHolder.setImageDrawable(this.d);
            }
        };

        task.execute(book.cover());
    }

    // Received from stackoverflow
    // https://stackoverflow.com/questions/6407324/how-to-display-image-from-url-on-android
    public static Drawable LoadImageFromWebOperations(String url) {
        try {
            InputStream is = (InputStream) new URL(url).getContent();
            return Drawable.createFromStream(is, "src name");
        } catch (Exception e) {
            return null;
        }
    }
}
