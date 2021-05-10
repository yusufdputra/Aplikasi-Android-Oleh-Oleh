package com.ysf.oleholeh.ui.pesananku.ui.main;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.jpvs0101.currencyfy.Currencyfy;
import com.ysf.oleholeh.R;
import com.ysf.oleholeh.model.PemesananModel;
import com.ysf.oleholeh.ui.helper.Methods;
import com.ysf.oleholeh.ui.home.HomeFragment;
import com.ysf.oleholeh.ui.pesananku.ui.main.activity.DetailPesananActivity;

import static com.ysf.oleholeh.ui.helper.KEY.KEY_ID;
import static com.ysf.oleholeh.ui.helper.KEY.KEY_STATUS_PESANAN;
import static com.ysf.oleholeh.ui.helper.KEY.STATUS_KIRIM;

public class DiKirimFragment extends Fragment implements View.OnClickListener {
    private String idUser;

    private ImageView iv_empty_list;
    private RecyclerView rv_dikirim;

    private CollectionReference ref_pemesanan, ref_detail_pemesanan, ref_produk;
    private FirestoreRecyclerAdapter<PemesananModel, PemesananViewHolder> adapterPemesanan;
    private FirestoreRecyclerOptions options_pesanan;
    private StorageReference stRef_produk;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_dikirim, container, false);

        init(root);
        seClickListener();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            idUser = currentUser.getUid();
            getListPesanan();
        }
        return root;
    }


    private void init(View root) {
        iv_empty_list = root.findViewById(R.id.iv_empty);
        rv_dikirim = root.findViewById(R.id.rv_dikirim);

        ref_produk = FirebaseFirestore.getInstance().collection(getResources().getString(R.string.produk_ref));
        ref_pemesanan = FirebaseFirestore.getInstance().collection(getResources().getString(R.string.pemesanan_ref));
        ref_detail_pemesanan = FirebaseFirestore.getInstance().collection(getResources().getString(R.string.detail_pemesanan_ref));
        stRef_produk = FirebaseStorage.getInstance().getReference().child(getResources().getString(R.string.produk_ref));
    }

    private void seClickListener() {

    }

    @Override
    public void onClick(View v) {

    }

    private void getListPesanan() {

        options_pesanan = new FirestoreRecyclerOptions.Builder<PemesananModel>()
                .setQuery(ref_pemesanan.whereEqualTo("id_user", idUser).whereEqualTo("id_status_pemesanan", "3"), PemesananModel.class)
                .build();

        rv_dikirim.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

    }


    @Override
    public void onStart() {
        super.onStart();
        Methods methods = new Methods();
        adapterPemesanan = new FirestoreRecyclerAdapter<PemesananModel, PemesananViewHolder>(options_pesanan) {
            @Override
            public void onDataChanged() {
                if (getItemCount() == 0){
                    iv_empty_list.setVisibility(View.VISIBLE);
                    rv_dikirim.setVisibility(View.GONE);
                }else{
                    rv_dikirim.setVisibility(View.VISIBLE);
                }
            }
            @Override
            protected void onBindViewHolder(@NonNull PemesananViewHolder holder, int i, @NonNull PemesananModel pemesananModel) {
                holder.tv_total_bayar.setText("Rp. " + Currencyfy.currencyfy(pemesananModel.getTotal_bayar(), false, false));

                holder.tv_estimasi_waktu.setText(pemesananModel.getEstimasi_sampai());


                // get barang yang ada di pemesanan
                ref_detail_pemesanan.whereEqualTo("id_pemesanan", getSnapshots().getSnapshot(i).getId())
                        .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            int size = task.getResult().size();


                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                ref_produk.document(doc.get("id_produk").toString()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            DocumentSnapshot documentSnapshot = task.getResult();
                                            if (documentSnapshot.exists()) {
                                                holder.tv_nama.setText(documentSnapshot.get("nama").toString());
                                                methods.setImg(stRef_produk, holder.iv_foto, documentSnapshot.get("foto_path").toString());
                                            }
                                        }

                                    }
                                });

                                break;
                            }

                            if (size > 1) {
                                holder.tv_lainnya.setVisibility(View.VISIBLE);
                            } else {
                                holder.tv_lainnya.setVisibility(View.GONE);
                            }
                        }
                    }
                });


                // btn detail
                holder.btn_detail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), DetailPesananActivity.class);
                        intent.putExtra(KEY_ID, getSnapshots().getSnapshot(i).getId());
                        intent.putExtra(KEY_STATUS_PESANAN, STATUS_KIRIM);
                        startActivity(intent);
                    }
                });

                // terima pesanan
                holder.btn_terima.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder alerBuilder = new AlertDialog.Builder(getContext());
                        alerBuilder.setMessage("Apakah anda sudah menerima pesanan ini?");
                        alerBuilder.setPositiveButton("Sudah", (dialogInterface, j) -> {
                            ref_pemesanan.document(getSnapshots().getSnapshot(i).getId()).update("id_status_pemesanan", "4");
                            // update stok
                            ref_detail_pemesanan.whereEqualTo("id_pemesanan", getSnapshots().getSnapshot(i).getId())
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            for (QueryDocumentSnapshot doc: task.getResult()){
                                                ref_produk.document(doc.get("id_produk").toString()).update(
                                                        "stok", FieldValue.increment(-Long.parseLong(doc.get("kuantitas").toString())),
                                                        "terjual_akum", FieldValue.increment(+Long.parseLong(doc.get("kuantitas").toString()))
                                                );
                                            }
                                        }
                                    });
                        });

                        alerBuilder.setNegativeButton("Belum", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        });
                        alerBuilder.show();
                    }
                });
            }

            @NonNull
            @Override
            public PemesananViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_proses, parent, false);
                return new PemesananViewHolder(view);
            }
        };

        adapterPemesanan.startListening();
        rv_dikirim.setAdapter(adapterPemesanan);

//        if (adapterPemesanan.getItemCount() != 0){
//            adapterPemesanan.startListening();
//            rv_diproses.setAdapter(adapterPemesanan);
//        }else{
//            rv_diproses.setVisibility(View.GONE);
//            iv_empty_list.setVisibility(View.VISIBLE);
//        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapterPemesanan != null) {
            adapterPemesanan.stopListening();
        }
    }

    private class PemesananViewHolder extends RecyclerView.ViewHolder {
        TextView tv_total_bayar, tv_nama, tv_lainnya, tv_estimasi_waktu;
        Button btn_detail, btn_terima;
        ImageView iv_foto;

        public PemesananViewHolder(@NonNull View itemView) {
            super(itemView);

            tv_total_bayar = itemView.findViewById(R.id.tv_total_bayar);
            btn_detail = itemView.findViewById(R.id.btn_detail);
            tv_nama = itemView.findViewById(R.id.tv_nama);
            tv_lainnya = itemView.findViewById(R.id.tv_lainnya);
            tv_estimasi_waktu = itemView.findViewById(R.id.tv_estimasi_waktu);
            iv_foto = itemView.findViewById(R.id.iv_foto);
            btn_terima = itemView.findViewById(R.id.btn_terima);
            btn_terima.setVisibility(View.VISIBLE);

        }
    }
}
