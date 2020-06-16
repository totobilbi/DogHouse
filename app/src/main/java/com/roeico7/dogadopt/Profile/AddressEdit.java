package com.roeico7.dogadopt.Profile;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.roeico7.dogadopt.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class AddressEdit extends AppCompatDialogFragment {

    private View container;

    public AddressEdit() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_address_edit, container, false);

        int widthPixels = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
        int heightPixels = (int) (getResources().getDisplayMetrics().heightPixels * 0.9);

        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(widthPixels, heightPixels);
        v.setLayoutParams(layoutParams);


        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        container = view.findViewById(R.id.container);
    }




}
