package com.roeico7.dogadopt.logic;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Location;
import android.net.Uri;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.roeico7.dogadopt.Home.MainFragment;
import com.roeico7.dogadopt.R;
import com.roeico7.dogadopt.objects.Dog;
import com.roeico7.dogadopt.objects.User;

import java.util.List;

public class GeneralStuff {
    public static Resources resources;
    public static List<User> userList;


    private GeneralStuff() {} // hide the constructor

    public static GeneralStuff shared = new GeneralStuff();


    public void showError(Context context, Exception e) {
        new AlertDialog.Builder(context).setTitle(resources.getString(R.string.error_occured)).setMessage(e.getLocalizedMessage()).setPositiveButton(resources.getString(R.string.dismiss), (dialog, which) -> {
        }).show();
    }


    public void executeProgress(ProgressDialog progressDialog, String title, String message) {
        progressDialog.show();

        progressDialog.setCancelable(false);
        progressDialog.setContentView(R.layout.progress_dialog);

        TextView tv_title = progressDialog.getWindow().findViewById(R.id.title);
        TextView tv_message = progressDialog.getWindow().findViewById(R.id.message);

        tv_title.setText(title);
        tv_message.setText(message);
    }



    public void executeAlert(ProgressDialog progressDialog, String title, String message, boolean cancelNegative, Runnable runnable) {
        progressDialog.show();

        progressDialog.setCancelable(false);
        progressDialog.setContentView(R.layout.alert_dialog);

        TextView tv_title = progressDialog.getWindow().findViewById(R.id.title);
        TextView tv_message = progressDialog.getWindow().findViewById(R.id.message);

        Button btn_plus = progressDialog.getWindow().findViewById(R.id.btn_plus);
        Button btn_minus = progressDialog.getWindow().findViewById(R.id.btn_minus);

        tv_title.setText(title);
        tv_message.setText(message);

        if(cancelNegative) {
            btn_minus.setVisibility(View.GONE);
        } else {
            btn_minus.setOnClickListener(v-> {
                progressDialog.dismiss();
            });
        }

        btn_plus.setOnClickListener(v-> {
            runnable.run();
            progressDialog.dismiss();
        });
    }


    public void executeAlert(ProgressDialog progressDialog, String title, String message, boolean cancelNegative, Runnable positiveAction, Runnable negativeAction) {
        executeAlert(progressDialog, title, message, cancelNegative, positiveAction);
        Button btn_minus = progressDialog.getWindow().findViewById(R.id.btn_minus);
        btn_minus.setOnClickListener(v-> {
            negativeAction.run();
            progressDialog.dismiss();
        });
    }




    public String getFileExtension(Context context, Uri uri) {
        ContentResolver contentResolver = context.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }


    public  void openFileChooser(Fragment fragment) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        fragment.startActivityForResult(intent, 1);
    }


    public Location getUserLocation(User user) {
        Location location = new Location("UserLocation");

        String locationString = user.getProfileInfo().get("location");
        int indexOfPsik = locationString.indexOf(',');

        location.setLatitude(Double.parseDouble(locationString.substring(0,indexOfPsik)));
        location.setLongitude(Double.parseDouble(locationString.substring(indexOfPsik+1)));

        return location;
    }



    public Integer getCurrentUserDistanceFromDog(Dog dog) {
        Location location = getUserLocation(MainFragment.currentUser);
        Location ownerLocation = new Location("dogOwnerLocation");
        float[] result = new float[1];

        for(User user: MainFragment.userList) {
            if(user.getUserID().equals(dog.getDogOwner())) {
                ownerLocation = getUserLocation(user);
                break;
            }
        }

        Location.distanceBetween(location.getLatitude(), location.getLongitude(), ownerLocation.getLatitude(), ownerLocation.getLongitude(), result);
        return (int) (result[0]/1000);

    }



    public User getDogOwner(Dog givenDog) {
        for (User user: userList) {
            for (Dog dog: user.getDogList()) {
                if(dog.equals(givenDog))
                    return user;
            }
        }
        return null;
    }


    public User getUserById(String uid) {
        for (User user: userList) {
                if(user.getUserID().equals(uid))
                    return user;
        }
        return null;
    }

}

