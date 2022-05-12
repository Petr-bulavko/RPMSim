package com.example.rpmsim.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rpmsim.R;
import com.example.rpmsim.activity.AddDetector;
import com.example.rpmsim.entity.Detector;
import com.example.rpmsim.recycler_view_adapter.DetectorAdapter;

import java.util.ArrayList;
import java.util.Locale;

//Главная страница)
public class FragmentDetector extends Fragment {

    private static ArrayList<Detector> detectors = new ArrayList<>();
    final String LOG_TAG = "myLogs";

    private RecyclerView recycler_view_detector;

    private ArrayList<String> arrayList;

    public static ArrayList<Detector> getDetectors() {
        return detectors;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.fragment_detector, container, false);
        Log.d(LOG_TAG, "onCreateView_fragment_detector");
        recycler_view_detector = result.findViewById(R.id.recycler_view_detector);
        Button addNewDetector = result.findViewById(R.id.addNewDetector);

        addNewDetector.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), AddDetector.class);
                requireContext().startActivity(intent);
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
        arrayList = new ArrayList<>();
        for (int i = 0; i < detectors.size(); i++) {
            arrayList.add(String.format(Locale.ROOT,"%s - (%.1f, %.1f, %.1f) фон - %.1f",
                    detectors.get(i).getNameDetector(), detectors.get(i).getX(),
                    detectors.get(i).getY(), detectors.get(i).getZ(), detectors.get(i).getBackground()));
        }

        recycler_view_detector.setLayoutManager(new LinearLayoutManager(getActivity()));
        DetectorAdapter adapter = new DetectorAdapter(getActivity(), arrayList, detectors);
        recycler_view_detector.setAdapter(adapter);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(LOG_TAG, "onPause_fragment_detector");
    }

    public static void add_detectors(Detector detector) {
        detectors.add(detector);
    }
    public static void save_detectors(ArrayList<Detector> arrayList) {
        detectors = arrayList;
    }
}
