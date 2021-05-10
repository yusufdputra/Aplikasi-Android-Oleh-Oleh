package com.ysf.oleholeh.ui.pencarian;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.jpvs0101.currencyfy.Currencyfy;
import com.ysf.oleholeh.R;
import com.ysf.oleholeh.adapter.SearchingAdapter;
import com.ysf.oleholeh.model.ProduksModel;
import com.ysf.oleholeh.ui.helper.Methods;
import com.ysf.oleholeh.ui.home.DetailProdukFragment;

import java.util.Collection;

import static com.ysf.oleholeh.ui.helper.KEY.KEY_ID;
import static com.ysf.oleholeh.ui.helper.KEY.KEY_NAMA;

public class CariByKategoriFragment extends Fragment implements View.OnClickListener {

    private RecyclerView rv_produk;
    private ShimmerFrameLayout sl_produk;

    private ImageView iv_back, iv_cari, iv_empty_list;
    private TextView tv_hasil, tv_by_nama, tv_jumlah;
    private TextInputEditText et_search_key;
    private TextInputLayout tl_cari;

    private String id_kategori, nama_kategori;

    private FirestoreRecyclerOptions produk_option;
    private FirestoreRecyclerAdapter<ProduksModel, ProdukViewHolder> adapterProduk;
    private CollectionReference ref_produk;
    private StorageReference stRef_produk;
    private Query query;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_cari_by_kategori, container, false);
        init(root);
        seClickListener();

        if (getArguments() != null) {
            id_kategori = getArguments().getString(KEY_ID);
            nama_kategori = getArguments().getString(KEY_NAMA);

            getDetailKategori();
            getProdukByKategori();

            et_search_key.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)){
                        SearchProdukByName(et_search_key.getText().toString().toLowerCase());
                    }
                    return false;
                }
            });
        }

        return root;
    }




    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        sl_produk.startShimmer();
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapterProduk != null) {
            adapterProduk.stopListening();
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.iv_back:
                getActivity().onBackPressed();
                break;
            case R.id.iv_cari:
                tl_cari.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void init(View root) {
        iv_back = root.findViewById(R.id.iv_back);
        iv_cari = root.findViewById(R.id.iv_cari);
        tv_hasil = root.findViewById(R.id.tv_hasil);
        tv_by_nama = root.findViewById(R.id.tv_cari_nama);
        tv_jumlah = root.findViewById(R.id.tv_jumlah);
        rv_produk = root.findViewById(R.id.rv_produk);
        sl_produk = root.findViewById(R.id.sl_produk);
        et_search_key = root.findViewById(R.id.et_search_key);
        tl_cari = root.findViewById(R.id.tl_cari);
        iv_empty_list = root.findViewById(R.id.iv_empty);

        stRef_produk = FirebaseStorage.getInstance().getReference().child(getResources().getString(R.string.produk_ref));
        ref_produk = FirebaseFirestore.getInstance().collection(getResources().getString(R.string.produk_ref));
    }

    private void seClickListener() {
        iv_back.setOnClickListener(this);
        iv_cari.setOnClickListener(this);
    }

    private void getDetailKategori() {
        tv_hasil.setText("Hasil Untuk '" + nama_kategori + "' ");

        ref_produk.whereEqualTo("kategori", id_kategori).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    int jumlah_produk = task.getResult().size();
                    tv_jumlah.setText("(" + jumlah_produk + " Produk)");
                }
            }
        });


    }

    private void getProdukByKategori() {

        produk_option = new FirestoreRecyclerOptions.Builder<ProduksModel>()
                .setQuery(ref_produk.whereEqualTo("kategori", id_kategori), ProduksModel.class)
                .build();

        rv_produk.setLayoutManager(new GridLayoutManager(getContext(), 2));

        setAdapterProduk(produk_option);
    }

    private void SearchProdukByName(String key) {
        sl_produk.stopShimmer();
        sl_produk.setVisibility(View.VISIBLE);
        produk_option = new FirestoreRecyclerOptions.Builder<ProduksModel>()
                .setQuery(ref_produk.whereEqualTo("kategori", id_kategori)
                        .whereGreaterThanOrEqualTo("nama_low_case", key)
                        .whereLessThanOrEqualTo("nama_low_case", key+"\uF7FF"), ProduksModel.class)
                .build();

        rv_produk.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rv_produk.setVisibility(View.VISIBLE);

        ref_produk.whereEqualTo("kategori", id_kategori).orderBy("nama").startAt(key).endAt(key+ "\uf8ff").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {

            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    int jumlah_produk = task.getResult().size();
                    tv_jumlah.setText(" (" + jumlah_produk + " Produk)");
                }
            }
        });

        tv_by_nama.setText(" "+key);

        setAdapterProduk(produk_option);
    }



    private class ProdukViewHolder extends RecyclerView.ViewHolder {
        private final View view;
        ImageView iv_produk, iv_favorit;
        TextView tv_nama, tv_harga, tv_rating;
        ConstraintLayout clProduk;

        public ProdukViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            iv_produk = itemView.findViewById(R.id.iv_foto);
            iv_favorit = itemView.findViewById(R.id.iv_favorit);
            tv_nama = itemView.findViewById(R.id.tv_nama);
            tv_harga = itemView.findViewById(R.id.tv_harga);
            tv_rating = itemView.findViewById(R.id.tv_rating);
            clProduk = itemView.findViewById(R.id.CL_Poduk);
        }
    }

    private void setAdapterProduk(FirestoreRecyclerOptions produk_option){
        Methods methods = new Methods();
        adapterProduk = new FirestoreRecyclerAdapter<ProduksModel, ProdukViewHolder>(produk_option) {
            @Override
            public void onDataChanged() {
                if (getItemCount() == 0){
                    iv_empty_list.setVisibility(View.VISIBLE);
                    rv_produk.setVisibility(View.GONE);
                }else{
                    iv_empty_list.setVisibility(View.GONE);
                    tv_jumlah.setText(getItemCount()+" Produk");
                    rv_produk.setVisibility(View.VISIBLE);

                }

            }
            @Override
            protected void onBindViewHolder(@NonNull ProdukViewHolder holder, int i, @NonNull ProduksModel produksModel) {

                holder.tv_nama.setText(produksModel.getNama());
                holder.tv_harga.setText("Rp. " + Currencyfy.currencyfy(produksModel.getHarga(), false, false));
                holder.tv_rating.setText(produksModel.getRate_akum().toString());

                methods.setImg(stRef_produk, holder.iv_produk, produksModel.getFoto_path());

                holder.clProduk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle bundle = new Bundle();
                        bundle.putString(KEY_ID, getSnapshots().getSnapshot(i).getId());
                        DetailProdukFragment fragment = new DetailProdukFragment();
                        fragment.setArguments(bundle);
                        AppCompatActivity activity = (AppCompatActivity) v.getContext();
                        activity.getSupportFragmentManager().beginTransaction().replace(R.id.frame_container, fragment).addToBackStack(null).commit();
                    }
                });
            }

            @NonNull
            @Override
            public ProdukViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_produk, parent, false);
                return new ProdukViewHolder(view);
            }
        };

        adapterProduk.startListening();
        rv_produk.setAdapter(adapterProduk);
        sl_produk.stopShimmer();
        sl_produk.setVisibility(View.GONE);
    }
}
