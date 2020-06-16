package com.roeico7.dogadopt.Notifications;

import androidx.annotation.NonNull;

import com.roeico7.dogadopt.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;

import static com.roeico7.dogadopt.logic.GeneralStuff.resources;


public class FirebaseService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String tokenRefresh = FirebaseInstanceId.getInstance().getToken();

        if(user != null) {
            updateToken(tokenRefresh);
        }
    }


    private void updateToken(String tokenRefresh) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(resources.getString(R.string.key_tokens));
        Token token = new Token(tokenRefresh);
        ref.child(user.getUid()).setValue(token);
    }
}
