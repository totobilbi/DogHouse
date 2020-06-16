package com.roeico7.dogadopt.logic;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.Fragment;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.roeico7.dogadopt.R;

import java.util.HashMap;

import static com.roeico7.dogadopt.logic.GeneralStuff.resources;


public class PermissionFragment extends AppCompatDialogFragment {

    private Button btnGrant;
    private Fragment fragment;
    private HashMap<String, String> userDetails = new HashMap<>();

    public PermissionFragment() {

    }


    public PermissionFragment(HashMap<String, String> userDetails) {
        this.userDetails = userDetails;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_premission, container, false);

        int widthPixels = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
        int heightPixels = (int) (getResources().getDisplayMetrics().heightPixels * 0.9);

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(widthPixels, heightPixels);
        v.setLayoutParams(layoutParams);

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fragment = this;

        btnGrant = view.findViewById(R.id.btn_grant);

        btnGrant.setOnClickListener(v -> Dexter.withActivity(getActivity())
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        getActivity().getSupportFragmentManager().beginTransaction().remove(fragment).commit();

                        if (!userDetails.isEmpty()) {
                            FragmentMap cdf = new FragmentMap(userDetails);
                            cdf.setCancelable(false);
                            cdf.show(getActivity().getSupportFragmentManager(), "mapFragment");
                        }
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        if (response.isPermanentlyDenied()) {
                            ProgressDialog progressDialog = new ProgressDialog(getContext());
                            GeneralStuff.shared.executeAlert(progressDialog,
                                    resources.getString(R.string.permission_denied),
                                    resources.getString(R.string.permission_denied_message),
                                    false,
                                    () -> {
                                        Intent intent = new Intent();
                                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        intent.setData(Uri.fromParts("package", getContext().getPackageName(), null));
                                    });
                        } else {
                            Toast.makeText(getContext(), resources.getString(R.string.permission_denied), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                })
                .check());
    }


    @Override
    public void onResume() {
        super.onResume();
        if (!userDetails.isEmpty()) {
            getDialog().setOnKeyListener((dialog, keyCode, event) -> {
                if ((keyCode == android.view.KeyEvent.KEYCODE_BACK)) {
                    //This is the filter
                    if (event.getAction() != KeyEvent.ACTION_DOWN)
                        return true;
                    else {
                        ProgressDialog progressDialog = new ProgressDialog(getContext());
                        GeneralStuff.shared.executeAlert(progressDialog,
                                resources.getString(R.string.ui_cancel_register),
                                resources.getString(R.string.ui_cancel_register_text),
                                false,
                                () -> {
                                    getActivity().getSupportFragmentManager().beginTransaction().remove(fragment).commit();
                                    getActivity().getSupportFragmentManager().popBackStack();
                                });
                        return true;
                    }
                } else
                    return false;
            });
        }
    }

}

