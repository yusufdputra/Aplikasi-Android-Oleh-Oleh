package com.ysf.oleholeh.ui.auth;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.ysf.oleholeh.R;
import com.ysf.oleholeh.model.Users;
import com.ysf.oleholeh.ui.helper.KEY;

import static com.ysf.oleholeh.ui.helper.KEY.MSG;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {

    // Image request code for onActivityResult() .
    int Image_Request_Code1 = 1;
    private Button btnSignup;
    private EditText tv_name, tv_phone, tv_email, tv_password;
    private Spinner tv_user_type;
    private ProgressBar progressBar;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private ImageView img_profile, iv_back;
    private Uri filepath_profile;
    private TextInputLayout error_nama, error_phone, error_email, error_password;


    private CollectionReference ref_users;
    private StorageReference strRefUsers;
    private StorageTask mUploadTask;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        init();
        setOnClickListener();
    }


    private void setOnClickListener() {
        btnSignup.setOnClickListener(this);
        img_profile.setOnClickListener(this);
        iv_back.setOnClickListener(this);
    }

    private void init() {
        iv_back = findViewById(R.id.iv_back);
        btnSignup = findViewById(R.id.btn_sign_up);
        tv_name = findViewById(R.id.tv_nameReg);
        tv_phone = findViewById(R.id.tv_phoneReg);
        tv_email = findViewById(R.id.tv_emailReg);
        tv_password = findViewById(R.id.tv_passwordReg);
        progressBar = findViewById(R.id.progressBar);
        img_profile = findViewById(R.id.profile_image);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        progressBar.setVisibility(View.GONE);

        error_nama = findViewById(R.id.textInputLayout_nama);
        error_email = findViewById(R.id.textInputLayout_email);
        error_phone = findViewById(R.id.textInputLayout_phone);
        error_password = findViewById(R.id.textInputLayout_password);

        ref_users = FirebaseFirestore.getInstance().collection(getResources().getString(R.string.users_ref));
        strRefUsers = FirebaseStorage.getInstance().getReference(getResources().getString(R.string.users_ref));

    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        Intent intent;
        switch (id) {
            case R.id.iv_back:
                this.onBackPressed();
                break;
            case R.id.btn_sign_up:
                cekDataUser(v);
                break;

            case R.id.profile_image:
                SelectImage(Image_Request_Code1);
                break;
        }
    }


    //method pilih gambar
    public void SelectImage(int request) {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(i, Image_Request_Code1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Image_Request_Code1 && resultCode == RESULT_OK && data != null) {
            filepath_profile = data.getData();
            img_profile.setImageURI(filepath_profile);
        }
    }


    private void cekDataUser(View v) {
        String email = tv_email.getText().toString();
        String password = tv_password.getText().toString();

        if (!validasiEmail() | !validasiPhone() | !validasiPassword() | !validasiNama() | !validasiFotoUser(v)) {
            return;
        } else {
            buatAkunBaru(v, email, password);


        }
    }

    private void buatAkunBaru(View v, String email, String password) {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setTitle("Loading...");
        progressDialog.show();

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    String idUser = currentUser.getUid();

                    uploadData(v, email, password, idUser);

                } else {
                    error_email.setError("Email yang digunakan sudah terdaftar");
                    progressDialog.dismiss();
                }
            }
        });
    }


    private String getFileExtension(Uri filepath) {
        ContentResolver cR = this.getApplicationContext().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(filepath));
    }

    private void uploadData(View v, String email, String password, String id_user) {

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setCanceledOnTouchOutside(true);
        String nama = tv_name.getText().toString();
        String phone = tv_phone.getText().toString();

        try {
            StorageReference fileReference_profile = strRefUsers.child(System.currentTimeMillis() + "." + getFileExtension(filepath_profile));


            mUploadTask = fileReference_profile.putFile(filepath_profile)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // hashMap.put(1, taskSnapshot.getStorage().getDownloadUrl().toString());

                            Users value = new Users(
                                    nama,
                                    email,
                                    phone,
                                    taskSnapshot.getStorage().getName(),
                                    null,
                                    null,
                                    null
                            );
                            ref_users.document(id_user).set(value).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){

                                        progressDialog.dismiss();
                                        EmailVerifikasi();
                                        Toast.makeText(getApplicationContext(), R.string.msg_new_akun, Toast.LENGTH_LONG).show();
                                        onBackPressed();
                                        finish();

                                    }
                                }
                            });




                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Snackbar snackbar = Snackbar
                                    .make(v, "Terjadi Kesalahan Saat Mendaftar!", Snackbar.LENGTH_LONG);
                            snackbar.show();
                        }
                    }).addOnProgressListener(new com.google.firebase.storage.OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                                double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                                progressDialog.setProgress((int) progress);
                        }
                    });

        } catch (Exception e) {
            progressDialog.cancel();
            Snackbar snackbar = Snackbar
                    .make(v, "Terjadi Kesalahan Saat Mendaftar!", Snackbar.LENGTH_LONG);
            snackbar.show();
        }
    }


    private void EmailVerifikasi() {
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            auth.signOut();
                        }
                    }
                });
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

    private boolean validasiFotoUser(View v) {
        if (filepath_profile == null) {
            Snackbar snackbar = Snackbar
                    .make(v, "Silahkan pilih foto profil terlebih dahulu!", Snackbar.LENGTH_LONG);
            snackbar.show();
            return false;
        } else {
            return true;
        }
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


    private boolean validasiEmail() {
        String email = error_email.getEditText().getText().toString().trim();
        if (email.isEmpty()) {
            error_email.setError("Field tidak boleh kosong");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            error_email.setError("Masukkan alamat email yang benar");
            return false;
        } else {
            error_email.setError(null);
            return true;
        }
    }

    private boolean validasiPassword() {
        String pw = error_password.getEditText().getText().toString().trim();
        if (pw.isEmpty()) {
            error_password.setError("Field tidak boleh kosong");
            return false;
        } else if (!LoginActivity.PASSWORD_PATTERN.matcher(pw).matches()) {
            error_password.setError("Password lemah. Harus terdiri dari 1 [0-9] dan [a-zA-Z]. Minimal 4 karakter.");
            return false;
        } else if (pw.length() < 6) {
            error_password.setError("Minimal 4 karakter.");
            return false;
        } else {
            error_password.setError(null);
            return true;
        }
    }

}
