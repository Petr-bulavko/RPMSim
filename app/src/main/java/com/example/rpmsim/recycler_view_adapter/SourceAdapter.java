package com.example.rpmsim.recycler_view_adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rpmsim.R;
import com.example.rpmsim.activity.EditSource;
import com.example.rpmsim.entity.Source;
import com.example.rpmsim.fragment.FragmentAddDetector;
import com.example.rpmsim.fragment.FragmentEditDetector;
import com.example.rpmsim.fragment.FragmentEditSource;

import java.util.ArrayList;


public class SourceAdapter extends RecyclerView.Adapter<SourceAdapter.ViewHolder> {

    Context context;
    ArrayList<String> arrayList;
    ArrayList<Source> arrayListSource;

    MainViewModel mainViewModel;
    boolean isEnable = false;
    boolean isSelectAll = false;
    ArrayList<String> selectList = new ArrayList<>();

    public SourceAdapter(Context context, ArrayList<String> arrayList, ArrayList<Source> arrayListSource) {
        this.context = context;
        this.arrayList = arrayList;
        this.arrayListSource = arrayListSource;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.item_main, parent, false);

        mainViewModel = ViewModelProviders.of((FragmentActivity) context)
                .get(MainViewModel.class);

        return new ViewHolder(view);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        holder.textView.setText(String.format("%d. %s", position + 1, arrayList.get(position)));

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            //Значит долгое нажатие))))))
            public boolean onLongClick(View v) {
                // check condition
                if (!isEnable) {
                    // when action mode is not enable
                    // initialize action mode
                    ActionMode.Callback callback = new ActionMode.Callback() {
                        @Override
                        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                            // initialize menu inflater
                            MenuInflater menuInflater = mode.getMenuInflater();
                            // inflate menu
                            menuInflater.inflate(R.menu.menu, menu);
                            return true;
                        }

                        @Override
                        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                            // when action mode is prepare
                            // set isEnable true
                            isEnable = true;
                            // create method
                            ClickItem(holder);
                            // set observer on getText method
                            mainViewModel.getText().observe((LifecycleOwner) context
                                    , new Observer<String>() {
                                        @Override
                                        public void onChanged(String s) {
                                            // when text change
                                            // set text on action mode title
                                            mode.setTitle(String.format("%s Selected", s));
                                        }
                                    });
                            return true;
                        }

                        @SuppressLint({"NotifyDataSetChanged", "NonConstantResourceId"})
                        @Override
                        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                            // when click on action mode item
                            int id = item.getItemId();
                            switch (id) {
                                case R.id.menu_delete:
                                    // when click on delete
                                    // use for loop
                                    for (String s : selectList) {
                                        arrayListSource.remove(arrayList.indexOf(s));
                                        arrayList.remove(s);

                                    }
                                    // finish action mode
                                    mode.finish();
                                    break;

                                case R.id.menu_select_all:
                                    // when click on select all
                                    if (selectList.size() == arrayList.size()) {
                                        // when all item selected
                                        // set is selected false
                                        isSelectAll = false;
                                        // create select array list
                                        selectList.clear();
                                    } else {
                                        // when  all item unselected
                                        // set isSelectALL true
                                        isSelectAll = true;
                                        // clear select array list
                                        selectList.clear();
                                        // add value in select array list
                                        selectList.addAll(arrayList);
                                    }
                                    // set text on view model
                                    mainViewModel.setText(String.valueOf(selectList.size()));
                                    // notify adapter
                                    notifyDataSetChanged();
                                    break;
                            }
                            // return true
                            return true;
                        }

                        @SuppressLint("NotifyDataSetChanged")
                        @Override
                        public void onDestroyActionMode(ActionMode mode) {
                            // when action mode is destroy
                            // set isEnable false
                            isEnable = false;
                            // set isSelectAll false
                            isSelectAll = false;
                            // clear select array list
                            selectList.clear();
                            // notify adapter
                            notifyDataSetChanged();
                        }
                    };
                    // start action mode
                    ((AppCompatActivity) v.getContext()).startActionMode(callback);
                } else {
                    // when action mode is already enable
                    // call method
                    ClickItem(holder);
                }
                // return true
                return true;
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEnable) {
                    ClickItem(holder);
                } else {
                    FragmentEditSource fragmentEditSource = new FragmentEditSource();
                    Bundle args = new Bundle();
                    args.putSerializable("sources", arrayListSource);
                    args.putInt("position", position);
                    fragmentEditSource.setArguments(args);
                    FragmentManager fragmentManager = ((AppCompatActivity)context).getSupportFragmentManager();
                    fragmentManager.beginTransaction().setCustomAnimations(R.anim.to_left_in, R.anim.to_left_out).replace(R.id.swipe_page_two, fragmentEditSource).commit();
                }
            }
        });
        if (isSelectAll) {
            holder.checkbox.setVisibility(View.VISIBLE);
            holder.itemView.setBackgroundColor(Color.LTGRAY);
        } else {
            holder.checkbox.setVisibility(View.GONE);
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    private void ClickItem(ViewHolder holder) {
        // get selected item value
        String s = arrayList.get(holder.getAdapterPosition());
        if (holder.checkbox.getVisibility() == View.GONE) {
            // when item not selected
            holder.checkbox.setVisibility(View.VISIBLE);
            holder.itemView.setBackgroundColor(Color.LTGRAY);
            selectList.add(s);
        } else {
            // when item selected
            holder.checkbox.setVisibility(View.GONE);
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
            selectList.remove(s);

        }
        // set text on view model
        mainViewModel.setText(String.valueOf(selectList.size()));
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        // initialize variables
        TextView textView;
        ImageView checkbox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // assign variables
            textView = itemView.findViewById(R.id.txtSource_edit);
            checkbox = itemView.findViewById(R.id.check_box);

        }
    }
}
