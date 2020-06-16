package com.roeico7.dogadopt.MyDogs.editDog;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.roeico7.dogadopt.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.roeico7.dogadopt.Translate.Translate;
import com.roeico7.dogadopt.logic.FirebaseDAO;
import com.roeico7.dogadopt.logic.GeneralStuff;
import com.roeico7.dogadopt.objects.Dog;
import com.xeoh.android.checkboxgroup.CheckBoxGroup;

import java.util.ArrayList;
import java.util.HashMap;

import static com.roeico7.dogadopt.logic.GeneralStuff.resources;

/**
 * A simple {@link Fragment} subclass.
 */
public class EditDogInfo extends Fragment {
    private TextView et_description;
    private Button btn_add_dog;
    private ArrayList<String> options;



    private Uri dogImageUri;
    private StorageReference mStorageRef;
    private StorageTask mUploadTask;
    private StorageReference fileRef;
    private ProgressDialog progressDialog;
    private Task mDogRemoveTask;
    private Dog oldDogInfo;
    private Dog currentDog;
    private ImageView iv_title;

    public EditDogInfo() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dog_info, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        currentDog = EditDogInfoArgs.fromBundle(getArguments()).getNewDog();
        oldDogInfo = EditDogInfoArgs.fromBundle(getArguments()).getOldDog();

        iv_title = view.findViewById(R.id.iv_title);


        if(Translate.shared.checkDisplayLanguage())
            iv_title.setImageResource(R.drawable.additional_information2);

        if(!currentDog.getPic().contains("firebase")) {
            dogImageUri = Uri.parse(currentDog.getPic());
        }

        //get current marked options from dog
        options = oldDogInfo.getOptions();

        mStorageRef = FirebaseStorage.getInstance().getReference(resources.getString(R.string.key_uploads));

        HashMap<CheckBox, String> checkBoxMap = new HashMap<>();
        checkBoxMap.put(view.findViewById(R.id.option1), "Option1");
        checkBoxMap.put(view.findViewById(R.id.option2), "Option2");
        checkBoxMap.put(view.findViewById(R.id.option3), "Option3");
        checkBoxMap.put(view.findViewById(R.id.option4), "Option4");
        checkBoxMap.put(view.findViewById(R.id.option5), "Option5");
        checkBoxMap.put(view.findViewById(R.id.option6), "Option6");
        checkBoxMap.put(view.findViewById(R.id.option7), "Option7");
        checkBoxMap.put(view.findViewById(R.id.option8), "Option8");

        CheckBoxGroup<String> checkBoxGroup = new CheckBoxGroup<>(checkBoxMap,
                options -> {});

        //load current marked options
        if(options!=null) {
            for (String option : options) {
                checkBoxGroup.toggleCheckBox(option);
            }
        }



        et_description = view.findViewById(R.id.et_description);
        et_description.setText(oldDogInfo.getDescription());


        btn_add_dog = view.findViewById(R.id.btn_add_dog);

        et_description.setOnClickListener(v-> {
            AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
            View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.input_text, (ViewGroup) getView(), false);
            final EditText input = (EditText) viewInflated.findViewById(R.id.input);
            final Button button = (Button) viewInflated.findViewById(R.id.btn_ok);

            if(!et_description.getText().toString().equals(""))
                input.setText(et_description.getText().toString());

            button.setOnClickListener(b-> {
                et_description.setText(input.getText().toString());
                alertDialog.dismiss();

            });
            alertDialog.setView(viewInflated);

            alertDialog.show();
        });






        btn_add_dog.setOnClickListener(v-> {
            if(et_description.getText().toString().equals("")) {
                et_description.setError(resources.getString(R.string.empty_field));
            } else if (mUploadTask != null && mUploadTask.isInProgress()) {
                Toast.makeText(getActivity(), R.string.upload_progress, Toast.LENGTH_LONG).show();
            } else {
                options = checkBoxGroup.getValues();
                saveChanges();
            }
        });

    }

    private void saveChanges() {
        if(dogImageUri==null) {
            Dog d = new Dog(currentDog.getDogOwner(), resources.getString(R.string.key_dog1), currentDog.getName(), currentDog.getPic(), currentDog.getType(), currentDog.getGender(), currentDog.getAge(), et_description.getText().toString(), options);
            FirebaseDAO.shared.updateDog(oldDogInfo, d);


            progressDialog = new ProgressDialog(getContext());
            GeneralStuff.shared.executeAlert(progressDialog,
                    resources.getString(R.string.woof),
                    resources.getString(R.string.changes_saved),
                    true,
                    () -> NavHostFragment.findNavController(this).navigate(R.id.action_editDogInfo_to_navigation_myDogs));


        } else {
            progressDialog = new ProgressDialog(getContext());
            GeneralStuff.shared.executeProgress(progressDialog, resources.getString(R.string.save_dog), resources.getString(R.string.please_wait));

            fileRef = FirebaseStorage.getInstance().getReferenceFromUrl(oldDogInfo.getPic());
            mDogRemoveTask = fileRef.delete()
                    .addOnSuccessListener(removeSuccessListener)
                    .addOnFailureListener(removeFailureListener);

            fileRef = mStorageRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(System.currentTimeMillis() + "." + GeneralStuff.shared.getFileExtension(getContext(), dogImageUri));
            mUploadTask = fileRef.putFile(dogImageUri)
                    .addOnSuccessListener(uploadSuccessListener)
                    .addOnFailureListener(uploadFailureListener);
        }

    }


    private OnSuccessListener removeSuccessListener = (OnSuccessListener<Void>) aVoid -> {};
    private OnFailureListener removeFailureListener = e -> GeneralStuff.shared.showError(getContext(), e);


    private OnSuccessListener uploadSuccessListener = (OnSuccessListener<UploadTask.TaskSnapshot>) taskSnapshot -> {
        if (progressDialog.isShowing())
            progressDialog.dismiss();

        fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
            Dog d = new Dog(currentDog.getDogOwner(), resources.getString(R.string.key_dog1), currentDog.getName(), uri.toString(), currentDog.getType(), currentDog.getGender(), currentDog.getAge(), et_description.getText().toString(), options);
            FirebaseDAO.shared.updateDog(oldDogInfo, d);


            progressDialog = new ProgressDialog(getContext());
            GeneralStuff.shared.executeAlert(progressDialog,
                    resources.getString(R.string.woof),
                    resources.getString(R.string.changes_saved),
                    true,
                    () -> NavHostFragment.findNavController(this).navigate(R.id.action_editDogInfo_to_navigation_myDogs));

        })

                .addOnFailureListener(e -> GeneralStuff.shared.showError(getContext(), e));
    };


    private OnFailureListener uploadFailureListener = e ->  {
        if (progressDialog.isShowing())
            progressDialog.dismiss();

        GeneralStuff.shared.showError(getContext(), e);
    };

}
