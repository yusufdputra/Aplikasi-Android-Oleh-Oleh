package com.ysf.oleholeh.ui.pesananku.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.jpvs0101.currencyfy.Currencyfy;
import com.ysf.oleholeh.R;
import com.ysf.oleholeh.model.PemesananModel;
import com.ysf.oleholeh.ui.checkout.PembayaranFragment;
import com.ysf.oleholeh.ui.helper.KEY;
import com.ysf.oleholeh.ui.helper.Methods;
import com.ysf.oleholeh.ui.pesananku.ui.main.activity.DetailPesananActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static com.ysf.oleholeh.ui.helper.KEY.KEY_ID;
import static com.ysf.oleholeh.ui.helper.KEY.KEY_ID_METODE_PEMBAYARAN;
import static com.ysf.oleholeh.ui.helper.KEY.KEY_STATUS_PESANAN;
import static com.ysf.oleholeh.ui.helper.KEY.STATUS_BELUM_BAYAR;

public class BelumBayarFragment extends Fragment implements View.OnClickListener {
    private String idUser;

    private ImageView iv_empty_list;
    private RecyclerView rv_belum_bayar;

    private CollectionReference ref_pemesanan, ref_detail_pemesanan, ref_produk;
    private FirestoreRecyclerAdapter<PemesananModel, PemesananViewHolder> adapterPemesanan;
    private FirestoreRecyclerOptions options_pesanan;
    private StorageReference stRef_produk;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_belum_bayar, container, false);

        init(root);
        seClickListener();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            idUser = currentUser.getUid();
            // generate pesanan yang sudah melampaui batas waktu


            getListPesanan();
        }
        return root;
    }




    private void init(View root) {
        iv_empty_list = root.findViewById(R.id.iv_empty);
        rv_belum_bayar = root.findViewById(R.id.rv_belum_bayar);

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
                .setQuery(ref_pemesanan.whereEqualTo("id_user", idUser).whereEqualTo("id_status_pemesanan", "1"), PemesananModel.class)
                .build();

        rv_belum_bayar.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

    }


    @Override
    public void onStart() {
        super.onStart();

        adapterPemesanan = new FirestoreRecyclerAdapter<PemesananModel, PemesananViewHolder>(options_pesanan) {
            @Override
            public void onDataChanged() {
                if (getItemCount() == 0){
                    iv_empty_list.setVisibility(View.VISIBLE);
                    rv_belum_bayar.setVisibility(View.GONE);
                }else{
                    rv_belum_bayar.setVisibility(View.VISIBLE);
                }
            }

            @Override
            protected void onBindViewHolder(@NonNull PemesananViewHolder holder, int i, @NonNull PemesananModel pemesananModel) {
                holder.tv_total_bayar.setText("Rp. " + Currencyfy.currencyfy(pemesananModel.getTotal_bayar(), false, false));
                // get waktu
                Date timestamp = pemesananModel.getWaktu_pesan();
                // tambah waktu 15 menit
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(timestamp);
                calendar.add(Calendar.MINUTE, 15);

                // generate pesanan yang melewati batas
                SimpleDateFormat get_jam_format = new SimpleDateFormat("MMddHHmm");


                // batas waktu pesanan
                float batas_waktu = Integer.parseInt(get_jam_format.format(calendar.getTime()));
                // jam sekarang
                float waktu_sekarang = Integer.parseInt(get_jam_format.format(Calendar.getInstance().getTime()));
                Log.i("TAG", "batas waktu "+batas_waktu);
                Log.i("TAG", "Waktu sekarang "+waktu_sekarang);
                if (waktu_sekarang > batas_waktu){
                    // hapus data dari detail pemesanan
                    ref_detail_pemesanan.whereEqualTo("id_pemesanan", getSnapshots().getSnapshot(i).getId()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()){
                                for (QueryDocumentSnapshot doc: task.getResult()){
                                    ref_detail_pemesanan.document(doc.getId()).delete();
                                }
                            }
                        }
                    });
                    // hapus pemesanan
                    ref_pemesanan.document(getSnapshots().getSnapshot(i).getId()).delete();

                }else{
                    SimpleDateFormat dateFormat = new SimpleDateFormat("h:m a / d-MM-y");
                    holder.tv_batas_waktu.setText(dateFormat.format(calendar.getTime()));
                }




                // get barang yang ada di pemesanan
                ref_detail_pemesanan.whereEqualTo("id_pemesanan", getSnapshots().getSnapshot(i).getId())
                        .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            int size = task.getResult().size();
                            Methods methods = new Methods();

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


                // btn
                holder.btn_bayar_sekarang.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // buka fragment bayar
                        // kirim data harga total, id jenis pembayaran
//                        Bundle bundle = new Bundle();
//                        bundle.putString(KEY_ID, getSnapshots().getSnapshot(i).getId());
//                        bundle.putString(KEY_ID_METODE_PEMBAYARAN, pemesananModel.getId_metode_pembayaran());
//                        bundle.putDouble(KEY.KEY_HARGA, pemesananModel.getTotal_bayar());
//                        PembayaranFragment fragment = new PembayaranFragment();
//                        fragment.setArguments(bundle);
//                        AppCompatActivity activity = (AppCompatActivity) v.getContext();
//                        activity.getSupportFragmentManager().beginTransaction().replace(R.id.activity_pesanan, fragment).addToBackStack(null).commit();


                        Intent intent = new Intent(getActivity(), DetailPesananActivity.class);
                        intent.putExtra(KEY_ID, getSnapshots().getSnapshot(i).getId());
                        intent.putExtra(KEY_STATUS_PESANAN, STATUS_BELUM_BAYAR);
                        startActivity(intent);
                    }
                });
            }

            @NonNull
            @Override
            public PemesananViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_belum_bayar, parent, false);
                return new PemesananViewHolder(view);
            }
        };

        adapterPemesanan.startListening();
        rv_belum_bayar.setAdapter(adapterPemesanan);
//        if (adapterPemesanan.getItemCount() != 0){
//            adapterPemesanan.startListening();
//            rv_belum_bayar.setAdapter(adapterPemesanan);
//        }else{
//            rv_belum_bayar.setVisibility(View.GONE);
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
        TextView tv_total_bayar, tv_nama, tv_lainnya, tv_batas_waktu;
        Button btn_bayar_sekarang;
        ImageView iv_foto;

        public PemesananViewHolder(@NonNull View itemView) {
            super(itemView);

            tv_total_bayar = itemView.findViewById(R.id.tv_total_bayar);
            btn_bayar_sekarang = itemView.findViewById(R.id.btn_bayar_sekarang);
            tv_nama = itemView.findViewById(R.id.tv_nama);
            tv_lainnya = itemView.findViewById(R.id.tv_lainnya);
            iv_foto = itemView.findViewById(R.id.iv_foto);
            tv_batas_waktu = itemView.findViewById(R.id.tv_batas_waktu);

        }
    }
}
