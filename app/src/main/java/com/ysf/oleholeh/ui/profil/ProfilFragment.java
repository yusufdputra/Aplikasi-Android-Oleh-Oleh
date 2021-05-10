package com.ysf.oleholeh.ui.profil;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.ysf.oleholeh.R;
import com.ysf.oleholeh.ui.checkout.MapsActivity;
import com.ysf.oleholeh.ui.helper.Methods;
import com.ysf.oleholeh.ui.home.HomeFragment;
import com.ysf.oleholeh.ui.pesananku.PesananKu;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfilFragment extends Fragment implements View.OnClickListener {
    private CircleImageView iv_foto, iv_edit_profil;
    private TextView tv_nama, tv_email, tv_noHp, tv_alamat, tv_logout, tv_pesananku;
    private ImageView iv_search_alamat;

    private CollectionReference ref_users;
    private StorageReference stRef_users;
    private FirebaseAuth auth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_profil, container, false);
        init(root);
        seClickListener();
        getDataUser();

        return root;
    }


    private void seClickListener() {
        iv_edit_profil.setOnClickListener(this);
        iv_search_alamat.setOnClickListener(this);
        tv_logout.setOnClickListener(this);
        tv_pesananku.setOnClickListener(this);
    }

    private void init(View root) {
        iv_foto = root.findViewById(R.id.iv_profil);
        iv_edit_profil = root.findViewById(R.id.iv_edit_profil);
        tv_nama = root.findViewById(R.id.tv_nama);
        tv_email = root.findViewById(R.id.tv_email);
        tv_noHp = root.findViewById(R.id.no_hp);
        tv_alamat = root.findViewById(R.id.tv_detail_alamat);
        iv_search_alamat = root.findViewById(R.id.iv_search_alamat);
        tv_logout = root.findViewById(R.id.tv_logout);
        tv_pesananku = root.findViewById(R.id.tv_pesananku);

        ref_users = FirebaseFirestore.getInstance().collection(getResources().getString(R.string.users_ref));
        stRef_users = FirebaseStorage.getInstance().getReference(getResources().getString(R.string.users_ref));

        auth = FirebaseAuth.getInstance();

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        Intent intent;
        switch (id) {
            case R.id.iv_edit_profil:
                intent = new Intent(getActivity(), EditProfilActivity.class);
                startActivity(intent);
                break;
            case R.id.iv_search_alamat:
                intent = new Intent(getActivity(), MapsActivity.class);
                startActivity(intent);
                break;
            case R.id.tv_logout:
                logOut();
                break;
            case R.id.tv_pesananku:
                intent = new Intent(getActivity(), PesananKu.class);
                startActivity(intent);
                break;
        }
    }

    private void getDataUser() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String idUser = currentUser.getUid();
        ref_users.document(idUser).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                    String nama = value.get("nama").toString();
                    String email = value.get("email").toString();
                    String nomor_hp = value.get("nomor_hp").toString();
                    String foto_path = value.get("foto_path").toString();

                    tv_nama.setText(nama);
                    tv_email.setText(email);
                    tv_noHp.setText(nomor_hp);
                    if (value.get("list_alamat") == null) {
                        tv_alamat.setText(getResources().getString(R.string.not_found));
                    } else {
                        String alamat = value.get("list_alamat").toString();
                        tv_alamat.setText(alamat);
                    }

                    Methods methods = new Methods();
                    methods.setImg(stRef_users, iv_foto, foto_path);


            }
        });

    }

    private void logOut() {
        AlertDialog.Builder alerBuilder = new AlertDialog.Builder(getContext());
        alerBuilder.setMessage("Ingin keluar?");
        alerBuilder.setPositiveButton("Yes", (dialogInterface, i) -> {
            auth.signOut();
            Fragment fragment = new HomeFragment();
            loadFragment(fragment);
        });

        alerBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        alerBuilder.show();
    }

    private void loadFragment(Fragment fragment) {
        AppCompatActivity activity = (AppCompatActivity) getContext();
        activity.getSupportFragmentManager().beginTransaction().replace(R.id.frame_container, fragment).addToBackStack(null).commit();

    }

}
