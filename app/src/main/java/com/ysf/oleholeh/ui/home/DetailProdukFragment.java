package com.ysf.oleholeh.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.jpvs0101.currencyfy.Currencyfy;
import com.ysf.oleholeh.R;
import com.ysf.oleholeh.model.FavoriteModel;
import com.ysf.oleholeh.model.KeranjangModel;
import com.ysf.oleholeh.model.ProduksModel;
import com.ysf.oleholeh.ui.auth.LoginActivity;
import com.ysf.oleholeh.ui.helper.Methods;
import com.ysf.oleholeh.ui.keranjang.KeranjangActivity;

import static com.ysf.oleholeh.ui.helper.KEY.KEY_ID;

public class DetailProdukFragment extends Fragment implements View.OnClickListener {
    Methods methods = new Methods();
    private FirestoreRecyclerAdapter<ProduksModel, ProdukViewHolder> adapterProduk;
    private CollectionReference ref_produk, ref_keranjang, ref_fovorite;
    private FirestoreRecyclerOptions options_produk;
    private StorageReference stRef_produk;

    private ShimmerFrameLayout sl_head_produk, sl_rekomendasi, sl_detail;
    private LinearLayout LL_head_produk;
    private ImageView iv_back, iv_img_produk, iv_favorit, iv_keranjang;
    private Button btn_beli;

    private String id_produk;
    private TextView tv_title,tv_rating, tv_nama_produk, tv_harga, tv_stok, tv_produksi, tv_rasa, tv_keterangan, tv_fav_akum, tv_total_keranjang, tv_terjual;
    private RatingBar rb_Rating;

    private RecyclerView rv_rekomendasi;

    private TableLayout tl_detail;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_detail_produk, container, false);

        init(root);
        seClickListener();

        if (getArguments() != null) {
            id_produk = getArguments().getString(KEY_ID);

            getDetailProduk();
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String idUser = currentUser.getUid();
            getFavoriteStatus(idUser);
            getBanyakKeranjang(idUser);
        }
        getListRekomendasi();


        return root;
    }


    private void init(View root) {
        iv_back = root.findViewById(R.id.iv_back);
        sl_head_produk = root.findViewById(R.id.SL_head_produk);
        sl_rekomendasi = root.findViewById(R.id.SL_rekom);
        sl_detail = root.findViewById(R.id.sl_detail);
        iv_keranjang = root.findViewById(R.id.iv_keranjang);

        rv_rekomendasi = root.findViewById(R.id.rv_rekomendasi);

        LL_head_produk = root.findViewById(R.id.LL_head_produk);

        tv_title = root.findViewById(R.id.tv_title);
        iv_img_produk = root.findViewById(R.id.iv_foto);
        tv_nama_produk = root.findViewById(R.id.tv_nama);
        tv_harga = root.findViewById(R.id.tv_harga);
        tv_rating = root.findViewById(R.id.tv_rating);
        iv_favorit = root.findViewById(R.id.iv_favorit);
        tv_fav_akum = root.findViewById(R.id.tv_fav_akum);
        tl_detail = root.findViewById(R.id.tl_detail);
        tv_terjual = root.findViewById(R.id.tv_terjual);
        tv_stok = root.findViewById(R.id.tv_stok);
        tv_produksi = root.findViewById(R.id.tv_produksi);
        tv_rasa = root.findViewById(R.id.tv_rasa);
        tv_keterangan = root.findViewById(R.id.tv_detail_desk);
        tv_total_keranjang = root.findViewById(R.id.tv_total_keranjang);

        ref_keranjang = FirebaseFirestore.getInstance().collection(getResources().getString(R.string.keranjang_ref));
        ref_fovorite = FirebaseFirestore.getInstance().collection(getResources().getString(R.string.favorite_ref));
        ref_produk = FirebaseFirestore.getInstance().collection(getResources().getString(R.string.produk_ref));
        stRef_produk = FirebaseStorage.getInstance().getReference().child(getResources().getString(R.string.produk_ref));

        btn_beli = root.findViewById(R.id.btn_beli);
    }


    private void seClickListener() {
        iv_back.setOnClickListener(this);
        btn_beli.setOnClickListener(this);
        iv_favorit.setOnClickListener(this);
        iv_keranjang.setOnClickListener(this);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        sl_rekomendasi.startShimmer();
        sl_head_produk.startShimmer();
        sl_detail.startShimmer();
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        Intent intent = null;
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        switch (id) {
            case R.id.iv_back:
                getActivity().onBackPressed();
                break;
            case R.id.btn_beli:
                if (currentUser != null) {
                    String idUser = currentUser.getUid();
                    tambahKeranjang(v, idUser);
                } else {
                    intent = new Intent(getActivity(), LoginActivity.class);
                    startActivity(intent);
                }
                break;
            case R.id.iv_keranjang:
                if (currentUser != null) {
                    intent = new Intent(getActivity(), KeranjangActivity.class);
                    startActivity(intent);
                } else {
                    intent = new Intent(getActivity(), LoginActivity.class);
                    startActivity(intent);
                }

                break;
            case R.id.iv_favorit:
                if (currentUser != null) {
                    String idUser = currentUser.getUid();
                    tambahFavorite(v, idUser);
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

    private void getFavoriteStatus(String idUser) {
        ref_fovorite.whereEqualTo("id_produk", id_produk)
                .whereEqualTo("id_user", idUser)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    boolean already = task.getResult().isEmpty();
                    if (!already) {
                        iv_favorit.setImageResource(R.drawable.ic_baseline_favorite_check_24);
                    } else {
                        iv_favorit.setImageResource(R.drawable.ic_baseline_favorite_border_24);
                    }
                }
            }
        });
    }

    private void tambahFavorite(View v, String idUser) {
        ref_fovorite.whereEqualTo("id_user", idUser)
                .whereEqualTo("id_produk", id_produk)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            boolean already = task.getResult().isEmpty();
                            if (!already) {
                                for (QueryDocumentSnapshot doc : task.getResult()) {
                                    String id_fav = doc.getId();
                                    ref_fovorite.document(id_fav).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            ref_produk.document(id_produk).update("fav_akum", FieldValue.increment(-1));
                                            iv_favorit.setImageResource(R.drawable.ic_baseline_favorite_border_24);

                                        }
                                    });
                                }
                            } else {
                                iv_favorit.setImageResource(R.drawable.ic_baseline_favorite_check_24);
                                FavoriteModel value = new FavoriteModel(
                                        idUser,
                                        id_produk
                                );
                                ref_fovorite.add(value).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentReference> task) {
                                        ref_produk.document(id_produk).update("fav_akum", FieldValue.increment(1));
                                    }
                                });

                            }
                        }
                    }
                });
    }

    private void tambahKeranjang(View view, String idUser) {
        // cek apakah sudah ada atau belum
        ref_keranjang.whereEqualTo("id_user", idUser)
                .whereEqualTo("id_produk", id_produk)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            boolean already = task.getResult().isEmpty();
                            if (!already) {
                                for (QueryDocumentSnapshot doc : task.getResult()) {
                                    String id_keranjang = doc.getId();
                                    ref_keranjang.document(id_keranjang).update("kuantitas", FieldValue.increment(1));
                                    Toast.makeText(getActivity(), "Berhasil Menambahkan Ke Keranjang", Toast.LENGTH_LONG).show();
                                }
                            } else {
                                KeranjangModel value = new KeranjangModel(
                                        idUser,
                                        id_produk,
                                        1,
                                        false
                                );
                                ref_keranjang.add(value).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentReference> task) {
                                        Toast.makeText(getActivity(), "Berhasil Menambahkan Ke Keranjang", Toast.LENGTH_LONG).show();
                                    }
                                });

                            }
                        }
                    }
                });
    }


    private void getDetailProduk() {

        ref_produk.document(id_produk).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {

                if (value.exists()){

                    if (Long.parseLong(value.get("stok").toString()) != 0){
                        btn_beli.setEnabled(true);
                    }

                    methods.setImg(stRef_produk, iv_img_produk, value.get("foto_path").toString());
                    tv_title.setText(value.get("nama").toString());
                    tv_nama_produk.setText(value.get("nama").toString());
                    tv_harga.setText("Rp. " + Currencyfy.currencyfy(Double.parseDouble(value.get("harga").toString()), false, false));
                    tv_stok.setText(value.get("stok").toString()+ " Pcs");
                    tv_produksi.setText(value.get("produksi").toString());
                    tv_rasa.setText(value.get("rasa").toString());
                    tv_keterangan.setText(value.get("deskripsi").toString());
                    tv_rating.setText(String.valueOf(value.get("rate_akum")));
                    tv_fav_akum.setText(value.get("fav_akum").toString() + " Menyukai");
                    tv_terjual.setText(value.get("terjual_akum").toString() + " Pcs");
                    tl_detail.setVisibility(View.VISIBLE);
                    tv_keterangan.setVisibility(View.VISIBLE);
                    LL_head_produk.setVisibility(View.VISIBLE);



                }
            }
        });


        sl_head_produk.stopShimmer();
        sl_head_produk.setVisibility(View.GONE);

        sl_detail.stopShimmer();
        sl_detail.setVisibility(View.GONE);



    }

    private void getListRekomendasi() {
        options_produk = new FirestoreRecyclerOptions.Builder<ProduksModel>()
                .setQuery(ref_produk.orderBy("terjual_akum", com.google.firebase.firestore.Query.Direction.DESCENDING).limit(20), ProduksModel.class)
                .build();
        rv_rekomendasi.setLayoutManager(new GridLayoutManager(getContext(), 2));

        sl_rekomendasi.stopShimmer();
        sl_rekomendasi.setVisibility(View.GONE);
        rv_rekomendasi.setVisibility(View.VISIBLE);

    }

    @Override
    public void onStart() {
        super.onStart();


        adapterProduk = new FirestoreRecyclerAdapter<ProduksModel, ProdukViewHolder>(options_produk) {
            @Override
            protected void onBindViewHolder(@NonNull ProdukViewHolder holder, int i, @NonNull ProduksModel produksModel) {
                holder.tv_nama.setText(produksModel.getNama());
                holder.tv_harga.setText("Rp. " + Currencyfy.currencyfy(produksModel.getHarga(), false, false));
                holder.tv_rating.setText(String.valueOf(produksModel.getRate_akum()));

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
        rv_rekomendasi.setAdapter(adapterProduk);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapterProduk != null) {
            adapterProduk.stopListening();
        }

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
