package com.roeico7.dogadopt.Home;


import android.app.ProgressDialog;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;



import com.roeico7.dogadopt.R;
import com.roeico7.dogadopt.Translate.Translator;
import com.roeico7.dogadopt.logic.GeneralStuff;
import com.roeico7.dogadopt.logic.RecyclerItemClickListener;
import com.roeico7.dogadopt.objects.Dog;
import com.roeico7.dogadopt.objects.User;

import com.google.firebase.auth.FirebaseAuth;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.roeico7.dogadopt.logic.GeneralStuff.resources;


public class MainFragment extends Fragment {


    private RecyclerView rvDogs;
    private MainViewModel mainViewModel;
    public static ProgressDialog progressDialog;
    private Fragment mFragment;
    public static List<User> userList;
    public static List<Dog> dogList;
    public static User currentUser;
    private int translationCounter;

    @Override
    public void onPause() {
        super.onPause();
        if(progressDialog.isShowing())
            progressDialog.dismiss();
    }

    public View onCreateView(@NonNull final LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mainViewModel =
                ViewModelProviders.of(this).get(MainViewModel.class);
        View root = inflater.inflate(R.layout.main_fragment, container, false);

        if(FirebaseAuth.getInstance().getCurrentUser()!=null) {
            progressDialog = new ProgressDialog(getContext());
            GeneralStuff.shared.executeProgress(progressDialog, resources.getString(R.string.searching_dogs), resources.getString(R.string.please_wait));
        }

        mFragment = this;
        rvDogs = root.findViewById(R.id.recycle_dogs);


        mainViewModel.getUsers().observe(getViewLifecycleOwner(), new Observer<List<User>>() {
            @Override
            public void onChanged(List<User> users) {
                dogList = new ArrayList<>();
                userList = users;
                GeneralStuff.userList = users;
                String currentUserUID = FirebaseAuth.getInstance().getCurrentUser().getUid();


                for (User user : userList) {
                    if (user.getUserID().equals(currentUserUID)) {
                        currentUser = user;
                    }

                    for (Dog dog : user.getDogList()) {
                        if (dog.getName() != null)
                            dogList.add(dog);
                    }
                }

                if (currentUser == null)
                    FirebaseAuth.getInstance().signOut();
                else {
                    reArrangeDogsByDistance();

                    //translation adjustment
                    translationCounter = 0;
                    for (int i = 0; i < dogList.size(); i++) {
                        Dog dog = dogList.get(i);
                        new Translator(dog.getName(), new TranslateListener(i));
                    }


                }
            }
        });


        rvDogs.addOnItemTouchListener(
                new RecyclerItemClickListener(getContext(), rvDogs ,new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        String ownerID = ((TextView) rvDogs.getLayoutManager().findViewByPosition(position).findViewById(R.id.tv_owner)).getText().toString().substring(7);

                        if(!ownerID.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                            MainFragmentDirections.ActionNavigationHomeToDisplayDog action = MainFragmentDirections.actionNavigationHomeToDisplayDog(dogList.get(position));
                            NavHostFragment.findNavController(mFragment).navigate(action);
                        }








                    }

                    @Override public void onLongItemClick(View view, int position) {
                        // do whatever
                    }
                })
        );


        return root;
    }



    private class TranslateListener implements Translator.Listener {

        private final int index;

        public TranslateListener(int index) {
            this.index = index;
        }

        @Override
        public void textTranslated(String translatedText) {
            dogList.get(index).setName(translatedText);
            translationCounter++;


            if (translationCounter == dogList.size()){
                initAdapter();
            }

        }
    }




    private void reArrangeDogsByDistance() {
        Collections.sort(dogList, (o1, o2) -> GeneralStuff.shared.getCurrentUserDistanceFromDog(o1).compareTo(GeneralStuff.shared.getCurrentUserDistanceFromDog(o2)));
    }


    private void initAdapter() {
        DogAdapter adapter = new DogAdapter(dogList, getLayoutInflater());
        rvDogs.setLayoutManager(new LinearLayoutManager(getContext()));
        rvDogs.setAdapter(adapter);

        if(progressDialog.isShowing())
            progressDialog.dismiss();
    }


}