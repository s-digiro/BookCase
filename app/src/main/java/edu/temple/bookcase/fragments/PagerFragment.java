package edu.temple.bookcase.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import java.util.List;

import edu.temple.bookcase.Book;
import edu.temple.bookcase.adapters.FragmentAdapter;
import edu.temple.bookcase.R;

public class PagerFragment extends Fragment {

    List<Fragment> fragments;

    public static PagerFragment newInstance(List<Fragment> fragments) {
        PagerFragment retval = new PagerFragment();
        retval.fragments = fragments;

        return retval;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View retval = inflater.inflate(R.layout.fragment_pager, container, false);
        FragmentManager fm = this.getActivity().getSupportFragmentManager();
        ((ViewPager) retval.findViewById(R.id.viewPager)).setAdapter(new FragmentAdapter(fm, this.fragments));

        return retval;
    }

}
