package com.example.rpmsim.fragment_adpter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.rpmsim.fragment.FragmentAddMaterial;
import com.example.rpmsim.fragment.FragmentAddSource;
import com.example.rpmsim.fragment.FragmentDetector;
import com.example.rpmsim.fragment.FragmentOtherParameter;
import com.example.rpmsim.fragment.FragmentResult;

public class MyAdapter extends FragmentStateAdapter {

    public MyAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 1:
                return new FragmentAddSource();
            case 2:
                return new FragmentAddMaterial();
            case 3:
                return new FragmentOtherParameter();
            case 4:
                return new FragmentResult();
            default:
                return new FragmentDetector();
        }
    }

    @Override
    public int getItemCount() {
        return 5;
    }

}
