package com.ysf.oleholeh.ui.keranjang;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.jpvs0101.currencyfy.Currencyfy;
import com.ysf.oleholeh.R;
import com.ysf.oleholeh.model.KeranjangModel;
import com.ysf.oleholeh.ui.checkout.CheckoutActivity;
import com.ysf.oleholeh.ui.helper.KEY;
import com.ysf.oleholeh.ui.helper.Methods;

import java.util.ArrayList;
import java.util.List;

public class KeranjangActivity extends AppCompatActivity implements View.OnClickListener {

    List<String> id_keranjang;
    List<String> id_suplier;
    private ShimmerFrameLayout sl_keranjang;
    private RecyclerView rv_keranjang;
    private ProgressBar pb_total;
    private ImageView iv_back, iv_empty;
    private TextView tv_harga_total;
    private FirestoreRecyclerAdapter<KeranjangModel, KeranjangViewHolder> adapterKeranjang;
    private CollectionReference ref_produk, ref_keranjang;
    private FirestoreRecyclerOptions options_keranjang;
    private StorageReference stRef_produk;
    private View popUpDialogView;
    private ElegantNumberButton btn_kuantitas;
    private Button btn_batal, btn_simpan, btn_checkout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_keranjang);

        init();
        setClickListener();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String idUser = currentUser.getUid();
            getListKeranjang(idUser);
        }


    }


    private void init() {
        sl_keranjang = findViewById(R.id.SL_keranjang);
        rv_keranjang = findViewById(R.id.rv_keranjang);
        iv_back = findViewById(R.id.iv_back);
        iv_empty = findViewById(R.id.iv_empty);

        pb_total = findViewById(R.id.progressBar);
        tv_harga_total = findViewById(R.id.tv_total_bayar);
        btn_checkout = findViewById(R.id.btn_checkout);

        ref_keranjang = FirebaseFirestore.getInstance().collection(getResources().getString(R.string.keranjang_ref));
        ref_produk = FirebaseFirestore.getInstance().collection(getResources().getString(R.string.produk_ref));
        stRef_produk = FirebaseStorage.getInstance().getReference().child(getResources().getString(R.string.produk_ref));


        sl_keranjang.startShimmer();
    }

    private void setClickListener() {
        iv_back.setOnClickListener(this);
        btn_checkout.setOnClickListener(this);
    }

    private void getListKeranjang(String idUser) {

        options_keranjang = new FirestoreRecyclerOptions.Builder<KeranjangModel>()
                .setQuery(ref_keranjang.whereEqualTo("id_user", idUser), KeranjangModel.class)
                .build();
        rv_keranjang.setLayoutManager(new LinearLayoutManager(getApplicationContext()));


            sl_keranjang.stopShimmer();
            sl_keranjang.setVisibility(View.GONE);
            rv_keranjang.setVisibility(View.GONE);


    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        Intent intent = null;
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        switch (id) {
            case R.id.iv_back:
                onBackPressed();
                finish();
                break;
            case R.id.btn_checkout:
                intent = new Intent(getApplicationContext(), CheckoutActivity.class);
                intent.putStringArrayListExtra(KEY.KEY_ID, (ArrayList<String>) id_keranjang);
                intent.putExtra(KEY.KEY_HARGA, tv_harga_total.getText());
                startActivity(intent);
                break;
        }
    }


    @Override
    protected void onStart() {
        super.onStart();

        Methods abs = new Methods();

        if (id_keranjang != null) {
            id_keranjang.clear();
            tv_harga_total.setText("");
            btn_checkout.setEnabled(!id_keranjang.isEmpty());
        } else {
            id_keranjang = new ArrayList<>();
        }


        adapterKeranjang = new FirestoreRecyclerAdapter<KeranjangModel, KeranjangViewHolder>(options_keranjang) {
            @Override
            public void onDataChanged() {
                if (getItemCount() == 0){
                    iv_empty.setVisibility(View.VISIBLE);
                    rv_keranjang.setVisibility(View.GONE);
                }else{
                    rv_keranjang.setVisibility(View.VISIBLE);
                }
            }
            @Override
            protected void onBindViewHolder(@NonNull KeranjangViewHolder holder, int i, @NonNull KeranjangModel keranjangModel) {
                holder.tv_kuantitas.setText("Jumlah " + keranjangModel.getKuantitas());
                String getIdProduk = keranjangModel.getId_produk();

                ref_produk.document(getIdProduk).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
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


                holder.tv_ubah.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder alerBuilder = new AlertDialog.Builder(v.getContext());
                        alerBuilder.setTitle("Mau beli berapa?");
                        alerBuilder.setCancelable(false);
                        initPopupViewControls(v, keranjangModel.getKuantitas());

                        // Set the inflated layout view object to the AlertDialog builder
                        alerBuilder.setView(popUpDialogView);
                        // Create AlertDialog and show.
                        final AlertDialog alertDialog = alerBuilder.create();
                        alertDialog.show();
                        btn_simpan.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Integer new_kuantitas = Integer.valueOf(btn_kuantitas.getNumber());

                                ref_keranjang.document(getSnapshots().getSnapshot(i).getId())
                                        .update("kuantitas", new_kuantitas).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        adapterKeranjang.notifyItemChanged(i);
                                        adapterKeranjang.notifyDataSetChanged();
                                        tv_harga_total.setText(null);
                                        id_keranjang.clear();
                                    }
                                });

                                alertDialog.cancel();
                            }
                        });

                        btn_batal.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                alertDialog.cancel();
                            }
                        });
                    }
                });

                if (id_keranjang.size() == 0) {
                    holder.cb_item.setChecked(false);
                }

                holder.cb_item.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                        if (isChecked) {
                            pb_total.setVisibility(View.VISIBLE);
                            addTotalHarga(String.valueOf(holder.tv_harga.getText()), keranjangModel.getKuantitas());
                            id_keranjang.add(getSnapshots().getSnapshot(i).getId());
                        } else {
                            pb_total.setVisibility(View.VISIBLE);
                            minusTotalHarga(String.valueOf(holder.tv_harga.getText()), keranjangModel.getKuantitas());
                            for (int j = 0; j < id_keranjang.size(); j++) {
                                if (id_keranjang.get(j).equals(getSnapshots().getSnapshot(i).getId())) {
                                    id_keranjang.remove(j);
                                }
                            }
                        }

                        btn_checkout.setEnabled(!id_keranjang.isEmpty());
                    }
                });

                holder.tv_hapus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ref_keranjang.document(getSnapshots().getSnapshot(i).getId()).delete();
                        adapterKeranjang.notifyItemRemoved(i);
                        adapterKeranjang.notifyDataSetChanged();


                    }
                });

            }

            @NonNull
            @Override
            public KeranjangViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_keranjang, parent, false);
                return new KeranjangViewHolder(view);
            }
        };

        adapterKeranjang.startListening();
        rv_keranjang.setAdapter(adapterKeranjang);


    }

    private void minusTotalHarga(String harga, int kuantitas) {
        Integer hargaConv = Integer.valueOf(harga.replaceAll("[^0-9]", ""));

        String getHargaTotal = String.valueOf(tv_harga_total.getText());
        Integer minus;
        if (getHargaTotal != "") {
            Integer GethargaTotalConv = Integer.valueOf(getHargaTotal.replaceAll("[^0-9]", ""));
            minus = GethargaTotalConv - (hargaConv * kuantitas);
            tv_harga_total.setText("Rp. " + Currencyfy.currencyfy(minus, false, false));
            pb_total.setVisibility(View.GONE);
            tv_harga_total.setVisibility(View.VISIBLE);
        } else {
            pb_total.setVisibility(View.GONE);
        }
        //set


    }

    private void addTotalHarga(String harga, int kuantitas) {
        Integer hargaConv = Integer.valueOf(harga.replaceAll("[^0-9]", ""));

        String getHargaTotal = String.valueOf(tv_harga_total.getText());
        Integer sum;
        if (getHargaTotal != "") {
            Integer GethargaTotalConv = Integer.valueOf(getHargaTotal.replaceAll("[^0-9]", ""));
            sum = (hargaConv * kuantitas) + GethargaTotalConv;
        } else {
            sum = hargaConv * kuantitas;
        }
        //set
        tv_harga_total.setText("Rp. " + Currencyfy.currencyfy(sum, false, false));
        pb_total.setVisibility(View.GONE);
        tv_harga_total.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (adapterKeranjang != null) {
            adapterKeranjang.stopListening();
        }
    }

    private void initPopupViewControls(View v, Integer kuantitas) {
        //get layout
        LayoutInflater layoutInflater = LayoutInflater.from(v.getContext());
        popUpDialogView = layoutInflater.inflate(R.layout.popup_ubah_kuantitas, null);
        btn_kuantitas = popUpDialogView.findViewById(R.id.btn_kuantitas);
        btn_batal = popUpDialogView.findViewById(R.id.btn_cancel);
        btn_simpan = popUpDialogView.findViewById(R.id.btn_simpan);

        btn_kuantitas.setRange(1, 10000);
        btn_kuantitas.setNumber(String.valueOf(kuantitas));
    }

    private class KeranjangViewHolder extends RecyclerView.ViewHolder {
        private final View view;
        ImageView iv_foto;
        CheckBox cb_item;
        TextView tv_nama, tv_harga, tv_kuantitas, tv_ubah, tv_hapus;


        public KeranjangViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            iv_foto = itemView.findViewById(R.id.iv_foto);
            cb_item = itemView.findViewById(R.id.cb_pilih_item);
            tv_nama = itemView.findViewById(R.id.tv_nama);
            tv_harga = itemView.findViewById(R.id.tv_harga);
            tv_ubah = itemView.findViewById(R.id.tv_ubah);
            tv_kuantitas = itemView.findViewById(R.id.tv_kuantitas);
            tv_hapus = itemView.findViewById(R.id.tv_hapus);
        }


    }

}
