package com.ysf.oleholeh.ui.pencarian;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.jpvs0101.currencyfy.Currencyfy;
import com.ysf.oleholeh.R;
import com.ysf.oleholeh.model.ProduksModel;
import com.ysf.oleholeh.ui.helper.Methods;
import com.ysf.oleholeh.ui.home.DetailProdukFragment;

import static com.ysf.oleholeh.ui.helper.KEY.KEY_ID;
import static com.ysf.oleholeh.ui.helper.KEY.KEY_NAMA;

public class CariByNamaFragment extends Fragment implements View.OnClickListener {

    private int status_filter = 0;
    private ImageView iv_back, iv_empty_list;
    private EditText et_cari;
    private TextView tv_cariNama, tv_jumlah, tv_filter;
    private RecyclerView rv_produk;
    private ShimmerFrameLayout sl_produk;

    private FirestoreRecyclerOptions produk_option;
    private FirestoreRecyclerAdapter<ProduksModel, ProdukViewHolder> adapterProduk;
    private CollectionReference ref_produk;
    private StorageReference stRef_produk;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_cari_by_name, container, false);
        init(root);
        seClickListener();

        if (getArguments() != null) {
            String nama_cari = getArguments().getString(KEY_NAMA);
            et_cari.setText(nama_cari);
            tv_cariNama.setText(" '" + nama_cari + "' ");
            getProdukByNama(nama_cari);

        }

        return root;
    }


    private void init(View root) {
        iv_back = root.findViewById(R.id.iv_back);
        et_cari = root.findViewById(R.id.et_searching);
        tv_cariNama = root.findViewById(R.id.tv_cari_nama);
        tv_jumlah = root.findViewById(R.id.tv_jumlah);
        tv_filter = root.findViewById(R.id.tv_filter);
        rv_produk = root.findViewById(R.id.rv_produk);
        sl_produk = root.findViewById(R.id.sl_produk);
        iv_empty_list = root.findViewById(R.id.iv_empty);

        stRef_produk = FirebaseStorage.getInstance().getReference().child(getResources().getString(R.string.produk_ref));
        ref_produk = FirebaseFirestore.getInstance().collection(getResources().getString(R.string.produk_ref));
    }

    private void seClickListener() {
        iv_back.setOnClickListener(this);
        tv_filter.setOnClickListener(this);
        et_cari.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    String input = et_cari.getText().toString().toLowerCase();
                    tv_cariNama.setText(" '" + input + "' ");
                    getProdukByNama(input);
                    adapterProduk.notifyDataSetChanged();
                }
                return false;
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.iv_back:
                getActivity().onBackPressed();
                break;
            case R.id.tv_filter:
                if (status_filter == 0) {
                    Filter(Query.Direction.ASCENDING);
                    status_filter = 1;
                    tv_filter.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_arrow_down_filter, 0, 0, 0);
                } else if (status_filter == 1) {
                    Filter(Query.Direction.ASCENDING);
                    status_filter = 0;
                    tv_filter.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_arrow_up_filter, 0, 0, 0);
                }
                break;

        }
    }

    private void Filter(Query.Direction filter) {
        sl_produk.startShimmer();
        sl_produk.setVisibility(View.VISIBLE);
        String input = et_cari.getText().toString().toLowerCase();
        produk_option = new FirestoreRecyclerOptions.Builder<ProduksModel>()
                .setQuery(ref_produk
                                .whereGreaterThan("harga", 9999999)
                                .orderBy("harga", filter)
                                .whereGreaterThanOrEqualTo("nama_low_case", input)
                                .whereLessThanOrEqualTo("nama_low_case", input + "\uF7FF")
                        , ProduksModel.class)
                .build();


        rv_produk.setLayoutManager(new GridLayoutManager(getContext(), 2));
        sl_produk.stopShimmer();
        sl_produk.setVisibility(View.GONE);

        setAdapterProduk(produk_option);
    }


    private void getProdukByNama(String nama_cari) {
        status_filter = 0;
        tv_filter.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        sl_produk.startShimmer();
        sl_produk.setVisibility(View.VISIBLE);
        produk_option = new FirestoreRecyclerOptions.Builder<ProduksModel>()
                .setQuery(ref_produk.whereGreaterThanOrEqualTo("nama_low_case", nama_cari)
                        .whereLessThanOrEqualTo("nama_low_case", nama_cari + "\uF7FF"), ProduksModel.class)
                .build();


        rv_produk.setLayoutManager(new GridLayoutManager(getContext(), 2));
        sl_produk.stopShimmer();
        sl_produk.setVisibility(View.GONE);

        setAdapterProduk(produk_option);
    }

    private void setAdapterProduk(FirestoreRecyclerOptions produk_option) {
        Methods methods = new Methods();
        adapterProduk = new FirestoreRecyclerAdapter<ProduksModel, ProdukViewHolder>(produk_option) {
            @Override
            public void onDataChanged() {
                if (getItemCount() == 0) {
                    iv_empty_list.setVisibility(View.VISIBLE);
                    rv_produk.setVisibility(View.GONE);
                    tv_jumlah.setText(getItemCount() + " Produk");
                } else {
                    iv_empty_list.setVisibility(View.GONE);
                    tv_jumlah.setText(getItemCount() + " Produk");
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
}
