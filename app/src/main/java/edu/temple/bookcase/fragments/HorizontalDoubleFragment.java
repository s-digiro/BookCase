package edu.temple.bookcase.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import edu.temple.bookcase.R;

public class HorizontalDoubleFragment extends Fragment {
    private Fragment left;
    private Fragment right;

    public static HorizontalDoubleFragment newInstance(Fragment left, Fragment right) {
        HorizontalDoubleFragment retval = new HorizontalDoubleFragment();
        retval.left = left;
        retval.right = right;

        return retval;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View retval = inflater.inflate(R.layout.fragment_horizontal_double, container, false);

        FragmentManager fm = this.getActivity().getSupportFragmentManager();

        FragmentTransaction addLeft = fm.beginTransaction();
        addLeft.add(R.id.left, this.left);
        addLeft.commit();

        FragmentTransaction addRight = fm.beginTransaction();
        addRight.add(R.id.right, this.right);
        addRight.commit();

        return retval;
    }
}
