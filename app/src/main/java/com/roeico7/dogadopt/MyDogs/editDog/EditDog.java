package com.roeico7.dogadopt.MyDogs.editDog;



import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.roeico7.dogadopt.R;
import com.roeico7.dogadopt.Translate.Translate;
import com.roeico7.dogadopt.logic.GeneralStuff;
import com.roeico7.dogadopt.objects.Dog;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;

import in.galaxyofandroid.spinerdialog.SpinnerDialog;

import static android.app.Activity.RESULT_OK;
import static com.roeico7.dogadopt.logic.GeneralStuff.resources;

public class EditDog extends Fragment {
    private Uri dogImageUri;
    private ImageView dogImage, dog_type_icon;
    private EditText dog_name;
    private TextView dog_type, gender_type, dog_age;
    private Button add_dog;
    private static final int PICK_IMAGE_REQUEST = 1;
    private StorageReference mStorageRef;
    private StorageTask mUploadTask;
    private StorageReference fileRef;
    private NumberPicker np;
    private ProgressDialog progressDialog;
    private ToggleButton gender_button;
    private Task mDogRemoveTask;
    private Dog oldDogInfo;


    private SpinnerDialog spinnerDialog;
    private String[] dogRace;
    private ImageView iv_title;






    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.add_dog_fragment, container, false);
        return  view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        dogImage = view.findViewById(R.id.dog_pic);
        dog_name = view.findViewById(R.id.dog_name);
        dog_type_icon = view.findViewById(R.id.dog_type_icon);
        dog_type = view.findViewById(R.id.dog_type);
        dog_age = view.findViewById(R.id.dog_age);
        add_dog = view.findViewById(R.id.btn_add_dog);
        gender_type = view.findViewById(R.id.gender_type);
        iv_title = view.findViewById(R.id.iv_title);
        gender_button = view.findViewById(R.id.gender_button);
        oldDogInfo = EditDogArgs.fromBundle(getArguments()).getDogToEdit();

        if(Translate.shared.checkDisplayLanguage()) {
            iv_title.setImageResource(R.drawable.basic_information2);
            dogRace = getResources().getStringArray(R.array.dogs_type_he);
        } else {
            dogRace = getResources().getStringArray(R.array.dogs_type_en);
        }


        // dog age number picker
        np = view.findViewById(R.id.numberPicker);
        np.setMinValue(1);
        np.setMaxValue(15);


        // place holder for dog image
        dogImage.setImageResource(R.drawable.dog_image_placeholder);


        // ref to upload the dog profile image
        mStorageRef = FirebaseStorage.getInstance().getReference(resources.getString(R.string.key_uploads));


        // dog gender
        checkGender();


        // old dog info displayed on screen
        applyCurrentInfo();


        // dog race spinner
        dogRaceSpinner();


        // choose image from device
        dogImage.setOnClickListener(v -> GeneralStuff.shared.openFileChooser(this));

        // upload dog to firebase
        add_dog.setOnClickListener(v -> {
            if(dog_name.getText().toString().equals("")) {
                dog_name.setError(resources.getString(R.string.empty_field));

            } else {
                Dog newDog;
                String race = dog_type.getText().toString();
                race = race.replaceAll("\n", " ");
                if(dogImageUri==null) {
                    newDog = new Dog(FirebaseAuth.getInstance().getCurrentUser().getUid(), getString(R.string.key_dog1), dog_name.getText().toString(), oldDogInfo.getPic(), race, gender_type.getText().toString(), np.getValue() + "", oldDogInfo.getDescription(), oldDogInfo.getOptions());
                } else {
                    newDog = new Dog(FirebaseAuth.getInstance().getCurrentUser().getUid(), getString(R.string.key_dog1), dog_name.getText().toString(), ""+dogImageUri, race, gender_type.getText().toString(), np.getValue() + "", oldDogInfo.getDescription(), oldDogInfo.getOptions());
                }

                EditDogDirections.ActionEditDogToEditDogInfo2 action = EditDogDirections.actionEditDogToEditDogInfo2(newDog, oldDogInfo);
                NavHostFragment.findNavController(this).navigate(action);
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            dogImageUri = data.getData();

            Picasso.get().load(dogImageUri).into(dogImage);
            dogImage.setBackgroundResource(R.drawable.add_dog_pic_border);
        }
    }








    private void applyCurrentInfo() {
        dog_name.setText(oldDogInfo.getName());
        np.setValue(Integer.valueOf(oldDogInfo.getAge()));
        String race = Translate.shared.translateRaceType(oldDogInfo.getType());
        dog_type.setText(race.replaceAll("[\\s']", "\n"));
        dogImage.setImageResource(R.drawable.dog_image_placeholder);
        gender_button.setChecked(!oldDogInfo.getGender().equals(resources.getString(R.string.male)));
        Picasso.get().load(oldDogInfo.getPic()).into(dogImage);
        dogImage.setBackgroundResource(R.drawable.add_dog_pic_border);
    }




    private void dogRaceSpinner() {
        spinnerDialog = new SpinnerDialog(getActivity(), new ArrayList<>(Arrays.asList(dogRace)),resources.getString(R.string.select_race));
        spinnerDialog.bindOnSpinerListener((item, position) -> {
            item = item.replaceAll("[\\s']", "\n");
            dog_type.setText(item);
        });

        dog_type_icon.setOnClickListener(v-> spinnerDialog.showSpinerDialog());
    }



    private void checkGender() {
        gender_button.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                gender_type.setText(resources.getString(R.string.female));
            } else {
                gender_type.setText(resources.getString(R.string.male));
            }
        });
    }






//                if (mUploadTask != null && mUploadTask.isInProgress()) {
//        Toast.makeText(getActivity(), resources.getString(R.string.upload_progress), Toast.LENGTH_LONG).show();
//    } else {
//        saveChanges();
//    }



}


