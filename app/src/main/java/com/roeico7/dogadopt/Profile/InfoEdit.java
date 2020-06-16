package com.roeico7.dogadopt.Profile;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.roeico7.dogadopt.R;
import com.roeico7.dogadopt.logic.FragmentMap;
import com.roeico7.dogadopt.logic.GeneralStuff;
import com.roeico7.dogadopt.logic.PermissionFragment;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import static android.app.Activity.RESULT_OK;
import static com.roeico7.dogadopt.logic.GeneralStuff.resources;

/**
 * A simple {@link Fragment} subclass.
 */
public class InfoEdit extends AppCompatDialogFragment {
    private TextView human_name, gender_type;
    private ToggleButton gender_button;
    private NumberPicker np;
    private Button btn_cancel;
    private Button btn_save;
    private String name;
    private String age;
    private String gender = resources.getString(R.string.male);
    private ProgressDialog pd;
    private HashMap<String, String> userDetails = new HashMap<>();
    private ImageView iv_avatar;
    private Uri avatarUri;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Fragment mFragment;


    public InfoEdit() {
        // Required empty public constructor
    }

    public InfoEdit(HashMap<String, String> userDetails) {
        this.userDetails = userDetails;
    }

    public InfoEdit(String name, String age, String gender) {
        this.name = name;
        this.age = age;
        this.gender = gender;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v;
        mFragment = this;
        int widthPixels, heightPixels;
        if (userDetails.isEmpty()) {
            v = inflater.inflate(R.layout.fragment_info_edit, container, false);
            heightPixels = (int) (getResources().getDisplayMetrics().heightPixels * 0.6);

        } else {
            v = inflater.inflate(R.layout.fragment_register_info, container, false);
            heightPixels = (int) (getResources().getDisplayMetrics().heightPixels * 0.8);
        }

        widthPixels = (int) (getResources().getDisplayMetrics().widthPixels * 0.8);
        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(widthPixels, heightPixels);
        v.setLayoutParams(layoutParams);


        np = v.findViewById(R.id.numberPicker);
        np.setMinValue(1);
        np.setMaxValue(99);


        human_name = v.findViewById(R.id.human_name);
        gender_type = v.findViewById(R.id.gender_type);
        gender_button = v.findViewById(R.id.gender_button);

        if (name != null && age != null && gender != null) {
            human_name.setText(name);
            np.setValue(Integer.parseInt(age));
            gender_button.setChecked(gender.equals(resources.getString(R.string.female)));
        }

        gender_button.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                gender_type.setText(resources.getString(R.string.female));
                gender = resources.getString(R.string.female);
            } else {
                gender_type.setText(resources.getString(R.string.male));
                gender = resources.getString(R.string.male);
            }
        });


        if (userDetails.isEmpty()) {
            btn_cancel = v.findViewById(R.id.btn_cancel);
            btn_save = v.findViewById(R.id.btn_save);

            btn_cancel.setOnClickListener(b -> {
                getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
            });

            btn_save.setOnClickListener(b -> {
                String uid = FirebaseAuth.getInstance().getUid();
                name = getName();
                age = String.valueOf(np.getValue());

                if (name == null || gender == null)
                    return;


                pd = new ProgressDialog(getContext());
                GeneralStuff.shared.executeProgress(pd, resources.getString(R.string.updating_profile), resources.getString(R.string.please_wait));

                HashMap<String, Object> result = new HashMap<>();
                result.put(resources.getString(R.string.key_fullName), name);
                result.put(resources.getString(R.string.key_age), age);
                result.put(resources.getString(R.string.key_gender), gender);


                FirebaseDatabase.getInstance().getReference(resources.getString(R.string.key_users))
                        .child(uid)
                        .child(resources.getString(R.string.key_profileInfo))
                        .updateChildren(result)
                        .addOnSuccessListener(aVoid -> {
                            pd.dismiss();
                            getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();

                        })
                        .addOnFailureListener(e -> {
                            pd.dismiss();
                            getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
                        });
            });
        } else {
            btn_save = v.findViewById(R.id.btn_next);
            iv_avatar = v.findViewById(R.id.iv_avatar);

            iv_avatar.setOnClickListener(b -> GeneralStuff.shared.openFileChooser(this));


            btn_save.setOnClickListener(b -> {
                name = getName();
                age = String.valueOf(np.getValue());

                if (name == null || gender == null)
                    return;

                if (avatarUri == null) {
                    Toast.makeText(getContext(), "Please upload a profile image", Toast.LENGTH_SHORT).show();
                    return;
                }
                userDetails.put(resources.getString(R.string.key_fullName), name);
                userDetails.put(resources.getString(R.string.key_age), age);
                userDetails.put(resources.getString(R.string.key_gender), gender);
                userDetails.put(resources.getString(R.string.key_avatar), "" + avatarUri);

                getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();

                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    FragmentMap cdf = new FragmentMap(userDetails);
                    cdf.setCancelable(false);
                    cdf.show(getActivity().getSupportFragmentManager(), "mapFragment");
                } else {
                    PermissionFragment cdf = new PermissionFragment(userDetails);
                    cdf.setCancelable(false);
                    cdf.show(getActivity().getSupportFragmentManager(), "permissionFragment");
                }

            });
        }


        return v;
    }


    private String getName() {
        if (!human_name.getText().toString().trim().equals("")) {
            return human_name.getText().toString();
        }
        human_name.setError(resources.getString(R.string.fill_field));
        return null;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            avatarUri = data.getData();
            Picasso.get().load(avatarUri).into(iv_avatar);
            iv_avatar.setBackgroundResource(R.drawable.add_dog_pic_border);
        }
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
                                    getActivity().getSupportFragmentManager().beginTransaction().remove(mFragment).commit();
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








