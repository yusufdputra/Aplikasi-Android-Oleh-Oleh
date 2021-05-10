package com.ysf.oleholeh.ui.pesananku.ui.main.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.jpvs0101.currencyfy.Currencyfy;
import com.ysf.oleholeh.R;
import com.ysf.oleholeh.model.DetailPemesananModel;
import com.ysf.oleholeh.ui.checkout.PembayaranFragment;
import com.ysf.oleholeh.ui.helper.KEY;
import com.ysf.oleholeh.ui.helper.Methods;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static com.ysf.oleholeh.ui.helper.KEY.KEY_ID;
import static com.ysf.oleholeh.ui.helper.KEY.KEY_ID_METODE_PEMBAYARAN;
import static com.ysf.oleholeh.ui.helper.KEY.STATUS_BELUM_BAYAR;
import static com.ysf.oleholeh.ui.helper.KEY.STATUS_KIRIM;
import static com.ysf.oleholeh.ui.helper.KEY.STATUS_PROSES;
import static com.ysf.oleholeh.ui.helper.KEY.STATUS_SELESAI;
import static com.ysf.oleholeh.ui.helper.KEY.STATUS_TERIMA;

public class DetailPesananActivity extends AppCompatActivity implements View.OnClickListener {

    private LinearLayout cl_bottom_action;
    private ImageView iv_back;
    private RecyclerView rv_produk;
    private View include_belum_bayar, include_diproses, include_dikirim, include_diterima;
    private TextInputEditText et_note;
    private TextView tv_batas_waktu, tv_total_bayar, tv_estimasi_waktu, tv_alamat, tv_no_rek, tv_atas_nama;
    private Button btn_bayar, btn_cancel;

    private String id_pemesanan, id_pembayaran;
    private double harga_total;


    private CollectionReference ref_produk, ref_keranjang, ref_users, ref_pembayarans, ref_pemesanan, ref_detail_pemesanan;
    private StorageReference stRef_produk;

    private FirestoreRecyclerAdapter<DetailPemesananModel, DetailPemesananViewHolder> adapterPemesanan;
    private FirestoreRecyclerOptions options_pesanan;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_detail_pesanan);

        init();
        setClickListener();
        id_pemesanan = getIntent().getStringExtra(KEY.KEY_ID);
        String STATUS_PESANAN = getIntent().getStringExtra(KEY.KEY_STATUS_PESANAN);

        if (STATUS_PESANAN.equals(STATUS_BELUM_BAYAR)) {
            include_belum_bayar.setVisibility(View.VISIBLE);
        } else if (STATUS_PESANAN.equals(STATUS_PROSES)) {
            include_diproses.setVisibility(View.VISIBLE);
            cl_bottom_action.setVisibility(View.GONE);
        } else if (STATUS_PESANAN.equals(STATUS_KIRIM)) {
            include_dikirim.setVisibility(View.VISIBLE);
            cl_bottom_action.setVisibility(View.GONE);
        } else if (STATUS_PESANAN.equals(STATUS_TERIMA)) {
            include_diterima.setVisibility(View.VISIBLE);
            cl_bottom_action.setVisibility(View.GONE);
        }

        getProduk();
        getDetailPemesanan();

    }


    private void init() {
        iv_back = findViewById(R.id.iv_back);
        rv_produk = findViewById(R.id.rv_keranjang);
        et_note = findViewById(R.id.et_note);
        tv_total_bayar = findViewById(R.id.tv_total_bayar);
        tv_estimasi_waktu = findViewById(R.id.tv_estimasi_waktu);
        tv_alamat = findViewById(R.id.tv_alamat);
        tv_no_rek = findViewById(R.id.tv_no_rek);
        tv_atas_nama = findViewById(R.id.tv_atas_nama);
        btn_bayar = findViewById(R.id.btn_bayar_sekarang);
        btn_cancel = findViewById(R.id.btn_cancel);

        include_belum_bayar = findViewById(R.id.include_belum_bayar);
        tv_batas_waktu = include_belum_bayar.findViewById(R.id.tv_batas_waktu);

        include_diproses = findViewById(R.id.include_diproses);
        include_dikirim = findViewById(R.id.include_dikirim);
        include_diterima = findViewById(R.id.include_diterima);

        cl_bottom_action = findViewById(R.id.cl_bottom_action);


        ref_pembayarans = FirebaseFirestore.getInstance().collection(getResources().getString(R.string.pembayaran_ref));
        ref_users = FirebaseFirestore.getInstance().collection(getResources().getString(R.string.users_ref));
        ref_keranjang = FirebaseFirestore.getInstance().collection(getResources().getString(R.string.keranjang_ref));
        ref_produk = FirebaseFirestore.getInstance().collection(getResources().getString(R.string.produk_ref));
        ref_pemesanan = FirebaseFirestore.getInstance().collection(getResources().getString(R.string.pemesanan_ref));
        ref_detail_pemesanan = FirebaseFirestore.getInstance().collection(getResources().getString(R.string.detail_pemesanan_ref));
        stRef_produk = FirebaseStorage.getInstance().getReference().child(getResources().getString(R.string.produk_ref));
    }

    private void setClickListener() {
        iv_back.setOnClickListener(this);
        btn_cancel.setOnClickListener(this);
        btn_bayar.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.iv_back:
                onBackPressed();
                break;
            case R.id.btn_bayar_sekarang:
                goToBayarSekarang(v);
                break;
            case R.id.btn_cancel:

                break;
        }
    }

    private void goToBayarSekarang(View v) {
        // buka fragment bayar
        // kirim data harga total, id jenis pembayaran
        Bundle bundle = new Bundle();
        bundle.putString(KEY_ID, id_pemesanan);
        bundle.putString(KEY_ID_METODE_PEMBAYARAN, id_pembayaran);
        bundle.putDouble(KEY.KEY_HARGA, harga_total);
        PembayaranFragment fragment = new PembayaranFragment();
        fragment.setArguments(bundle);
        AppCompatActivity activity = (AppCompatActivity) v.getContext();
        activity.getSupportFragmentManager().beginTransaction().replace(R.id.detail_pesanan_activity, fragment).addToBackStack(null).commit();
    }

    private void getDetailPemesanan() {
        ref_pemesanan.document(id_pemesanan)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
                        // batas waktu
                        // get waktu
                        Date timestamp = doc.getDate("waktu_pesan");
                        // tambah waktu 15 menit
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(timestamp);
                        calendar.add(Calendar.MINUTE, 15);

                        SimpleDateFormat dateFormat = new SimpleDateFormat("h:m a / d-MM-y");
                        tv_batas_waktu.setText(dateFormat.format(calendar.getTime()));

                        harga_total = Double.parseDouble(doc.get("total_bayar").toString());
                        tv_total_bayar.setText("Rp. " + Currencyfy.currencyfy(harga_total, false, false));
                        tv_estimasi_waktu.setText(doc.get("estimasi_sampai").toString());
                        et_note.setText(doc.get("pesan").toString());

                        // get alamat dari user
                        ref_users.document(doc.get("id_user").toString()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot docUser = task.getResult();
                                    if (docUser.exists()) {
                                        tv_alamat.setText(docUser.get("list_alamat").toString());
                                    }
                                }
                            }
                        });

                        // get rekening
                        id_pembayaran = doc.get("id_metode_pembayaran").toString();
                        ref_pembayarans.document(id_pembayaran).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot docBank = task.getResult();
                                    if (docBank.exists()) {
                                        tv_no_rek.setText(docBank.get("rekening").toString());
                                        tv_atas_nama.setText(docBank.get("atas_nama").toString());
                                    }
                                }
                            }
                        });

                    }
                }
            }
        });

    }

    private void getProduk() {

        options_pesanan = new FirestoreRecyclerOptions.Builder<DetailPemesananModel>()
                .setQuery(ref_detail_pemesanan.whereEqualTo("id_pemesanan", id_pemesanan), DetailPemesananModel.class)
                .build();

        rv_produk.setLayoutManager(new LinearLayoutManager(getBaseContext(), LinearLayoutManager.VERTICAL, false));
        rv_produk.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Methods abs = new Methods();

        adapterPemesanan = new FirestoreRecyclerAdapter<DetailPemesananModel, DetailPemesananViewHolder>(options_pesanan) {
            @Override
            protected void onBindViewHolder(@NonNull DetailPemesananViewHolder holder, int i, @NonNull DetailPemesananModel detailPemesananModel) {
                holder.tv_kuantitas.setText(detailPemesananModel.getKuantitas() + "Pcs");


                ref_produk.document(detailPemesananModel.getId_produk()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot doc = task.getResult();
                            if (doc.exists()) {
                                holder.tv_nama.setText(doc.get("nama").toString());
                                holder.tv_harga.setText("Rp. " + Currencyfy.currencyfy(Double.parseDouble(doc.get("harga").toString()), false, false));

                                abs.setImg(stRef_produk, holder.iv_foto, doc.get("foto_path").toString());

                            }
                        }
                    }
                });
            }

            @NonNull
            @Override
            public DetailPemesananViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_checkout, parent, false);
                return new DetailPemesananViewHolder(view);
            }
        };
        adapterPemesanan.startListening();
        rv_produk.setAdapter(adapterPemesanan);

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private class DetailPemesananViewHolder extends RecyclerView.ViewHolder {
        ImageView iv_foto;
        TextView tv_nama, tv_harga, tv_kuantitas;

        public DetailPemesananViewHolder(@NonNull View itemView) {
            super(itemView);
            iv_foto = itemView.findViewById(R.id.iv_foto);
            tv_nama = itemView.findViewById(R.id.tv_nama);
            tv_harga = itemView.findViewById(R.id.tv_harga);
            tv_kuantitas = itemView.findViewById(R.id.tv_kuantitas);
        }
    }
}
