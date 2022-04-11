package com.example.rpmsim.fragment_adpter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.rpmsim.fragment.FragmentAddSource;
import com.example.rpmsim.fragment.FragmentDetector;
import com.example.rpmsim.fragment.FragmentOtherParameter;
import com.example.rpmsim.fragment.FragmentResult;
import com.example.rpmsim.fragment_for_swipe.FragmentPageFour;
import com.example.rpmsim.fragment_for_swipe.FragmentPageOne;
import com.example.rpmsim.fragment_for_swipe.FragmentPageThree;
import com.example.rpmsim.fragment_for_swipe.FragmentPageTwo;

public class MyAdapter extends FragmentStateAdapter {

    public MyAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 0:
                //return new FragmentDetector();
                return FragmentPageOne.newInstance(position);
            case 1:
                //return new FragmentAddSource();
                return FragmentPageTwo.newInstance(position);
            case 2:
                return new FragmentOtherParameter();
                //return FragmentPageThree.newInstance(position);
            case 3:
                return new FragmentResult();
                //return FragmentPageFour.newInstance(position);
            default:
                return new FragmentDetector();
        }
    }

    //Короче если больше 4, то работает onDestroy
    @Override
    public int getItemCount() {
        return 4;
    }

}
