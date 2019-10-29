package edu.temple.bookcase.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import edu.temple.bookcase.R;

public class BookDetailsFragment extends Fragment {
    private TextView bookTitle;

    public static BookDetailsFragment newInstance() {
        return new BookDetailsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View retval = inflater.inflate(R.layout.fragment_book_details, container, false);
        this.bookTitle = retval.findViewById(R.id.bookTitle);

        return retval;
    }

    public void displayBook(String book) {
        this.bookTitle.setText(book);
    }
}
