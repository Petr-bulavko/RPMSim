package com.example.rpmsim.fragment_for_swipe;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.rpmsim.R;

public class FragmentPage extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.fragment_page, container, false);
        return result;
    }

    public static FragmentPage newInstance(String category) {
        Bundle args = new Bundle();
        args.putString("category", category);
        FragmentPage fragment = new FragmentPage();
        fragment.setArguments(args);
        return fragment;
    }


}
