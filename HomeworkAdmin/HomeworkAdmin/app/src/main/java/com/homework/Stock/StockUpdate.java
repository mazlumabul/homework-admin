package com.homework.Stock;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.homework.R;
import com.kongzue.dialog.v3.TipDialog;
import com.kongzue.dialog.v3.WaitDialog;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import id.zelory.compressor.Compressor;

public class StockUpdate extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    AppCompatSpinner spinner;
    Boolean isSelected = false;
    String urunler[] ;
    EditText name , size , value;
    String isSelectedType;
    private static final int gallery_request =400;
    private static final int image_pick_request =600;
    private static final int camera_pick_request =800;
    String cameraPermission[];
    String storagePermission[];
    Uri image;
    StorageTask<UploadTask.TaskSnapshot> storageTask;
    private StorageReference imageStorage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_update);

        storagePermission= new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        cameraPermission= new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        imageStorage= FirebaseStorage.getInstance().getReference();
        name = (EditText)findViewById(R.id.name);
        size = (EditText)findViewById(R.id.size);
        value = (EditText)findViewById(R.id.value);

        spinner = (AppCompatSpinner)findViewById(R.id.spinner);

        spinner.setPrompt("Ünvan Seçiniz");

        if (getIntent().getStringExtra("list").equals("man")){
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.man,R.layout.spinner_item);

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);

            urunler = new String[]{"","shoes","pantolon","ceketler","tshirt","gym","sortlar","formalar"};
        }else if (getIntent().getStringExtra("list").equals("woman")){
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.woman,R.layout.spinner_item);

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
            urunler = new String[]{"","shoes","pantolon","ceketler","tshirt","gym","sortlar","formalar"};
        }else {
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.kid,R.layout.spinner_item);

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
            urunler = new String[]{"","shoes","pantolon","ceketler","tshirt","gym","sortlar","formalar"};
        }


        spinner.setOnItemSelectedListener(this);
        spinner.setSelection(0);
        spinner.getBackground().setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
    }
    //TODO persmissions
    private void uploadImage() {
        if (!checkGalleryPermissions()){
            requestStoragePermission();
        }
        else{ pickGallery();}
    }
    private boolean checkGalleryPermissions()
    {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }
    private void requestStoragePermission()
    {
        ActivityCompat.requestPermissions(this,storagePermission,gallery_request);

    }
    private void pickGallery()
    {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addCategory(intent.CATEGORY_OPENABLE);
        //  Intent intent = new Intent(Intent.ACTION_PICK);
        // intent.setType("*/*");
        startActivityForResult(intent,image_pick_request);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode==RESULT_OK){
            if(requestCode==image_pick_request){
                CropImage.activity(data.getData())
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1,1)
                        .setMinCropWindowSize(500 ,500)
                        .start(this);
            }
            if (requestCode==camera_pick_request){
                CropImage.activity(image)

                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1,1)
                        .setMinCropWindowSize(500, 500)
                        .start(this);

            }
        }

        final String _name = name.getText().toString();
        if (_name.isEmpty()){
            name.setError("Lütfen Ürün Adı Gir");
            name.requestFocus();
            return;
        }

        if (requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result =CropImage.getActivityResult(data);
            if(resultCode==RESULT_OK){

                WaitDialog.show(StockUpdate.this,"Resim Yükleniyor");
                Uri resultUri = result.getUri();
                File thumb_filePath = new File(resultUri.getPath());
                try {
                    Bitmap thumb_bitmap= new Compressor(this)
                            .setMaxHeight(512)
                            .setMaxWidth(512)
                            .setQuality(100)
                            .compressToBitmap(thumb_filePath);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();

                    thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    final byte[] thumb_byte = baos.toByteArray();
                    String fileName = _name + String.valueOf(Calendar.getInstance().getTimeInMillis());
                    //                        let storageRef = Storage.storage().reference().child("profileImage").child(filename)
                    final StorageReference filePath = imageStorage.child("product").child(fileName+".jpg");
                    storageTask = filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()){


                                filePath .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        final String downloadUri = uri.toString();

                                        final Map<String, Object> imageName = new HashMap<>();
                                        imageName.put("images",FieldValue.arrayUnion(downloadUri));
                                        DocumentReference db = FirebaseFirestore.getInstance().collection(getIntent().getStringExtra("list"))
                                                .document(isSelectedType).collection(isSelectedType).document(_name);
                                        db.set(imageName, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()){

                                                    Map<String, Object> map = new HashMap<>();
                                                    map.put("thumbImage",downloadUri);
                                                    DocumentReference db = FirebaseFirestore.getInstance().collection(getIntent().getStringExtra("list"))
                                                            .document(isSelectedType).collection(isSelectedType).document(_name);
                                                    db.set(map,SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()){
                                                                WaitDialog.dismiss();
                                                                TipDialog.show(StockUpdate.this,"Resim Yüklendi", TipDialog.TYPE.SUCCESS);
                                                            }
                                                        }
                                                    });


                                                }
                                            }
                                        });
                                    }
                                });
                            }
                        }
                    });
                }catch (Exception e){

                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {
        if (parent.getSelectedItemPosition() == 0){
            Toast.makeText(this,"Lütfen ünvan Seçini",Toast.LENGTH_LONG).show();
            isSelected = false;

            return;
        }
        else {
            isSelected = true;
            isSelectedType = urunler[position];

        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        Toast.makeText(this,"Lütfen ünvan Seçini",Toast.LENGTH_LONG).show();
    }



    public void addName(View view)
    {
        String _name = name.getText().toString();
        if (_name.isEmpty()){
            name.setError("Lütfen Ürün Adı Gir");
            name.requestFocus();
                    return;
        }

        DocumentReference ref = FirebaseFirestore.getInstance().collection(getIntent().getStringExtra("list"))
                .document(isSelectedType).collection(isSelectedType).document(_name);
        Map<String,Object> map = new HashMap<>();
        map.put("name",_name);

        ref.set(map, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    name.setText("");
                }
            }
        });

    }

    public void addSize(View view)
    {
        Float number = Float.parseFloat(String.valueOf(size.getText()));
        String _name = name.getText().toString();
        if (_name.isEmpty()){
            name.setError("Lütfen Ürün Adı Gir");
            name.requestFocus();
            return;
        }
        DocumentReference ref = FirebaseFirestore.getInstance().collection(getIntent().getStringExtra("list"))
                .document(isSelectedType).collection(isSelectedType).document(_name);
        Map<String,Object> map = new HashMap<>();
        map.put("number",FieldValue.arrayUnion(number));
                ref.set(map,SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            size.setText("");
                        }
                    }
                });
    }

    public void addValue(View view)
    {
        Float _value = Float.parseFloat(String.valueOf(value.getText()));
        String _name = name.getText().toString();
        if (_name.isEmpty()){
            name.setError("Lütfen Ürün Adı Gir");
            name.requestFocus();
            return;
        }
        DocumentReference ref = FirebaseFirestore.getInstance().collection(getIntent().getStringExtra("list"))
                .document(isSelectedType).collection(isSelectedType).document(_name);
        Map<String,Object> map = new HashMap<>();
        map.put("value",_value);
        ref.set(map,SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    value.setText("");
                }
            }
        });
    }

    public void addImage(View view) {
        uploadImage();
    }
}
