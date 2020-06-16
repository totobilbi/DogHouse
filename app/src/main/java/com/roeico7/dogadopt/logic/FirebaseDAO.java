package com.roeico7.dogadopt.logic;



import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.roeico7.dogadopt.R;
import com.roeico7.dogadopt.objects.Dog;
import com.roeico7.dogadopt.objects.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.roeico7.dogadopt.logic.GeneralStuff.resources;



public class FirebaseDAO {
//    public String uid =  FirebaseAuth.getInstance().getCurrentUser().getUid();

    //singleton: share a single object across the entire app

    private FirebaseDAO() {} // hide the constructor

    public static FirebaseDAO shared = new FirebaseDAO();
    private static String myUid = FirebaseAuth.getInstance().getCurrentUser()== null? null : FirebaseAuth.getInstance().getCurrentUser().getUid();

    public void addDog(Dog dog) {
        myUid =  FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userListRef = FirebaseDatabase.getInstance().getReference(resources.getString(R.string.key_users));
        userListRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    if (child.getKey().equals(myUid)) {
                        User value = child.getValue(User.class);
                        ArrayList<Dog> dogList = value.getDogList();
                        HashMap<String, Object> result = new HashMap<>();
                        boolean dogAdded = false;

                        if (dogList.get(0).getName() == null) {
                            dog.setDogID("1");
                            dogList.set(0, dog);
                            dogAdded = true;
                        } else if (dogList.get(1).getName() == null) {
                            dog.setDogID("2");
                            dogList.set(1, dog);
                            dogAdded = true;
                        } else if (dogList.get(2).getName() == null) {
                            dog.setDogID("3");
                            dogList.set(2, dog);
                            dogAdded = true;
                        }

                        if(dogAdded) {
                            result.put(resources.getString(R.string.key_dogList), dogList);
                            userListRef.child(myUid).updateChildren(result);
                        }

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }




    public void readUserDogs(MutableLiveData<List<Dog>> mDogs) {
        DatabaseReference userListRef = FirebaseDatabase.getInstance().getReference(resources.getString(R.string.key_users));
        userListRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    if (child.getKey().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                        User value = child.getValue(User.class);
                        ArrayList<Dog> dogList = value.getDogList();
                        mDogs.setValue(dogList);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }




//    public void readAllDogs(MutableLiveData<List<Dog>> mDogs) {
//            DatabaseReference userListRef = FirebaseDatabase.getInstance().getReference("Users");
//            userListRef.addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                    ArrayList<Dog> allDogs = new ArrayList<>();
//                    for (DataSnapshot child : dataSnapshot.getChildren()) {
//                        User value = child.getValue(User.class);
//                        ArrayList<Dog> dogList = value.getDogList();
//
//                        for (Dog dog : dogList) {
//                            if (dog.getName() != null)
//                                allDogs.add(dog);
//                        }
//
//                    }
//                    mDogs.setValue(allDogs);
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                }
//            });
//    }


    public void updateDog(Dog oldDog, Dog newDog) {
        DatabaseReference userListRef = FirebaseDatabase.getInstance().getReference(resources.getString(R.string.key_users));
        userListRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    if (child.getKey().equals(myUid)) {
                        User value = child.getValue(User.class);
                        ArrayList<Dog> dogList = value.getDogList();
                        HashMap<String, Object> result = new HashMap<>();

                        newDog.setDogID(oldDog.getDogID());
                        dogList.set(Integer.valueOf(oldDog.getDogID())-1, newDog);
                        result.put(resources.getString(R.string.key_dogList), dogList);
                        userListRef.child(myUid).updateChildren(result);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }



    public void readAllUsers(MutableLiveData<List<User>> userList) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference(resources.getString(R.string.key_users));
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<User> users = new ArrayList<>();

                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    User user = ds.getValue(User.class);
                    users.add(user);
                }
                userList.setValue(users);
                GeneralStuff.userList = users;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    public void uploadProfilePicToStorage(Context context, Uri avatarUri, String key) {
        StorageReference mStorageRef = FirebaseStorage.getInstance().getReference(resources.getString(R.string.key_uploads));
        StorageReference fileRef = mStorageRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(System.currentTimeMillis() + "." + GeneralStuff.shared.getFileExtension(context, avatarUri));
        StorageTask mUploadTask = fileRef.putFile(avatarUri)
                .addOnSuccessListener(taskSnapshot -> {
                    fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(resources.getString(R.string.key_users));
                        Query query = databaseReference.orderByChild(resources.getString(R.string.key_email)).equalTo(FirebaseAuth.getInstance().getCurrentUser().getEmail());
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                    HashMap<String, Object> hashMap = new HashMap<>();
                                    hashMap.put(key, uri.toString());
                                    ds.getRef().updateChildren(hashMap);

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    })

                            .addOnFailureListener(e -> GeneralStuff.shared.showError(context, e));
                })
                .addOnFailureListener(e -> GeneralStuff.shared.showError(context, e));
    }





}
