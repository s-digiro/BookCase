package edu.temple.bookcase;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import edu.temple.bookcase.fragments.BookDetailsFragment;
import edu.temple.bookcase.fragments.BookListFragment;
import edu.temple.bookcase.fragments.HorizontalDoubleFragment;
import edu.temple.bookcase.fragments.PagerFragment;

public class MainActivity extends AppCompatActivity {

    int orientation;

    String[] books;
    ViewGroup holder;
    Fragment bookList;
    BookDetailsFragment bookDetails;

    boolean isTablet = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.books = this.getResources().getStringArray(R.array.books);
        this.holder = this.findViewById(R.id.holder);
        this.bookList = BookListFragment.newInstance(books);
        this.bookDetails = BookDetailsFragment.newInstance();

        this.isTablet = this.getResources().getBoolean(R.bool.isTablet);
        this.orientation = this.getResources().getConfiguration().orientation;
        if (this.isTablet) {
            this.setForOrientation(Configuration.ORIENTATION_LANDSCAPE);
        } else {
            this.setForOrientation(this.orientation);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void setForOrientation(int orientation) {
        this.holder.removeAllViews();
        Fragment frag;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            frag = HorizontalDoubleFragment.newInstance(this.bookList, this.bookDetails);
        } else {
            List<Fragment> list = new ArrayList<>();
            list.add(this.bookList);
            list.add(this.bookDetails);
            frag = PagerFragment.newInstance(list);
        }
        FragmentTransaction ft = this.getSupportFragmentManager().beginTransaction();
        ft.add(R.id.holder, frag);
        ft.commit();
    }

    public void selectBook(int index) {
        this.bookDetails.displayBook((books[index]));
    }
}
