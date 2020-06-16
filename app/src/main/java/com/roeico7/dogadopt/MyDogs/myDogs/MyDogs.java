package com.roeico7.dogadopt.MyDogs.myDogs;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.roeico7.dogadopt.R;
import com.roeico7.dogadopt.Translate.Translator;
import com.roeico7.dogadopt.logic.FirebaseDAO;
import com.roeico7.dogadopt.logic.GeneralStuff;
import com.roeico7.dogadopt.objects.Dog;
import com.squareup.picasso.Picasso;

import java.util.List;

import static com.roeico7.dogadopt.logic.GeneralStuff.resources;


public class MyDogs extends Fragment {

    private MyDogsViewModel mViewModel;
    private TextView dog1_name, dog2_name, dog3_name;
    private ConstraintLayout details_dog1, details_dog2, details_dog3;
    private ImageView iv_add_dog1, iv_add_dog2, iv_add_dog3;
    private ImageView dog1_image, dog2_image, dog3_image;
    private ImageView trash_dog1, trash_dog2, trash_dog3;
    private Task mDogRemoveTask;
    private StorageReference fileRef;
    private List<Dog> dogList;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mViewModel = ViewModelProviders.of(this).get(MyDogsViewModel.class);
        View view = inflater.inflate(R.layout.my_dogs_fragment, container, false);

        iv_add_dog1 = view.findViewById(R.id.iv_add_dog1);
        iv_add_dog2 = view.findViewById(R.id.iv_add_dog2);
        iv_add_dog3 = view.findViewById(R.id.iv_add_dog3);

        iv_add_dog1.setOnClickListener(v-> moveToAddDog());
        iv_add_dog2.setOnClickListener(v-> moveToAddDog());
        iv_add_dog3.setOnClickListener(v-> moveToAddDog());



        details_dog1 = view.findViewById(R.id.details_dog1);
        details_dog2 = view.findViewById(R.id.details_dog2);
        details_dog3 = view.findViewById(R.id.details_dog3);

        dog1_name = view.findViewById(R.id.dog1_name);
        dog2_name = view.findViewById(R.id.dog2_name);
        dog3_name = view.findViewById(R.id.dog3_name);


        dog1_image = view.findViewById(R.id.dog1_image);
        dog2_image = view.findViewById(R.id.dog2_image);
        dog3_image = view.findViewById(R.id.dog3_image);


        trash_dog1 = view.findViewById(R.id.trash_dog1);
        trash_dog2 = view.findViewById(R.id.trash_dog2);
        trash_dog3 = view.findViewById(R.id.trash_dog3);



        // remove dog
        trash_dog1.setOnClickListener(v -> removeDog(0));
        trash_dog2.setOnClickListener(v -> removeDog(1));
        trash_dog3.setOnClickListener(v -> removeDog(2));


        // edit dog
        details_dog1.setOnClickListener(v -> moveToEditDog(mViewModel.getDogs().getValue().get(0)));
        details_dog2.setOnClickListener(v -> moveToEditDog(mViewModel.getDogs().getValue().get(1)));
        details_dog3.setOnClickListener(v -> moveToEditDog(mViewModel.getDogs().getValue().get(2)));



        loadDogs();

        return view;
    }




    private void moveToAddDog() {
        NavHostFragment.findNavController(this).navigate(R.id.action_navigation_myDogs_to_add_dog);
    }



    private void moveToEditDog(Dog dog) {
        MyDogsDirections.ActionNavigationMyDogsToEditDog action = MyDogsDirections.actionNavigationMyDogsToEditDog(dog);
        NavHostFragment.findNavController(this).navigate(action);
    }




    private void loadDogs() {
        mViewModel.getDogs().observe(getViewLifecycleOwner(), new Observer<List<Dog>>() {
            @Override
            public void onChanged(List<Dog> dogs) {
                dogList= dogs;
                if (dogs.get(0).getName() == null) {
                    iv_add_dog1.setVisibility(View.VISIBLE);
                } else {
                    details_dog1.setVisibility(View.VISIBLE);
                    trash_dog1.setVisibility(View.VISIBLE);
                    new Translator(dogs.get(0).getName(), new TranslateListener(0));
                    Picasso.get().load(dogs.get(0).getPic()).into(dog1_image);
                }


                if (dogs.get(1).getName() == null) {
                    iv_add_dog2.setVisibility(View.VISIBLE);
                } else {
                    details_dog2.setVisibility(View.VISIBLE);
                    trash_dog2.setVisibility(View.VISIBLE);
                    dog2_name.setText(dogs.get(1).getName());
                    new Translator(dogs.get(1).getName(), new TranslateListener(1));
                    Picasso.get().load(dogs.get(1).getPic()).into(dog2_image);
                }


                if (dogs.get(2).getName() == null) {
                    iv_add_dog3.setVisibility(View.VISIBLE);
                } else {
                    details_dog3.setVisibility(View.VISIBLE);
                    trash_dog3.setVisibility(View.VISIBLE);
                    dog3_name.setText(dogs.get(2).getName());
                    new Translator(dogs.get(2).getName(), new TranslateListener(2));
                    Picasso.get().load(dogs.get(2).getPic()).into(dog3_image);
                }
            }
        });
    }


    private void removeDog(int num) {
        TextView dogName = null;

        switch (num) {
            case 0:
                dogName = dog1_name;
                break;

            case 1:
                dogName = dog2_name;
                break;

            case 2:
                dogName = dog3_name;
                break;
        }

        Dog oldDog = mViewModel.getDogs().getValue().get(num);



        ProgressDialog progressDialog = new ProgressDialog(getContext());
        GeneralStuff.shared.executeAlert(progressDialog,
                resources.getString(R.string.dog_removal),
                resources.getString(R.string.delete_dog) +" "+ dogName.getText().toString() + "?",
                false,
                () -> {
                    FirebaseDAO.shared.updateDog(oldDog, new Dog(oldDog.getDogOwner(),"dog", null, null, null, null, null));
                    fileRef = FirebaseStorage.getInstance().getReferenceFromUrl(oldDog.getPic());
                    mDogRemoveTask = fileRef.delete()
                            .addOnSuccessListener(removeSuccessListener)
                            .addOnFailureListener(removeFailureListener);
                });

    }



    private OnSuccessListener removeSuccessListener = (OnSuccessListener<Void>) aVoid -> NavHostFragment.findNavController(this).navigate(R.id.action_navigation_myDogs_self);
    private OnFailureListener removeFailureListener = e -> GeneralStuff.shared.showError(getContext(), e);




    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(MyDogsViewModel.class);
        // TODO: Use the ViewModel
    }

    private class TranslateListener implements Translator.Listener {

        private final int index;

        public TranslateListener(int index) {
            this.index = index;
        }

        @Override
        public void textTranslated(String translatedText) {
            dogList.get(index).setName(translatedText);
            switch (index) {
                case 0:
                    dog1_name.setText(dogList.get(index).getName());
                    break;
                case 1:
                    dog2_name.setText(dogList.get(index).getName());
                    break;
                case 2:
                    dog3_name.setText(dogList.get(index).getName());
                    break;
            }

        }
    }
}
