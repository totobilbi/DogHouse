package com.roeico7.dogadopt.MyDogs.addDog;


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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.roeico7.dogadopt.R;
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
public class AddDogInfo extends Fragment {
    private TextView et_description;
    private Button btn_add_dog;
    private ArrayList<String> options;



    private Uri dogImageUri;
    private StorageReference mStorageRef;
    private StorageTask mUploadTask;
    private StorageReference fileRef;
    private ProgressDialog progressDialog;
    private Dog currentDog;
    private ImageView iv_title;


    public AddDogInfo() {
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

        currentDog = AddDogInfoArgs.fromBundle(getArguments()).getDog();
        dogImageUri = Uri.parse(currentDog.getPic());

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


        et_description = view.findViewById(R.id.et_description);
        iv_title = view.findViewById(R.id.iv_title);
        btn_add_dog = view.findViewById(R.id.btn_add_dog);

        if(Translate.shared.checkDisplayLanguage())
            iv_title.setImageResource(R.drawable.additional_information2);

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
                addDog();
            }
        });

    }





    private void addDog() {

        progressDialog = new ProgressDialog(getContext());
        GeneralStuff.shared.executeProgress(progressDialog, resources.getString(R.string.upload_progress), resources.getString(R.string.please_wait));


        mStorageRef = FirebaseStorage.getInstance().getReference(getString(R.string.key_uploads));
            fileRef = mStorageRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(System.currentTimeMillis() + "." + GeneralStuff.shared.getFileExtension(getContext(), dogImageUri));
            mUploadTask = fileRef.putFile(dogImageUri)
                    .addOnSuccessListener(uploadSuccessListener)
                    .addOnFailureListener(uploadFailureListener);
    }



    private OnSuccessListener uploadSuccessListener = (OnSuccessListener<UploadTask.TaskSnapshot>) taskSnapshot -> {
        if (progressDialog.isShowing())
            progressDialog.dismiss();

        fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
            currentDog.setOptions(options);
            currentDog.setDescription(et_description.getText().toString());
            currentDog.setPic(uri.toString());
            FirebaseDAO.shared.addDog(currentDog);

            progressDialog = new ProgressDialog(getContext());
            GeneralStuff.shared.executeAlert(progressDialog,
                    resources.getString(R.string.woof),
                    resources.getString(R.string.dog_upload_finish),
                    true,
                    () -> NavHostFragment.findNavController(this).navigate(R.id.action_dogInfoFragment_to_navigation_myDogs));
        })

                .addOnFailureListener(e -> GeneralStuff.shared.showError(getContext(), e));
    };


    private OnFailureListener uploadFailureListener = e ->  {
        if (progressDialog.isShowing())
            progressDialog.dismiss();

        GeneralStuff.shared.showError(getContext(), e);
    };


}
