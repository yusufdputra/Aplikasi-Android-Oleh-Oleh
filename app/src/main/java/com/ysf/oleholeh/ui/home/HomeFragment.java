package com.ysf.oleholeh.ui.home;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ahmadrosid.svgloader.SvgLoader;
import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.jpvs0101.currencyfy.Currencyfy;
import com.ysf.oleholeh.R;
import com.ysf.oleholeh.model.KategoriProdukModel;
import com.ysf.oleholeh.model.ProduksModel;
import com.ysf.oleholeh.ui.auth.LoginActivity;
import com.ysf.oleholeh.ui.helper.Methods;
import com.ysf.oleholeh.ui.keranjang.KeranjangActivity;
import com.ysf.oleholeh.ui.pencarian.CariByKategoriFragment;
import com.ysf.oleholeh.ui.pencarian.CariByNamaFragment;

import java.util.ArrayList;
import java.util.List;

import static com.ysf.oleholeh.ui.helper.KEY.KEY_ID;
import static com.ysf.oleholeh.ui.helper.KEY.KEY_NAMA;

public class HomeFragment extends Fragment implements View.OnClickListener {

    Methods methods = new Methods();
    private TextView tv_total_keranjang;

    private FirestoreRecyclerAdapter<ProduksModel, ProdukViewHolder> adapterProduk;
    private FirestoreRecyclerAdapter<KategoriProdukModel, KategoriViewHolder> adapterKategori;
    private FirebaseFirestore db_banners;

    private EditText et_searching;
    private ImageView iv_home, iv_keranjang;
    private ImageSlider IS_banner;
    private RecyclerView rv_kategori, rv_produk;
    private ShimmerFrameLayout SL_ktg, SL_banner, SL_rekomendasi;

    private CollectionReference ref_produk, ref_kategori,ref_keranjang;
    private FirestoreRecyclerOptions options_ktg, options_produk;
    private StorageReference stRef_produk, stRef_kategori, strRef_banner;
    Bundle bundle = new Bundle();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_home, container, false);
        init(root);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null){
            String idUser = currentUser.getUid();
            getBanyakKeranjang(idUser);
        }


        seClickListener();
        getListKategori();
        getListBanner();
        getListRekomendasi();

        SL_ktg.stopShimmer();
        SL_ktg.setVisibility(View.GONE);

        SL_banner.stopShimmer();
        SL_banner.setVisibility(View.GONE);

        SL_rekomendasi.stopShimmer();
        SL_rekomendasi.setVisibility(View.GONE);


        return root;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        SL_ktg.startShimmer();
        SL_banner.startShimmer();
        SL_rekomendasi.startShimmer();


        super.onViewCreated(view, savedInstanceState);
    }

    private void seClickListener() {

        iv_keranjang.setOnClickListener(this);

        et_searching.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)){
                    bundle.putString(KEY_NAMA, et_searching.getText().toString().toLowerCase());
                    CariByNamaFragment fragment = new CariByNamaFragment();
                    fragment.setArguments(bundle);
                    AppCompatActivity activity = (AppCompatActivity) v.getContext();
                    activity.getSupportFragmentManager().beginTransaction().replace(R.id.frame_container, fragment).addToBackStack(null).commit();
                }
                return false;
            }
        });

    }

    private void init(View root) {
        et_searching = root.findViewById(R.id.et_searching);
//        sv_searching.setQueryHint("Cari disini...");
        iv_keranjang = root.findViewById(R.id.iv_keranjang);
        IS_banner = root.findViewById(R.id.is_slider);
        rv_kategori = root.findViewById(R.id.rv_kategori);
        rv_produk = root.findViewById(R.id.rv_rekomendasi);
        tv_total_keranjang = root.findViewById(R.id.tv_total_keranjang);

        db_banners = FirebaseFirestore.getInstance();

        ref_produk = FirebaseFirestore.getInstance().collection(getResources().getString(R.string.produk_ref));
        ref_kategori = FirebaseFirestore.getInstance().collection(getResources().getString(R.string.ktg_ref));
        ref_keranjang = FirebaseFirestore.getInstance().collection(getResources().getString(R.string.keranjang_ref));

        stRef_produk = FirebaseStorage.getInstance().getReference().child(getResources().getString(R.string.produk_ref));
        stRef_kategori = FirebaseStorage.getInstance().getReference().child(getResources().getString(R.string.ktg_ref));
        strRef_banner = FirebaseStorage.getInstance().getReference().child(getResources().getString(R.string.banner_ref));

        SL_ktg = root.findViewById(R.id.SL_kategori);
        SL_banner = root.findViewById(R.id.SL_banner);
        SL_rekomendasi = root.findViewById(R.id.SL_rekom);

    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        Intent intent = null;
        FirebaseUser currentUser;
        switch (id) {
            case R.id.iv_menu:
                break;

            case R.id.iv_keranjang:
                currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null) {
                    intent = new Intent(getActivity(), KeranjangActivity.class);
                    startActivity(intent);
                } else {
                    intent = new Intent(getActivity(), LoginActivity.class);
                    startActivity(intent);
                }

                break;
        }

    }

    private void getBanyakKeranjang(String idUser) {
        ref_keranjang.whereEqualTo("id_user", idUser).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                int doc = value.size();
                tv_total_keranjang.setText(String.valueOf(doc));
            }
        });
    }

    private void getListBanner() {
        List<SlideModel> slideModels = new ArrayList<>();
        db_banners.collection(getResources().getString(R.string.banner_ref))
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        String foto_path = doc.get("foto_path").toString();
                        strRef_banner.child(foto_path)
                                .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                slideModels.add(new SlideModel(uri.toString(), ScaleTypes.CENTER_CROP));
                                IS_banner.setImageList(slideModels, ScaleTypes.CENTER_CROP);
                            }
                        });

                    }
                }
            }
        });


        IS_banner.setVisibility(View.VISIBLE);

    }

    private void getListRekomendasi() {
        options_produk = new FirestoreRecyclerOptions.Builder<ProduksModel>()
                .setQuery(ref_produk.orderBy("rate_akum", Query.Direction.ASCENDING).limit(20), ProduksModel.class)
                .build();
        rv_produk.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rv_produk.setVisibility(View.VISIBLE);

    }

    private void getListKategori() {

        options_ktg = new FirestoreRecyclerOptions.Builder<KategoriProdukModel>()
                .setQuery(ref_kategori.orderBy("nama", Query.Direction.ASCENDING), KategoriProdukModel.class)
                .build();

        rv_kategori.setLayoutManager(new GridLayoutManager(getContext(), 1, GridLayoutManager.HORIZONTAL, false));


        rv_kategori.setVisibility(View.VISIBLE);

    }

    @Override
    public void onStart() {
        super.onStart();

        adapterKategori = new FirestoreRecyclerAdapter<KategoriProdukModel, KategoriViewHolder>(options_ktg) {
            @Override
            protected void onBindViewHolder(@NonNull KategoriViewHolder holder, int i, @NonNull KategoriProdukModel kategoriProdukModel) {
                holder.tv_kategori.setText(kategoriProdukModel.getNama());

                methods.setSVG(stRef_kategori, holder.iv_foto, kategoriProdukModel.getFoto_path(), getActivity());

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bundle.putString(KEY_ID, getSnapshots().getSnapshot(i).getId());
                        bundle.putString(KEY_NAMA, kategoriProdukModel.getNama());
                        CariByKategoriFragment fragment = new CariByKategoriFragment();
                        fragment.setArguments(bundle);
                        AppCompatActivity activity = (AppCompatActivity) v.getContext();
                        activity.getSupportFragmentManager().beginTransaction().replace(R.id.frame_container, fragment).addToBackStack(null).commit();
                    }
                });

            }

            @NonNull
            @Override
            public KategoriViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_kategori, parent, false);
                return new KategoriViewHolder(view);
            }
        };



        adapterProduk = new FirestoreRecyclerAdapter<ProduksModel, ProdukViewHolder>(options_produk) {
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

        adapterKategori.startListening();
        adapterProduk.startListening();

        rv_kategori.setAdapter(adapterKategori);
        rv_produk.setAdapter(adapterProduk);
    }


    @Override
    public void onStop() {
        super.onStop();
        if (adapterProduk != null) {
            adapterProduk.stopListening();
        }
        if (adapterKategori != null) {
            adapterKategori.stopListening();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SvgLoader.pluck().close();
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

    private class KategoriViewHolder extends RecyclerView.ViewHolder {
        private final View view;
        ImageView iv_foto;
        TextView tv_kategori;

        public KategoriViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            iv_foto = itemView.findViewById(R.id.iv_foto_ktg);
            tv_kategori = itemView.findViewById(R.id.tv_ktg);
        }
    }
}

