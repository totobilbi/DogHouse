package com.roeico7.dogadopt.Entry;


import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.roeico7.dogadopt.Profile.InfoEdit;
import com.roeico7.dogadopt.R;
import com.roeico7.dogadopt.Translate.Translate;
import com.roeico7.dogadopt.logic.GeneralStuff;
import com.roeico7.dogadopt.objects.User;

import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import static com.roeico7.dogadopt.logic.GeneralStuff.resources;
import static com.roeico7.dogadopt.logic.GeneralStuff.userList;

/**
 * A simple {@link Fragment} subclass.
 */
public class PhoneVerification extends AppCompatDialogFragment {
    private String phone;
    private EditText edit_phone;
    private Button btn_verify;
    private String mVerificationId;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    private TextView tv_title, tv_subtitle, tv_timer;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private HashMap<String, String> userDetails = new HashMap<>();
    private Fragment mFragment;


    public PhoneVerification(HashMap<String, String> userDetails) {
        this.userDetails = userDetails;

        if (userDetails.get(resources.getString(R.string.key_phone)) != null) {
            phone = userDetails.get(resources.getString(R.string.key_phone));
        }
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_phone_verification, container, false);

        int widthPixels = (int) (getResources().getDisplayMetrics().widthPixels * 0.8);
        int heightPixels = (int) (getResources().getDisplayMetrics().heightPixels * 0.8);

        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(widthPixels, heightPixels);
        v.setLayoutParams(layoutParams);

        btn_verify = v.findViewById(R.id.btn_verify);
        edit_phone = v.findViewById(R.id.edit_phone);
        tv_title = v.findViewById(R.id.tv_title);
        tv_subtitle = v.findViewById(R.id.tv_subtitle);
        tv_timer = v.findViewById(R.id.tv_timer);
        progressBar = v.findViewById(R.id.progressBar);
        mFragment = this;

        mAuth = FirebaseAuth.getInstance();
        mAuth.setLanguageCode(Translate.shared.checkDisplayLanguage() ? "iw" : "en");

        btn_verify.setOnClickListener(b -> {
                phone = getPhone();

                if (phone == null)
                    return;

                tv_subtitle.setText(resources.getString(R.string.ui_waiting_verify));
                tv_timer.setVisibility(View.VISIBLE);
                tv_timer.setText("2:00");
                progressBar.setVisibility(View.VISIBLE);
                edit_phone.setEnabled(false);
                updateTimer();
                sendVerificationCode(phone.substring(1));
                btn_verify.setVisibility(View.INVISIBLE);

        });

        if (phone != null) {
            edit_phone.setText(phone);
            btn_verify.performClick();
        }


        return v;
    }



    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return new Dialog(getActivity(), getTheme()){
            @Override
            public void onBackPressed() {
                ProgressDialog progressDialog = new ProgressDialog(getContext());
                GeneralStuff.shared.executeAlert(progressDialog,
                        resources.getString(R.string.ui_cancel_register),
                        resources.getString(R.string.ui_cancel_register_text),
                        false,
                        () -> {
                            getActivity().getSupportFragmentManager().beginTransaction().remove(mFragment).commit();
                            getActivity().getSupportFragmentManager().popBackStack();
                        });
            }
        };
    }


    private void updateTimer() {
        final int[] time = {1, 60};

        Handler handler = new Handler();
        handler.postDelayed(new Runnable(){
            public void run() {
                if (time[0] >= 0) {
                    time[1] -= 1;

                    if(time[1]>=0) {
                        if (time[1] < 10) {
                            tv_timer.setText(time[0] + ":0" + time[1]);
                        } else {
                            tv_timer.setText(time[0] + ":" + time[1]);
                        }
                    } else {
                        time[0] -= 1;
                        time[1] = 60;
                    }

                    handler.postDelayed(this, 1000);
                } else {
                    //verification failed

                    ProgressDialog progressDialog = new ProgressDialog(getContext());
                    GeneralStuff.shared.executeAlert(progressDialog,
                            resources.getString(R.string.ui_verification_failed),
                            resources.getString(R.string.ui_verification_failed_text),
                            false,
                            () -> {
                                getActivity().getSupportFragmentManager().beginTransaction().remove(mFragment).commit();
                                PhoneVerification cdf = new PhoneVerification(userDetails);
                                cdf.setCancelable(false);
                                cdf.show(getActivity().getSupportFragmentManager(), "phoneVerify");
                            },
                                () -> {
                                    getActivity().getSupportFragmentManager().beginTransaction().remove(mFragment).commit();
                                    getActivity().getSupportFragmentManager().popBackStack();
                            });

                }
            }
        }, 1000);
    }


    private String getPhone() {
        String[] possiblePrefix = {"050", "051", "052", "053", "054", "055", "056", "057", "058", "059"};
        if (!edit_phone.getText().toString().trim().equals("")) {
            String phone = edit_phone.getText().toString();

            if (phone.length() != 10) {
                edit_phone.setError(resources.getString(R.string.invalid_phone_length));
                return null;
            }


            if (Arrays.asList(possiblePrefix).contains(phone.substring(0, 3))) {
                for (User user : userList)
                    if (user.getPhone().equals(phone)) {
                        edit_phone.setError(resources.getString(R.string.phone_used));
                        return null;
                    }

                return phone;
            } else {
                edit_phone.setError(resources.getString(R.string.invalid_phone_prefix));
                return null;
            }

        }
        edit_phone.setError(resources.getString(R.string.empty_field));
        return null;
    }


    private void sendVerificationCode(String mobile) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+972" + mobile,
                120,
                TimeUnit.SECONDS,
                TaskExecutors.MAIN_THREAD,
                mCallbacks);
    }


    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            //Getting the code sent by SMS
            String code = phoneAuthCredential.getSmsCode();

            //sometime the code is not detected automatically
            //in this case the code will be null
            //so user has to manually enter the code
            if (code != null) {
                edit_phone.setText(code);
                verifiedCode();
            }
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            mVerificationId = s;
            mResendToken = forceResendingToken;
        }
    };


    private void verifiedCode() {
        getActivity().getSupportFragmentManager().beginTransaction().remove(mFragment).commit();

        if(userDetails.get(resources.getString(R.string.key_phone)) == null) {
            userDetails.put(resources.getString(R.string.key_phone), phone);
        }

        InfoEdit cdf = new InfoEdit(userDetails);
        cdf.setCancelable(false);
        cdf.show(getActivity().getSupportFragmentManager(), "registerInfo");
    }


}
