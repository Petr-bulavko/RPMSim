package com.example.rpmsim.fragment_for_swipe;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.rpmsim.R;
import com.example.rpmsim.fragment.FragmentDetector;

import java.util.Objects;

public class FragmentPageOne extends Fragment {

    private int pageNumber;
    FragmentManager fragmentManager;

    final String LOG_TAG = "myLogs";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageNumber = getArguments() != null ? getArguments().getInt("page_number") : 1;
        Log.d(LOG_TAG, "onCreate_fragment_page_one");

        //Пока не ебу
        //Фрагмент приходит, но не восстанавливается

        if (savedInstanceState != null) {
            FragmentDetector fragmentDetector = (FragmentDetector) requireActivity().getSupportFragmentManager().getFragment(savedInstanceState, "fragmentInstanceSaved");
            fragmentManager = requireActivity().getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.swipe_page_one, Objects.requireNonNull(fragmentDetector)).commit();
        }else {
            fragmentManager = requireActivity().getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.swipe_page_one, new FragmentDetector()).commit();
        }

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.fragment_page_one, container, false);
        Log.d(LOG_TAG, "onCreateView_fragment_page_one");
        return result;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(LOG_TAG, "onViewCreated_fragment_page_one");
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        Log.d(LOG_TAG, "onViewStateRestored_fragment_page_one");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(LOG_TAG, "onStart_fragment_page_one");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "onResume_fragment_page_one");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(LOG_TAG, "onPause_fragment_page_one");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(LOG_TAG, "onStop_fragment_page_one");
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(LOG_TAG, "onSaveInstanceState");
        requireActivity().getSupportFragmentManager().
                putFragment(outState, "fragmentInstanceSaved",
                        Objects.requireNonNull(requireActivity().
                                getSupportFragmentManager().findFragmentById(R.id.swipe_page_one)));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(LOG_TAG, "onDestroyView_fragment_page_one");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy_fragment_page_one");
    }

    //Если понадобится номер страницы
    public static FragmentPageOne newInstance(int page) {
        FragmentPageOne fragmentPageOne = new FragmentPageOne();
        Bundle args = new Bundle();
        args.putInt("page_number", page);
        fragmentPageOne.setArguments(args);
        return fragmentPageOne;
    }
}
