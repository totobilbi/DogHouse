package com.roeico7.dogadopt.Entry;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.roeico7.dogadopt.R;
import com.roeico7.dogadopt.Translate.Translate;
import com.roeico7.dogadopt.logic.GeneralStuff;

import java.util.regex.Pattern;

import static com.roeico7.dogadopt.logic.GeneralStuff.resources;


public class LoginAlertDialog extends AppCompatDialogFragment {


    private EditText edit_email, edit_password;
    private ProgressDialog progressDialog;



    private OnSuccessListener mSuccessListener = (OnSuccessListener<AuthResult>) authResult -> {
        if(progressDialog.isShowing())
            progressDialog.dismiss();

        Intent intent = new Intent(getActivity(), MainActivity.class);
        startActivity(intent);
        getActivity().finish();
    };

    private OnFailureListener mFailureListener = e -> {
        if(progressDialog.isShowing())
            progressDialog.dismiss();

        GeneralStuff.shared.showError(getContext(), e);

    };



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.login_alert_dialog, container, false);

        int widthPixels = (int) (getResources().getDisplayMetrics().widthPixels * 0.8);
        int heightPixels = (int) (getResources().getDisplayMetrics().heightPixels * 0.6);

        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(widthPixels, heightPixels);
        v.setLayoutParams(layoutParams);

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        edit_email = view.findViewById(R.id.edit_email);
        edit_password = view.findViewById(R.id.edit_password);
        view.findViewById(R.id.login).setOnClickListener(v->login() );


        if(Translate.shared.checkDisplayLanguage()) {
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 15, 0);
            edit_email.setLayoutParams(lp);
            edit_password.setLayoutParams(lp);

        }

    }



    private void login() {
        String email = getEmail();
        String password = getPassword();

        if(email == null || password == null)
            return;


        progressDialog = new ProgressDialog(getContext());
        GeneralStuff.shared.executeProgress(progressDialog, GeneralStuff.resources.getString(R.string.logging_in), GeneralStuff.resources.getString(R.string.please_wait));

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).
                addOnSuccessListener(mSuccessListener).
                addOnFailureListener(mFailureListener);
    }




    private String getEmail() {
        String email = edit_email.getText().toString();

        Pattern emailAdDressRegex = Patterns.EMAIL_ADDRESS;
        boolean isEmailValid = emailAdDressRegex.matcher(email).matches();

        if(!isEmailValid) {
            edit_email.setError(resources.getString(R.string.invalid_email));
            return null;
        }

        return email;
    }




    private String getPassword() {
        String password = edit_password.getText().toString();

        if(password.length()<6) {
            edit_password.setError(resources.getString(R.string.weak_password));
            return null;
        }

        return password;
    }
}