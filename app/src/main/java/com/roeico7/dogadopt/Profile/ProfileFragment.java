package com.roeico7.dogadopt.Profile;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.roeico7.dogadopt.R;
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.roeico7.dogadopt.Translate.Translate;
import com.roeico7.dogadopt.Translate.Translator;
import com.roeico7.dogadopt.logic.FirebaseDAO;
import com.roeico7.dogadopt.logic.FragmentMap;
import com.roeico7.dogadopt.logic.GeneralStuff;
import com.roeico7.dogadopt.logic.PermissionFragment;
import com.roeico7.dogadopt.objects.User;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import static android.app.Activity.RESULT_OK;
import static com.roeico7.dogadopt.logic.GeneralStuff.resources;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {


    private Uri avatarUri;
    private ImageView iv_avatar;
    private TextView tv_name, tv_email, tv_phone;
    private TextView human_name, human_age, human_gender, human_address, human_city, human_zipCode;
    private ImageView iv_edit_personal_info, iv_edit_address_info;
    private Button btn_logout;
    private static final int PICK_IMAGE_REQUEST = 1;
    private String avatar;
    private StorageTask mUploadTask;
    private StorageReference fileRef;
    private Task mRemoveAvatar;
    private String uid;
    private ProgressDialog pd;
    private FloatingActionButton btn_message, btn_phone, btn_location;





    public ProfileFragment() {
        // Required empty public constructor
    }





    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_profile, container, false);


        iv_avatar = view.findViewById(R.id.iv_avatar);
        tv_name = view.findViewById(R.id.tv_name);
        tv_email = view.findViewById(R.id.tv_email);
        tv_phone = view.findViewById(R.id.tv_phone);

        human_name = view.findViewById(R.id.human_name);
        human_age = view.findViewById(R.id.human_age);
        human_gender = view.findViewById(R.id.human_gender);
        human_address = view.findViewById(R.id.human_address);
        human_city = view.findViewById(R.id.human_city);
        human_zipCode = view.findViewById(R.id.human_zipCode);

        iv_edit_personal_info = view.findViewById(R.id.iv_edit_personal_info);
        iv_edit_address_info = view.findViewById(R.id.iv_edit_address_info);

        btn_logout = view.findViewById(R.id.btn_logout);



        if (!ProfileFragmentArgs.fromBundle(getArguments()).getUid().equals("null")) {
            uid = ProfileFragmentArgs.fromBundle(getArguments()).getUid();
        } else {
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        pd = new ProgressDialog(getActivity());


        if (uid.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            myProfile();
        } else {
            btn_message = view.findViewById(R.id.btn_message);
            btn_phone = view.findViewById(R.id.btn_phone);
            btn_location = view.findViewById(R.id.btn_location);
            otherProfile();
        }



        return view;
    }



    private void myProfile() {
        iv_edit_personal_info.setOnClickListener(v -> openPersonalForm());
        iv_edit_address_info.setOnClickListener(v -> openAddressForm());
        btn_logout.setOnClickListener(this::logout);
        iv_avatar.setOnClickListener(v -> GeneralStuff.shared.openFileChooser(this));
    }



    private void otherProfile(){
        iv_edit_personal_info.setVisibility(View.INVISIBLE);
        iv_edit_address_info.setVisibility(View.INVISIBLE);
        btn_logout.setVisibility(View.INVISIBLE);

        btn_message.setVisibility(View.VISIBLE);
        btn_phone.setVisibility(View.VISIBLE);
        btn_location.setVisibility(View.VISIBLE);

        // message owner of dog
        btn_message.setOnClickListener(b-> {
            ProfileFragmentDirections.ActionNavigationProfileToChatFrag action = ProfileFragmentDirections.actionNavigationProfileToChatFrag(uid);
            NavHostFragment.findNavController(this).navigate(action);
        });


        //call owner of dog
        btn_phone.setOnClickListener(b-> {
            User user = GeneralStuff.shared.getUserById(uid);
            String phone = user.getPhone();
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null));
            startActivity(intent);
        });


        //navigate to owner location
        btn_location.setOnClickListener(b-> {
            User user = GeneralStuff.shared.getUserById(uid);
            HashMap<String, String> profileInfo = user.getProfileInfo();
            Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("geo:0,0" + "?q=" + profileInfo.get("streetAddress") + "+" + profileInfo.get("city")));
            startActivity(intent);
        });
    }



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        loadTopInfo();
        loadBottomInfo();


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            avatarUri = data.getData();


            if (!avatar.equals("null")) {
                fileRef = FirebaseStorage.getInstance().getReferenceFromUrl(avatar);
                mRemoveAvatar = fileRef.delete()
                        .addOnSuccessListener(removeSuccessListener)
                        .addOnFailureListener(removeFailureListener);
            } else {
                FirebaseDAO.shared.uploadProfilePicToStorage(getContext(), avatarUri, resources.getString(R.string.key_avatar));
            }


        }
    }


    private OnSuccessListener removeSuccessListener = (OnSuccessListener<Void>) aVoid -> {
        FirebaseDAO.shared.uploadProfilePicToStorage(getContext(), avatarUri, resources.getString(R.string.key_avatar));
    };

    private OnFailureListener removeFailureListener = e -> GeneralStuff.shared.showError(getContext(), e);



    private void loadTopInfo() {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference(resources.getString(R.string.key_users));
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    if (uid.equals(ds.getKey())) {
                        String username = "" + ds.child(resources.getString(R.string.key_username)).getValue();
                        String email = "" + ds.child(resources.getString(R.string.key_email)).getValue();
                        String phone = "" + ds.child(resources.getString(R.string.key_phone)).getValue();
                        avatar = "" + ds.child(resources.getString(R.string.key_avatar)).getValue();


                        tv_name.setText(username);
                        tv_email.setText(email);
                        tv_phone.setText(phone);

                        if (!avatar.equals("null")) {
                            Picasso.get().load(avatar).into(iv_avatar);
                            iv_avatar.setBackgroundResource(R.drawable.add_dog_pic_border);
                        }

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void loadBottomInfo() {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference(resources.getString(R.string.key_users));
        dbRef.addValueEventListener(new ValueEventListener() {

            class TranslateListener implements Translator.Listener {
                private String s;

                public TranslateListener(String s) {
                    this.s = s;
                }

                @Override
                public void textTranslated(String translatedText) {
                    if (!translatedText.equals("null")) {
                        switch (s) {
                            case "fullName":
                                human_name.setText(translatedText);
                                break;
                            case "streetAddress":
                                human_address.setText(translatedText);
                                break;
                            case "city":
                                human_city.setText(translatedText);
                                break;
                            case "zipCode":
                                human_zipCode.setText(translatedText);
                                break;
                        }
                    }
                }
            }

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    if (uid.equals(ds.getKey())) {
                        DataSnapshot childProfileInfo = ds.child(resources.getString(R.string.key_profileInfo));

                        String fullName = "" + childProfileInfo.child(resources.getString(R.string.key_fullName)).getValue();
                        String age = "" + childProfileInfo.child(resources.getString(R.string.key_age)).getValue();
                        String gender = "" + childProfileInfo.child(resources.getString(R.string.key_gender)).getValue();
                        String streetAddress = "" + childProfileInfo.child(resources.getString(R.string.key_streetAddress)).getValue();
                        String city = "" + childProfileInfo.child(resources.getString(R.string.key_city)).getValue();
                        String zipCode = "" + childProfileInfo.child(resources.getString(R.string.key_zipCode)).getValue();


                        new Translator(fullName, new TranslateListener("fullName"));
                        new Translator(streetAddress, new TranslateListener("streetAddress"));
                        new Translator(city, new TranslateListener("city"));
                        new Translator(zipCode, new TranslateListener("zipCode"));


                        if (!age.equals("null")) {
                            if(Translate.shared.checkDisplayLanguage())
                                human_age.setText(resources.getString(R.string.years_old) + " " + age);
                            else
                                human_age.setText(age + " " + resources.getString(R.string.years_old));
                        }

                        if (!gender.equals("null")) {
                            human_gender.setText(Translate.shared.translateGender(gender));
                        }

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }





    private void openPersonalForm() {
        InfoEdit cdf;
        iv_edit_personal_info.setClickable(false);
        new Handler().postDelayed(() -> {
            iv_edit_personal_info.setClickable(true);
        }, 2000);
        if(Translate.shared.checkDisplayLanguage()) {
            cdf = new InfoEdit(human_name.getText().toString(), human_age.getText().toString().substring(human_age.getText().toString().length()-2).trim(), human_gender.getText().toString());
        } else {
            cdf = new InfoEdit(human_name.getText().toString(), human_age.getText().toString().substring(0, 2).trim(), human_gender.getText().toString());
        }
        cdf.setCancelable(false);
        cdf.show(getActivity().getSupportFragmentManager(), "editProfile");
    }


    private void openAddressForm() {
        iv_edit_address_info.setClickable(false);
        new Handler().postDelayed(() -> {
            iv_edit_address_info.setClickable(true);
        }, 2000);
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            FragmentMap cdf = new FragmentMap();
            cdf.setCancelable(false);
            cdf.show(getActivity().getSupportFragmentManager(), "mapFragment");
        } else {
            PermissionFragment cdf = new PermissionFragment();
            cdf.show(getActivity().getSupportFragmentManager(), "permissionFragment");
        }

    }



    private void logout(View v ) {
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        GeneralStuff.shared.executeAlert(progressDialog,
                resources.getString(R.string.ui_user_logout),
                resources.getString(R.string.ui_user_logout_text),
                false,
                () -> {
                    FirebaseAuth.getInstance().signOut();
                    LoginManager.getInstance().logOut();
                });
    }



}
