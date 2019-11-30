package edu.temple.bookcase.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

import edu.temple.bookcase.Book;
import edu.temple.bookcase.MainActivity;
import edu.temple.bookcase.R;
import edu.temple.bookcase.adapters.StringArrayAdapter;

public class BookListFragment extends Fragment {
    private static final String TITLES = "titles";

    private String[] books;

    public static BookListFragment newInstance(List<Book> books) {
        int size = books.size();

        String[] bookTitles = new String[size];

        for (int i = 0; i < bookTitles.length; ++i) {
            Book b = books.get(i);
            bookTitles[i] = b.title();
        }

        BookListFragment retval = new BookListFragment();

        Bundle bundle = new Bundle();
        bundle.putStringArray(TITLES, bookTitles);
        retval.setArguments(bundle);

        return retval;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Context context = this.getContext();
        Bundle args = this.getArguments();
        View retval = inflater.inflate(R.layout.fragment_book_list, container, false);

        if (context != null && args != null) {
            this.books = args.getStringArray(TITLES);
            StringArrayAdapter adapter = new StringArrayAdapter(context, this.books);
            ListView bookListView = retval.findViewById(R.id.bookList);
            bookListView.setAdapter(adapter);

            bookListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Activity activity = BookListFragment.this.getActivity();
                    if (activity != null) {
                        ((MainActivity)activity).selectBook(position);
                    }
                }
            });
        }

        return retval;
    }

}
