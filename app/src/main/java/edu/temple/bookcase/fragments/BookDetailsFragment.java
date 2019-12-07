package edu.temple.bookcase.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import edu.temple.bookcase.Book;
import edu.temple.bookcase.FileIO;
import edu.temple.bookcase.R;
import edu.temple.bookcase.WebIO;

public class BookDetailsFragment extends Fragment
{
    private Book book;

    private TextView bookTitle;
    private TextView bookAuthor;
    private ImageView imgHolder;
    private Button download;

    public static BookDetailsFragment newInstance() {
        return new BookDetailsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View retval = inflater.inflate(R.layout.fragment_book_details, container, false);
        this.bookTitle = retval.findViewById(R.id.bookTitle);
        this.bookAuthor = retval.findViewById(R.id.bookAuthor);
        this.imgHolder = retval.findViewById(R.id.imageView);
        this.download = retval.findViewById(R.id.download);

        return retval;
    }

    public void displayBook(Book book) {
        this.book = book;
        this.bookTitle.setText(book.title());
        this.bookAuthor.setText(book.author());

        @SuppressLint("StaticFieldLeak")
        AsyncTask task = new AsyncTask() {
            Context context = null;
            Book book = null;
            Drawable drawable = null;
            Boolean hasLocal = null;

            @Override
            protected void onPreExecute() {
                BookDetailsFragment b = BookDetailsFragment.this;
                this.context = b.getContext();
                this.book = b.book;
            }

            @Override
            protected Object doInBackground(Object[] objects) {
                this.drawable = WebIO.loadImageFromWebOperations(this.book.cover());
                this.hasLocal = FileIO.hasLocalAudio(this.context.getFilesDir(), this.book);
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                BookDetailsFragment b = BookDetailsFragment.this;
                b.imgHolder.setImageDrawable(this.drawable);
                if (this.hasLocal) {
                    b.download.setText("Delete");
                } else {
                    b.download.setText("Download");
                }
            }
        };
        task.execute(book.cover());
    }



    public Book book() {
        return this.book;
    }
}
