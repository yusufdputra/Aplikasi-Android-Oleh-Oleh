package com.ysf.oleholeh.ui.profil;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.ysf.oleholeh.R;
import com.ysf.oleholeh.model.Users;
import com.ysf.oleholeh.ui.auth.LoginActivity;
import com.ysf.oleholeh.ui.helper.Methods;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfilActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView iv_back, iv_save;
    private ConstraintLayout cl_klikFoto;
    private TextInputLayout error_nama, error_phone;
    private TextInputEditText tv_nama, tv_noHp;
    private CircleImageView iv_foto;
    private TextView tv_edit_img;
    private Uri filepath_profile;
    int Image_Request_Code = 1;

    private String file_path_current;

    private CollectionReference ref_users;
    private StorageReference stRef_users;
    private StorageTask mUploadTask;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit_profil);
        init();
        seClickListener();
        getDataUser();

    }

    private void seClickListener() {
        iv_back.setOnClickListener(this);
        iv_save.setOnClickListener(this);
        cl_klikFoto.setOnClickListener(this);
        tv_edit_img.setOnClickListener(this);
    }

    private void init() {
        iv_back = findViewById(R.id.iv_back);
        iv_save = findViewById(R.id.iv_save);
        cl_klikFoto = findViewById(R.id.cl_klik_foto);
        tv_nama = findViewById(R.id.tv_edit_nama);
        tv_noHp = findViewById(R.id.tv_edit_no_hp);
        iv_foto = findViewById(R.id.iv_profil);
        tv_edit_img = findViewById(R.id.tv_edit_img);

        error_nama = findViewById(R.id.error_edit_nama);
        error_phone = findViewById(R.id.error_edit_no_hp);

        ref_users = FirebaseFirestore.getInstance().collection(getResources().getString(R.string.users_ref));
        stRef_users = FirebaseStorage.getInstance().getReference(getResources().getString(R.string.users_ref));
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        Intent intent;
        switch (id) {

            case R.id.iv_back:
                onBackPressed();
                break;
            case R.id.iv_save:
                if (!validasiNama() || !validasiPhone()){
                    return;
                }else{
                    updateDataUser();
                }
                break;
            case R.id.tv_edit_img:
                SelectImage(Image_Request_Code);
                break;
        }
    }

    private void updateDataUser() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String idUser = currentUser.getUid();

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setTitle("Loading...");
        progressDialog.show();

        String nama = tv_nama.getText().toString();
        String phone = tv_noHp.getText().toString();

        Map<String, Object> values = new HashMap<>();
        values.put("nama", nama);
        values.put("nomor_hp", phone);

        ref_users.document(idUser).update(values).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                progressDialog.dismiss();
                onBackPressed();
                finish();
            }
        });



    }

    //method pilih gambar
    public void SelectImage(int request) {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(i, Image_Request_Code);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Image_Request_Code && resultCode == RESULT_OK && data != null) {
            filepath_profile = data.getData();
            iv_foto.setImageURI(filepath_profile);
			
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setTitle("Loading...");
            progressDialog.show();

            StorageReference fileReference_profile = stRef_users.child(System.currentTimeMillis() + "." + getFileExtension(filepath_profile));

            mUploadTask = fileReference_profile.putFile(filepath_profile)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                            String idUser = currentUser.getUid();


                            // get nama foto_path lama
                            ref_users.document(idUser).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()){
                                        DocumentSnapshot doc = task.getResult();
                                        stRef_users.child(doc.get("foto_path").toString()).delete();
                                    }
                                }
                            });
                            ref_users.document(idUser).update("foto_path", taskSnapshot.getStorage().getName());

                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(),"Berhasil Mengubah Foto Profil" , Toast.LENGTH_LONG).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(),"Terjadi Kesalahan Saat Upload!" , Toast.LENGTH_LONG).show();

                        }
                    }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                            double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                            progressDialog.setProgress((int) progress);
                        }
                    });


        }
    }

    private String getFileExtension(Uri filepath) {
        ContentResolver cR = this.getApplicationContext().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(filepath));
    }


    private void getDataUser() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String idUser = currentUser.getUid();
        ref_users.document(idUser).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (value.exists()) {
                    String nama = value.get("nama").toString();
                    String nomor_hp = value.get("nomor_hp").toString();
                    String foto_path = value.get("foto_path").toString();

                    tv_nama.setText(nama);
                    tv_noHp.setText(nomor_hp);

                    Methods methods = new Methods();
                    methods.setImg(stRef_users, iv_foto, foto_path);

                }
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private boolean validasiPhone() {
        String phone = error_phone.getEditText().getText().toString().trim();
        if (phone.isEmpty()) {
            error_phone.setError("Field tidak boleh kosong");
            return false;
        } else if (phone.length() < 10) {
            error_phone.setError("Masukkan Nomor Hp dengan benar");
            return false;
        } else {
            error_phone.setError(null);
            return true;
        }
    }



    private boolean validasiFotoUser(View v) {
        if (file_path_current == null) {
            Snackbar snackbar = Snackbar
                    .make(v, "Silahkan pilih foto profil terlebih dahulu!", Snackbar.LENGTH_LONG);
            snackbar.show();
            return false;
        } else {
            return true;
        }
    }



    private boolean validasiNama() {
        String nama = error_nama.getEditText().getText().toString().trim();
        if (nama.isEmpty()) {
            error_nama.setError("Field tidak boleh kosong");
            return false;
        } else {
            error_nama.setError(null);
            return true;
        }
    }



}
