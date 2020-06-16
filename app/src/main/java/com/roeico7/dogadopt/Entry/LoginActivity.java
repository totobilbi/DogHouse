package com.roeico7.dogadopt.Entry;


import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.roeico7.dogadopt.R;
import com.roeico7.dogadopt.logic.FirebaseDAO;
import com.roeico7.dogadopt.logic.GeneralStuff;
import com.roeico7.dogadopt.objects.User;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;


public class LoginActivity extends AppCompatActivity {

    private EditText edit_email, edit_password, edit_username, edit_phone;
    private TextView btn_register;
    private FloatingActionButton btn_google, btn_dog, btn_facebook;
    private MutableLiveData<List<User>> userList = new MutableLiveData<>();
    private GoogleSignInClient mGoogleSignInClient;
    private static int RC_SIGN_IN = 100;
    private FirebaseAuth mAuth;
    private CallbackManager mCallbackManager;
    private LoginButton loginButton;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        userList.observe(() -> getLifecycle(), users -> {
            if(progressDialog!=null) {
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                    btn_register.performClick();
                }
            }
        });

        GeneralStuff.resources = getApplicationContext().getResources();

        edit_email = findViewById(R.id.edit_email);
        edit_password = findViewById(R.id.edit_password);
        edit_username = findViewById(R.id.edit_username);
        edit_phone = findViewById(R.id.edit_phone);
        btn_register = findViewById(R.id.login);
        btn_google = findViewById(R.id.btn_google);
        btn_facebook = findViewById(R.id.btn_facebook);
        btn_dog = findViewById(R.id.btn_dog);

        //testing inputs
//        edit_email.setText("ladsalal@gmail.com");
//        edit_password.setText("ldsaalal@gmail.com");
//        edit_username.setText("lalal@gmail.com");
//        edit_phone.setText("0509485763");


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        //google sign in
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mAuth = FirebaseAuth.getInstance();

        //facebook sign in
        faceBookSignIn();

        //read all users
        FirebaseDAO.shared.readAllUsers(userList);


        btn_register.setOnClickListener(v -> register());

        btn_dog.setOnClickListener(v -> openLoginForm());
        btn_google.setOnClickListener(v -> googleSignIn());
        btn_facebook.setOnClickListener(v -> loginButton.performClick());

    }

    private void faceBookSignIn() {
        mCallbackManager = CallbackManager.Factory.create();
        loginButton = findViewById(R.id.login_button);
        loginButton.setReadPermissions("email");
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {
            }
        });
    }


    private void googleSignIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately

                // ...
            }
        } else {
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }


    private void handleFacebookAccessToken(AccessToken token) {
        progressDialog = new ProgressDialog(this);
        GeneralStuff.shared.executeProgress(progressDialog, GeneralStuff.resources.getString(R.string.logging_in), GeneralStuff.resources.getString(R.string.please_wait));
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        if(progressDialog.isShowing())
                            progressDialog.dismiss();
                        FirebaseUser fbUser = mAuth.getCurrentUser();
                        User user = GeneralStuff.shared.getUserById(fbUser.getUid());
                        if (user == null) {
                            HashMap<String, String> userDetails = new HashMap<>();
                            userDetails.put(GeneralStuff.resources.getString(R.string.key_email), fbUser.getEmail());
                            userDetails.put(GeneralStuff.resources.getString(R.string.key_username), fbUser.getDisplayName());
                            getSupportFragmentManager().beginTransaction().replace(R.id.container, new RegisterProcess(userDetails)).addToBackStack(null).commit();
                        } else {
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();

                        }

                    } else {
                        // If sign in fails, display a message to the user.

                    }
                });
    }


    private void firebaseAuthWithGoogle(String idToken) {
        progressDialog = new ProgressDialog(this);
        GeneralStuff.shared.executeProgress(progressDialog, GeneralStuff.resources.getString(R.string.logging_in), GeneralStuff.resources.getString(R.string.please_wait));
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            if(progressDialog.isShowing())
                                progressDialog.dismiss();
                            FirebaseUser fbUser = mAuth.getCurrentUser();
                            GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
                            if (acct != null) {
                                User user = GeneralStuff.shared.getUserById(fbUser.getUid());
                                if (user == null) {
                                    HashMap<String, String> userDetails = new HashMap<>();
                                    userDetails.put(GeneralStuff.resources.getString(R.string.key_email), acct.getEmail());
                                    userDetails.put(GeneralStuff.resources.getString(R.string.key_username), acct.getDisplayName());
                                    getSupportFragmentManager().beginTransaction().replace(R.id.container, new RegisterProcess(userDetails)).addToBackStack(null).commit();
                                } else {
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                        }

                        // ...
                    }
                });
    }


    private void openLoginForm() {
        LoginAlertDialog cdf = new LoginAlertDialog();
        cdf.show(getSupportFragmentManager(), "Custom Dialog");
    }

    private void register() {
        if(userList.getValue()!=null){
            String email = getEmail();
            String password = getPassword();
            String phone = getPhone();
            String username = getUsername();

            if (email == null || password == null || phone == null || username == null)
                return;


            HashMap<String, String> userDetails = new HashMap<>();
            userDetails.put(GeneralStuff.resources.getString(R.string.key_email), email);
            userDetails.put(GeneralStuff.resources.getString(R.string.key_password), password);
            userDetails.put(GeneralStuff.resources.getString(R.string.key_phone), phone);
            userDetails.put(GeneralStuff.resources.getString(R.string.key_username), username);

            getSupportFragmentManager().beginTransaction().replace(R.id.container, new RegisterProcess(userDetails), "registerProcess").addToBackStack(null).commit();
        } else {
            progressDialog = new ProgressDialog(this);
            GeneralStuff.shared.executeProgress(progressDialog, GeneralStuff.resources.getString(R.string.logging_in), GeneralStuff.resources.getString(R.string.please_wait));
        }
    }


    private String getUsername() {
        if (!edit_username.getText().toString().trim().equals("")) {
            String username = edit_username.getText().toString();

            for (User user : userList.getValue())
                if (user.getUsername().equals(username)) {
                    edit_username.setError(GeneralStuff.resources.getString(R.string.username_used));
                    return null;
                }


            return username;
        }
        edit_username.setError(GeneralStuff.resources.getString(R.string.empty_field));
        return null;
    }


    private String getPhone() {
        String[] possiblePrefix = {"050", "051", "052", "053", "054", "055", "056", "057", "058", "059"};
        if (!edit_phone.getText().toString().trim().equals("")) {
            String phone = edit_phone.getText().toString();

            if (phone.length() != 10) {
                edit_phone.setError(GeneralStuff.resources.getString(R.string.invalid_phone_length));
                return null;
            }

            if (Arrays.asList(possiblePrefix).contains(phone.substring(0, 3))) {

                for (User user : userList.getValue())
                    if (user.getPhone().equals(phone)) {
                        edit_phone.setError(GeneralStuff.resources.getString(R.string.phone_used));
                        return null;
                    }

                return phone;
            } else {
                edit_phone.setError(GeneralStuff.resources.getString(R.string.invalid_phone_prefix));
                return null;
            }
        }
        edit_phone.setError(GeneralStuff.resources.getString(R.string.empty_field));
        return null;
    }


    private String getEmail() {
        if (!edit_email.getText().toString().trim().equals("")) {
            String email = edit_email.getText().toString();

            Pattern emailAdDressRegex = Patterns.EMAIL_ADDRESS;
            boolean isEmailValid = emailAdDressRegex.matcher(email).matches();

            if (!isEmailValid) {
                edit_email.setError(GeneralStuff.resources.getString(R.string.invalid_email));
                return null;
            }

            for (User user : userList.getValue())
                if (user.getEmail().equals(email)) {
                    edit_email.setError(GeneralStuff.resources.getString(R.string.email_used));
                    return null;
                }

            return email;
        }
        edit_email.setError(GeneralStuff.resources.getString(R.string.empty_field));
        return null;
    }


    private String getPassword() {
        if (!edit_password.getText().toString().trim().equals("")) {
            String password = edit_password.getText().toString();

            if (password.length() < 6) {
                edit_password.setError(GeneralStuff.resources.getString(R.string.weak_password));
                return null;
            }

            return password;
        }
        edit_password.setError(GeneralStuff.resources.getString(R.string.empty_field));
        return null;
    }

}
