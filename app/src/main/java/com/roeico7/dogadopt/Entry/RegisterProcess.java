package com.roeico7.dogadopt.Entry;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.roeico7.dogadopt.R;

import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 */
public class RegisterProcess extends Fragment {
    private HashMap<String, String> userDetails = new HashMap<>();



    public RegisterProcess() {
        // Required empty public constructor
    }


    public RegisterProcess(HashMap<String, String> userDetails) {
        this.userDetails = userDetails;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_blank, container, false);

        PhoneVerification cdf = new PhoneVerification(userDetails);
        cdf.setCancelable(false);
        cdf.show(getActivity().getSupportFragmentManager(), "phoneVerify");

        return v;
    }







}
