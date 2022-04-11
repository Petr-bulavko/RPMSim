package com.example.rpmsim.fragment_for_swipe;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.rpmsim.R;
import com.example.rpmsim.fragment.FragmentAddSource;
import com.example.rpmsim.fragment.FragmentDetector;

public class FragmentPageTwo extends Fragment {

    private int pageNumber;
    FragmentManager fragmentManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.fragment_page_two, container, false);

        fragmentManager = requireActivity().getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.swipe_page_two, new FragmentAddSource()).commit();

        return result;
    }


    //Если понадобится номер страницы
    public static FragmentPageTwo newInstance(int page) {
        FragmentPageTwo fragmentPageOne = new FragmentPageTwo();
        Bundle args = new Bundle();
        args.putInt("page_number", page);
        fragmentPageOne.setArguments(args);
        return fragmentPageOne;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageNumber = getArguments() != null ? getArguments().getInt("page_number") : 1;

    }

}