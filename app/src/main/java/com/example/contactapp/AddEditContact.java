package com.example.contactapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class AddEditContact extends AppCompatActivity {

    private ImageView profileIv;
    private EditText nameEt,phoneEt,emailEt,noteEt;

    private String id;
    private String image;
    private String name;
    private String phone;
    private String email;
    private String note;
    private String addedTime;
    private Boolean isEditMode;

    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int STORAGE_PERMISSION_CODE = 200;
    private static final int IMAGE_FROM_GALLERY_CODE = 300;
    private static final int IMAGE_FROM_CAMERA_CODE = 400;

    private String[] cameraPermission;
    private String[] storagePermission;

    private Uri imageUri;

    private DbHelper dbHelper;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_contact);

        dbHelper = new DbHelper(this);

        cameraPermission = new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        ActionBar actionBar = getSupportActionBar();


        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        profileIv = findViewById(R.id.profileIv);
        nameEt = findViewById(R.id.nameEt);
        phoneEt = findViewById(R.id.phoneEt);
        emailEt = findViewById(R.id.emailEt);
        noteEt = findViewById(R.id.noteEt);
        FloatingActionButton fab = findViewById(R.id.fab);

        Intent intent = getIntent();
        isEditMode = intent.getBooleanExtra("isEditMode",false);

        if (isEditMode){

            actionBar.setTitle("Update Contact");

            id = intent.getStringExtra("ID");
            name = intent.getStringExtra("NAME");
            phone = intent.getStringExtra("PHONE");
            email = intent.getStringExtra("EMAIL");
            note = intent.getStringExtra("NOTE");
            addedTime = intent.getStringExtra("ADDEDTIME");
            String updatedTime = intent.getStringExtra("UPDATEDTIME");
            image = intent.getStringExtra("IMAGE");

            //set value in editText field
            nameEt.setText(name);
            phoneEt.setText(phone);
            emailEt.setText(email);
            noteEt.setText(note);

            imageUri = Uri.parse(image);

            if (image.equals("")){
                profileIv.setImageResource(R.drawable.ic_baseline_person_24);
            }else {
                profileIv.setImageURI(imageUri);
            }

        }else {
            actionBar.setTitle("Add Contact");
        }

        fab.setOnClickListener(v -> saveData());

        profileIv.setOnClickListener(v -> showImagePickerDialog());
    }

    private void showImagePickerDialog() {

        String[] options = {"Camera","Gallery"};

        AlertDialog.Builder builder  = new AlertDialog.Builder(this);

        builder.setTitle("Choose An Option");
        builder.setItems(options, (dialog, which) -> {

            if (which == 0){ //start from 0 index
                //camera selected
                if (!checkCameraPermission()){
                    //request camera permission
                    requestCameraPermission();
                }else {
                    pickFromCamera();
                }

            }else if (which == 1){
                //Gallery selected
                if (!checkStoragePermission()){
                    //request storage permission
                    requestStoragePermission();
                }else {
                    pickFromGallery();
                }

            }
        }).create().show();
    }

    private void pickFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");

        startActivityForResult(galleryIntent,IMAGE_FROM_GALLERY_CODE);
    }

    private void pickFromCamera() {

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"IMAGE_TITLE");
        values.put(MediaStore.Images.Media.DESCRIPTION,"IMAGE_DETAIL");

        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);

        //intent to open camera
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);

        startActivityForResult(cameraIntent,IMAGE_FROM_CAMERA_CODE);
    }

    private void saveData() {

        name = nameEt.getText().toString();
        phone = phoneEt.getText().toString();
        email = emailEt.getText().toString();
        note = noteEt.getText().toString();

        String timeStamp = ""+System.currentTimeMillis();


        if (!name.isEmpty() || !phone.isEmpty() || !email.isEmpty() || !note.isEmpty()){

            if (isEditMode){
                // edit
                dbHelper.updateContact(
                        ""+id,
                        ""+image,
                        ""+name,
                        ""+phone,
                        ""+email,
                        ""+note,
                        ""+addedTime,
                        ""+timeStamp // updated time will new time
                );

                Toast.makeText(getApplicationContext(), "Updated Successfully....", Toast.LENGTH_SHORT).show();

            }else {
                // add
                long id =  dbHelper.insertContact(
                        ""+imageUri,
                        ""+name,
                        ""+phone,
                        ""+email,
                        ""+note,
                        ""+timeStamp,
                        ""+timeStamp
                );
                //Inset successfully toast message
                Toast.makeText(getApplicationContext(), "Inserted Successfully.... "+id, Toast.LENGTH_SHORT).show();
            }

        }else {
            // toast message
            Toast.makeText(getApplicationContext(), "Nothing to save....", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    //camera permission
    private boolean checkCameraPermission(){
        boolean result = ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

        return result & result1;
    }

    //request for camera permission
    private void requestCameraPermission(){
        ActivityCompat.requestPermissions(this,cameraPermission,CAMERA_PERMISSION_CODE); // handle request permission on override method
    }

    //check storage permission
    private boolean checkStoragePermission(){

        return ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
    }

    //request for camera permission
    private void requestStoragePermission(){
        ActivityCompat.requestPermissions(this,storagePermission,STORAGE_PERMISSION_CODE);
    }


    //handle request permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case CAMERA_PERMISSION_CODE:
                if (grantResults.length >0){

                    //if all permission allowed return true , otherwise false
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (cameraAccepted && storageAccepted){
                        //both permission granted
                        pickFromCamera();
                    }else {
                        //permission not granted
                        Toast.makeText(getApplicationContext(), "Camera & Storage Permission needed..", Toast.LENGTH_SHORT).show();
                    }
                }
                break;

            case STORAGE_PERMISSION_CODE:
                if (grantResults.length >0){

                    //if all permission allowed return true , otherwise false
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (storageAccepted){
                        //permission granted
                        pickFromGallery();
                    }else {
                        //permission not granted
                        Toast.makeText(getApplicationContext(), "Storage Permission needed..", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
            if (requestCode == IMAGE_FROM_GALLERY_CODE){
                // picked image from gallery
                //crop image
                assert data != null;
                CropImage.activity(data.getData())
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1,1)
                        .start(AddEditContact.this);

            }else if (requestCode == IMAGE_FROM_CAMERA_CODE){
                //picked image from camera
                //crop Image
                CropImage.activity(imageUri)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1,1)
                        .start(AddEditContact.this);
            }else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                assert result != null;
                imageUri = result.getUri();
                profileIv.setImageURI(imageUri);

            }
        }
    }
}