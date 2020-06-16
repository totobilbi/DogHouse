package com.roeico7.dogadopt.Entry;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.roeico7.dogadopt.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.roeico7.dogadopt.Home.MainFragment;
import com.roeico7.dogadopt.Home.MainFragmentDirections;
import com.roeico7.dogadopt.Notifications.Token;
import com.roeico7.dogadopt.logic.GeneralStuff;

public class MainActivity extends AppCompatActivity {

    private String mUID;

    private FirebaseAuth.AuthStateListener mAuthListener = firebaseAuth -> {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else {

            mUID = user.getUid();

            SharedPreferences sp = getSharedPreferences(GeneralStuff.resources.getString(R.string.key_spUser), MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(GeneralStuff.resources.getString(R.string.key_current_userID), mUID);
            editor.apply();


            if(getIntent().getStringExtra(GeneralStuff.resources.getString(R.string.key_hisUID)) != null) {
                MainFragmentDirections.ActionNavigationHomeToChatFrag action = MainFragmentDirections.actionNavigationHomeToChatFrag(getIntent().getStringExtra(GeneralStuff.resources.getString(R.string.key_hisUID)));
                NavHostFragment.findNavController(getSupportFragmentManager().getFragments().get(0)).navigate(action);
                MainFragment.progressDialog.dismiss();
            } else {

            }


        }
    };



    @Override
    protected void onPause() {
        super.onPause();
        FirebaseAuth.getInstance().removeAuthStateListener(mAuthListener);
    }


    @Override
    protected void onResume() {
        super.onResume();
        FirebaseAuth.getInstance().addAuthStateListener(mAuthListener);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        GeneralStuff.resources = getApplicationContext().getResources();

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.

        //Titles for the appbar
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.add_dog, R.id.navigation_chat, R.id.navigation_profile)
                .build();


        //Setup nav controller
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);



        // navController : use the b_nav for navigation
        NavigationUI.setupWithNavController(navView, navController);

        if(FirebaseAuth.getInstance().getCurrentUser()!=null)
            updateToken(FirebaseInstanceId.getInstance().getToken());
    }


    public void updateToken(String token) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(GeneralStuff.resources.getString(R.string.key_tokens));
        Token mToken = new Token(token);
        ref.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(mToken);
    }


}
