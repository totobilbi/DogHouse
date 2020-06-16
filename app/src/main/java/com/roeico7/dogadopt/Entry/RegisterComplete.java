package com.roeico7.dogadopt.Entry;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.roeico7.dogadopt.R;
import com.roeico7.dogadopt.logic.FirebaseDAO;
import com.roeico7.dogadopt.logic.GeneralStuff;
import com.roeico7.dogadopt.objects.Dog;

import java.util.ArrayList;
import java.util.HashMap;

import static com.roeico7.dogadopt.logic.GeneralStuff.resources;

public class RegisterComplete extends Fragment {
    private ProgressDialog progressDialog;
    private String myUid;
    private HashMap<String, String> userDetails = new HashMap<>();


    public RegisterComplete() {
        // Required empty public constructor
    }


    public RegisterComplete(HashMap<String, String> userDetails) {
        this.userDetails = userDetails;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_blank, container, false);

        progressDialog = new ProgressDialog(getContext());
        GeneralStuff.shared.executeProgress(progressDialog, GeneralStuff.resources.getString(R.string.creating_profile), GeneralStuff.resources.getString(R.string.please_wait));

        if(FirebaseAuth.getInstance().getCurrentUser()==null) {
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(userDetails.get(GeneralStuff.resources.getString(R.string.key_email)), userDetails.get(GeneralStuff.resources.getString(R.string.key_password))).
                    addOnSuccessListener(registerSuccessListener).
                    addOnFailureListener(mFailureListener);
        } else {
            createAccount();
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);



    }

    private OnSuccessListener registerSuccessListener = (OnSuccessListener<AuthResult>) authResult -> {
        createAccount();
    };


    private void createAccount() {
        if (progressDialog.isShowing())
            progressDialog.dismiss();

        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference(GeneralStuff.resources.getString(R.string.key_users)).child(myUid);

        // create dogList with 3 empty dogs for user
        ArrayList<Dog> dogs = new ArrayList<>();
        for (int i = 1; i < 4; i++)
            dogs.add(new Dog(myUid, "dog" + i, null, null, null, null, null));


        //add basic user info
        HashMap<String, Object> result = new HashMap<>();
        result.put(GeneralStuff.resources.getString(R.string.key_userID), myUid);
        result.put(GeneralStuff.resources.getString(R.string.key_username), userDetails.get(GeneralStuff.resources.getString(R.string.key_username)));
        result.put(GeneralStuff.resources.getString(R.string.key_phone), userDetails.get(GeneralStuff.resources.getString(R.string.key_phone)));
        result.put(GeneralStuff.resources.getString(R.string.key_email), userDetails.get(GeneralStuff.resources.getString(R.string.key_email)));
        result.put(GeneralStuff.resources.getString(R.string.key_password), userDetails.get(GeneralStuff.resources.getString(R.string.key_password)));
        result.put(GeneralStuff.resources.getString(R.string.key_dogList) ,dogs);
        //chat stuff
        result.put(GeneralStuff.resources.getString(R.string.key_onlineStatus), GeneralStuff.resources.getString(R.string.key_offline));
        result.put(GeneralStuff.resources.getString(R.string.key_typingTo), GeneralStuff.resources.getString(R.string.key_noOne));
        dbRef.updateChildren(result);


        // add user profile info
        result = new HashMap<>();
        result.put(GeneralStuff.resources.getString(R.string.key_fullName),  userDetails.get(GeneralStuff.resources.getString(R.string.key_fullName)));
        result.put(GeneralStuff.resources.getString(R.string.key_age),  userDetails.get(GeneralStuff.resources.getString(R.string.key_age)));
        result.put(GeneralStuff.resources.getString(R.string.key_gender),  userDetails.get(GeneralStuff.resources.getString(R.string.key_gender)));
        result.put(GeneralStuff.resources.getString(R.string.key_streetAddress),  userDetails.get(GeneralStuff.resources.getString(R.string.key_streetAddress)));
        result.put(GeneralStuff.resources.getString(R.string.key_city),  userDetails.get(GeneralStuff.resources.getString(R.string.key_city)));
        result.put(GeneralStuff.resources.getString(R.string.key_zipCode),  userDetails.get(GeneralStuff.resources.getString(R.string.key_zipCode)));
        result.put(GeneralStuff.resources.getString(R.string.key_location),  userDetails.get(GeneralStuff.resources.getString(R.string.key_location)));
        dbRef.child(GeneralStuff.resources.getString(R.string.key_profileInfo)).updateChildren(result);

        FirebaseDAO.shared.uploadProfilePicToStorage(getContext(), Uri.parse(userDetails.get(GeneralStuff.resources.getString(R.string.key_avatar))), resources.getString(R.string.key_avatar));

        Intent intent = new Intent(getActivity(), MainActivity.class);
        startActivity(intent);
    }




    private OnFailureListener mFailureListener = e -> {
        if (progressDialog.isShowing())
            progressDialog.dismiss();

        GeneralStuff.shared.showError(getContext(), e);

    };


}

