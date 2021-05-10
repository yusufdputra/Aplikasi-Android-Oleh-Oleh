package com.ysf.oleholeh.ui.checkout;

import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.jpvs0101.currencyfy.Currencyfy;
import com.ysf.oleholeh.R;
import com.ysf.oleholeh.adapter.CustomSpinnerBankAdapter;
import com.ysf.oleholeh.adapter.ItemCheckOutAdapter;
import com.ysf.oleholeh.model.DetailPemesananModel;
import com.ysf.oleholeh.ui.helper.KEY;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;

import static com.ysf.oleholeh.ui.helper.KEY.KEY_ID;
import static com.ysf.oleholeh.ui.helper.KEY.KEY_ID_METODE_PEMBAYARAN;

public class CheckoutActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    String idUser;

    private String estimasi_sampai, getHarga_total, get_id_pembayaran, id_pemesanan;
    double harga_ongkir = 0;
    double total;
    private final static int FINE_LOCATION = 100;
    private final ArrayList<String> list_bank = new ArrayList<>();
    private final ArrayList<String> id_list_bank = new ArrayList<>();
    private final int PLACE_PICKER_REQUEST = 1;
    WifiManager wifiManager;
    private TextView tv_alamat, tv_no_rek, tv_atas_nama, tv_ongkir, tv_estimasi_waktu, tv_total_bayar, tv_kode_unik, tv_ask_kode_unik;
    private ImageView iv_search_alamat, iv_back;
    private TextInputEditText et_note;
    private RecyclerView rv_keranjang;
    private AppCompatSpinner sp_jenis_pembayaran;
    private ProgressBar progressBar, progressBarBtn;
    private Button btn_pesan;
    private CollectionReference ref_produk, ref_keranjang, ref_users, ref_pembayarans, ref_pemesanan, ref_detail_pemesanan;
    private StorageReference stRef_produk;
    private ArrayList<String> id_keranjang;
    private String id_suplier;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_checkout);


        init();
        seClickListener();

        // get alamat dari kamera

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            idUser = currentUser.getUid();
            getAlamatAntar(idUser);

            Intent intent = getIntent();
            id_keranjang = intent.getStringArrayListExtra(KEY.KEY_ID);
            getHarga_total = intent.getStringExtra(KEY.KEY_HARGA);
            if (id_keranjang != null) {
                getListKeranjang(idUser, id_keranjang);
            }
        }

        getJenisPengiriman();


        getEstimasiSampai();


    }


    private void seClickListener() {
        sp_jenis_pembayaran.setOnItemSelectedListener(this);
        iv_back.setOnClickListener(this);
        iv_search_alamat.setOnClickListener(this);
        btn_pesan.setOnClickListener(this);
        tv_ask_kode_unik.setOnClickListener(this);
    }

    private void init() {
        iv_back = findViewById(R.id.iv_back);
        tv_alamat = findViewById(R.id.tv_alamat);
        tv_no_rek = findViewById(R.id.tv_no_rek);
        tv_atas_nama = findViewById(R.id.tv_atas_nama);
        tv_ongkir = findViewById(R.id.tv_ongkir);
        tv_estimasi_waktu = findViewById(R.id.tv_estimasi_waktu);
        tv_total_bayar = findViewById(R.id.tv_total_bayar);
        iv_search_alamat = findViewById(R.id.iv_search_alamat);
        rv_keranjang = findViewById(R.id.rv_keranjang);
        tv_kode_unik = findViewById(R.id.tv_kode_unik);
        tv_ask_kode_unik = findViewById(R.id.tv_ask_kode_unik);

        et_note = findViewById(R.id.et_note);
        et_note.setHint(getResources().getString(R.string.note));
        et_note.setHintTextColor(getResources().getColor(R.color.grey_font));

        sp_jenis_pembayaran = findViewById(R.id.sp_jenis_pembayaran);
        progressBar = findViewById(R.id.progressBar);
        progressBarBtn = findViewById(R.id.progressBarButton);
        btn_pesan = findViewById(R.id.btn_pesan);

        ref_pembayarans = FirebaseFirestore.getInstance().collection(getResources().getString(R.string.pembayaran_ref));
        ref_users = FirebaseFirestore.getInstance().collection(getResources().getString(R.string.users_ref));
        ref_keranjang = FirebaseFirestore.getInstance().collection(getResources().getString(R.string.keranjang_ref));
        ref_produk = FirebaseFirestore.getInstance().collection(getResources().getString(R.string.produk_ref));
        ref_pemesanan = FirebaseFirestore.getInstance().collection(getResources().getString(R.string.pemesanan_ref));
        ref_detail_pemesanan = FirebaseFirestore.getInstance().collection(getResources().getString(R.string.detail_pemesanan_ref));
        stRef_produk = FirebaseStorage.getInstance().getReference().child(getResources().getString(R.string.produk_ref));

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.iv_back:
                onBackPressed();
                finish();
                break;
            case R.id.iv_search_alamat:
                Intent intent = new Intent(this, MapsActivity.class);
                startActivity(intent);
//                SelectMap(v);
                break;
            case R.id.btn_pesan:
                doPemesanan(v);
                break;
            case R.id.tv_ask_kode_unik:
                Toast.makeText(getBaseContext(), "Untuk memastikan Anda mentransfer Tepat sesuai \"Jumlah Bayar\" yang tertera di transaksi Anda hingga 3 terakhir.", Toast.LENGTH_LONG).show();
        }

    }


    private void doPemesanan(View v) {
        progressBarBtn.setVisibility(View.VISIBLE);

        Map<String, Object> data = new HashMap<>();

        data.put("id_user", idUser);
        data.put("id_status_pemesanan", "1");
        data.put("pesan", et_note.getText().toString());
        data.put("id_metode_pembayaran",get_id_pembayaran);
        data.put("ongkir", harga_ongkir);
        data.put("total_bayar", total);
        data.put("waktu_pesan", Timestamp.now());
        data.put("estimasi_sampai", estimasi_sampai);
        data.put("id_suplier", id_suplier);

        ref_pemesanan.add(data).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                if (task.isSuccessful()){
                    DocumentReference doc = task.getResult();
                    id_pemesanan = doc.getId();
                    for (int i = 0; i< id_keranjang.size(); i++){
                        ref_keranjang.document(id_keranjang.get(i)).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()){
                                    DocumentSnapshot docs = task.getResult();
                                    String id_produk = docs.get("id_produk").toString();
                                    int kuantitas = Integer.parseInt(docs.get("kuantitas").toString());
                                    DetailPemesananModel detailPemesanan = new DetailPemesananModel(
                                            id_pemesanan,
                                            id_produk,
                                            kuantitas
                                    );
                                    //simpan
                                    ref_detail_pemesanan.add(detailPemesanan);
                                    ref_keranjang.document(docs.getId()).delete();
                                }
                            }
                        });
                    }
                    progressBarBtn.setVisibility(View.GONE);
                }
            }
        });



        // buka fragment bayar
        // kirim data harga total, id jenis pembayaran
        Bundle bundle = new Bundle();
        bundle.putString(KEY_ID, id_pemesanan);
        bundle.putString(KEY_ID_METODE_PEMBAYARAN, get_id_pembayaran);
        bundle.putDouble(KEY.KEY_HARGA, total);
        PembayaranFragment fragment = new PembayaranFragment();
        fragment.setArguments(bundle);
        AppCompatActivity activity = (AppCompatActivity) v.getContext();
        activity.getSupportFragmentManager().beginTransaction().replace(R.id.activity_checkout, fragment).addToBackStack(null).commit();

        }


    private void getListKeranjang(String idUser, ArrayList<String> id_keranjang) {
        ItemCheckOutAdapter adapter = new ItemCheckOutAdapter(getApplicationContext(), id_keranjang);
        rv_keranjang.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        rv_keranjang.setAdapter(adapter);
        rv_keranjang.setVisibility(View.VISIBLE);
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        get_id_pembayaran = id_list_bank.get(position);

        ref_pembayarans.document(get_id_pembayaran)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot doc = task.getResult();
                            if (doc.exists()) {
                                tv_no_rek.setText(doc.get("rekening").toString());
                                tv_atas_nama.setText(doc.get("atas_nama").toString());
                            }
                        }
                    }
                });
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        tv_atas_nama.setText("");
        tv_no_rek.setText("");
    }


    private void getAlamatAntar(String idUser) {
        ref_users.document(idUser).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (value.get("list_alamat") != null) {
                    tv_alamat.setText(value.get("list_alamat").toString());
                } else {
                    tv_alamat.setText("Alamat Tidak Ditemukan!");
                }
            }
        });
    }

    private void getJenisPengiriman() {

        ref_pembayarans.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        String nama_bank = doc.getString("nama_bank");
                        String id_pembayaran = doc.getId();

                        id_list_bank.add(id_pembayaran);
                        list_bank.add(nama_bank);

                        CustomSpinnerBankAdapter bankAdapter = new CustomSpinnerBankAdapter(getApplicationContext(), id_list_bank, list_bank);
                        sp_jenis_pembayaran.setAdapter(bankAdapter);

                    }

                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // get biaya ongkir
        ref_users.document(idUser).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (value.exists()){
                    GeoPoint kordinat_cust = value.getGeoPoint("kordinat_alamat");
                    double lat_cust = kordinat_cust.getLatitude();
                    double long_cust = kordinat_cust.getLongitude();

                    setOngkir(lat_cust, long_cust);
                }
            }
        });


        // cek alamat kosong atau tidak
        String cek_alamat = tv_alamat.getText().toString();
        if (cek_alamat == null || tv_alamat.equals("")){
            btn_pesan.setEnabled(false);
        }else{
            btn_pesan.setEnabled(true);
        }
    }

    public void setOngkir(double lat_cust, double long_cust){
        // get kordinat suplier
        CollectionReference ref_suplier = FirebaseFirestore.getInstance().collection("supliers");

        ref_suplier.whereEqualTo("nama", "Lentera Oleh-Oleh").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){

                    for (QueryDocumentSnapshot doc : task.getResult()){
                        id_suplier = doc.getId();
                        GeoPoint kordinat_supl = doc.getGeoPoint("koordinat_alamat");
                        double lat_supl = kordinat_supl.getLatitude();
                        double long_supl = kordinat_supl.getLongitude();

                        //hitung ongkir
                        if ((lat_cust == lat_cust) && (long_cust == long_supl)){
                            tv_ongkir.setText("Rp. "+ Currencyfy.currencyfy(0, false, false));
                        }else{

                            LatLng start = new LatLng(lat_supl, long_supl);
                            LatLng end = new LatLng(lat_cust, long_cust);
                            int Radius = 6371;// radius of earth in Km
                            double lat1 = start.latitude;
                            double lat2 = end.latitude;
                            double lon1 = start.longitude;
                            double lon2 = end.longitude;
                            double dLat = Math.toRadians(lat2 - lat1);
                            double dLon = Math.toRadians(lon2 - lon1);
                            double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                                    + Math.cos(Math.toRadians(lat1))
                                    * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                                    * Math.sin(dLon / 2);
                            double c = 2 * Math.asin(Math.sqrt(a));
                            double valueResult = Radius * c;
                            double km = valueResult / 1;
//                            DecimalFormat newFormat = new DecimalFormat("####");
//                            int kmInDec = Integer.valueOf(newFormat.format(km));
//                            double meter = valueResult % 1000;
//                            int meterInDec = Integer.valueOf(newFormat.format(meter));
//                            Log.i("Radius Value", "" + valueResult + "   KM  " + kmInDec
//                                    + " Meter   " + meterInDec);


                            if (valueResult >= 0 &&  valueResult < 5){
                                harga_ongkir = 3000;
                                tv_ongkir.setText("Rp. "+ Currencyfy.currencyfy(harga_ongkir, false, false));
                            }else if (valueResult >= 5 &&  valueResult < 10){
                                harga_ongkir = 5000;
                                tv_ongkir.setText("Rp. "+ Currencyfy.currencyfy(harga_ongkir, false, false));
                            }else if (valueResult >= 10){
                                harga_ongkir = 10000;
                                tv_ongkir.setText("Rp. "+ Currencyfy.currencyfy(harga_ongkir, false, false));
                            }

                            // get harga total
                            Integer GethargaTotal = Integer.valueOf(getHarga_total.replaceAll("[^0-9]", ""));

                            // get 3 number random
                            final int min = 100;
                            final int max = 899;
                            final int random = new Random().nextInt((max - min) + 1) + min;

                            tv_kode_unik.setText("+ Rp. "+Currencyfy.currencyfy(random, false, false));

                            total =  (GethargaTotal + harga_ongkir + random);
                            tv_total_bayar.setText("Rp. "+Currencyfy.currencyfy(total, false, false));
                        }
                    }
                }
            }
        });


    }

    private void getEstimasiSampai() {
        // get current time
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMMM-yyyy");
        String date = sdf.format(calendar.getTime());

        DateFormat jam = new SimpleDateFormat("H");
        DateFormat menit = new SimpleDateFormat("mm");
        String currenthour = jam.format(Calendar.getInstance().getTime());
        String currentMEnit = menit.format(Calendar.getInstance().getTime());


        // jika jam 10 kebawah
        if ((Integer.parseInt(currenthour) >= 6 && Integer.parseInt(currenthour) < 10) && Integer.parseInt(currentMEnit) <= 59 ){

            estimasi_sampai = date+ " , 13:00 WIB - 14:00 WIB";
        }else if ((Integer.parseInt(currenthour) >= 10 && Integer.parseInt(currenthour) < 14) && Integer.parseInt(currentMEnit) <= 59){

            estimasi_sampai = date+ " , 15:00 WIB - 16:00 WIB";
        }else {
           // tambah 1 hari
            calendar.add(Calendar.DATE, 1);
            date = sdf.format(calendar.getTime());
            estimasi_sampai = date+ " , 07:00 WIB - 10:00 WIB";
        }

        tv_estimasi_waktu.setText(estimasi_sampai);


    }
}
