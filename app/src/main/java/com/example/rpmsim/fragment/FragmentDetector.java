package com.example.rpmsim.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rpmsim.R;
import com.example.rpmsim.entity.Detector;
import com.example.rpmsim.recycler_view_adapter.DetectorAdapter;

import java.util.ArrayList;

public class FragmentDetector extends Fragment {

    final String LOG_TAG = "myLogs";

    private RecyclerView recycler_view_detector;

    ArrayList<Detector> detectors = new ArrayList<>();
    ArrayList<String> arrayList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.fragment_detector, container, false);
        Log.d(LOG_TAG, "onCreateView_fragment_detector");
        recycler_view_detector = result.findViewById(R.id.recycler_view_detector);
        Button addNewDetector = result.findViewById(R.id.addNewDetector);

        addNewDetector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.swipe_page_one, FragmentAddDetector.newInstance(detectors)).commit();
            }
        });

        return result;
    }

    public static FragmentDetector newInstance(ArrayList<Detector> detectors) {
        FragmentDetector fragmentDetector = new FragmentDetector();
        Bundle args = new Bundle();
        args.putSerializable("detectors", detectors);
        fragmentDetector.setArguments(args);
        return fragmentDetector;
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onResume() {
        super.onResume();

        Log.d(LOG_TAG, "onResume_fragment_detector");

        Bundle args = getArguments();
        if (args != null) {
            detectors = (ArrayList<Detector>) args.getSerializable("detectors");
        }

        for (int i = 0; i < detectors.size(); i++) {
            arrayList.add(String.format("%s - (%.0f, %.0f, %.0f) фон - %.1f",
                    detectors.get(i).getNameDetector(), detectors.get(i).getX(),
                    detectors.get(i).getY(), detectors.get(i).getZ(), detectors.get(i).getBackground()));
        }

        recycler_view_detector.setLayoutManager(new LinearLayoutManager(getActivity()));
        DetectorAdapter adapter = new DetectorAdapter(getActivity(), arrayList, detectors);
        recycler_view_detector.setAdapter(adapter);

        //Короче тут беру массив detectors и закидываю в bundle

        Bundle bundle = new Bundle();
        bundle.putSerializable("detectors", detectors);
        getParentFragmentManager().setFragmentResult("request_detectors", bundle);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(LOG_TAG, "onPause_fragment_detector");
    }
}
