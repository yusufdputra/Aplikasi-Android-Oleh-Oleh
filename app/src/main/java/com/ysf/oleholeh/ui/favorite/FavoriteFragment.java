package com.ysf.oleholeh.ui.favorite;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.jpvs0101.currencyfy.Currencyfy;
import com.ysf.oleholeh.R;
import com.ysf.oleholeh.model.FavoriteModel;
import com.ysf.oleholeh.ui.helper.Methods;
import com.ysf.oleholeh.ui.home.DetailProdukFragment;

import pl.droidsonroids.gif.GifImageView;

import static com.ysf.oleholeh.ui.helper.KEY.KEY_ID;

public class FavoriteFragment extends Fragment implements View.OnClickListener {

    private ShimmerFrameLayout sl_favorite;
    private RecyclerView rv_favorite;
    private GifImageView iv_not_found;

    private FirestoreRecyclerAdapter<FavoriteModel, FavoriteViewHolder> adapterFavorite;
    private CollectionReference ref_produk, ref_fav;
    private FirestoreRecyclerOptions options_fav;
    private StorageReference stRef_produk;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_favorite, container, false);

        init(root);
        seClickListener();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String idUser = currentUser.getUid();
        getListFavorite(idUser);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        sl_favorite.startShimmer();
        super.onViewCreated(view, savedInstanceState);
    }

    private void init(View root) {
        sl_favorite = root.findViewById(R.id.SL_favorite);
        rv_favorite = root.findViewById(R.id.rv_favorite);
        iv_not_found = root.findViewById(R.id.iv_not_found);


        ref_fav = FirebaseFirestore.getInstance().collection(getResources().getString(R.string.favorite_ref));
        ref_produk = FirebaseFirestore.getInstance().collection(getResources().getString(R.string.produk_ref));
        stRef_produk = FirebaseStorage.getInstance().getReference().child(getResources().getString(R.string.produk_ref));
    }

    private void seClickListener() {

    }

    private void getListFavorite(String idUser) {
        options_fav = new FirestoreRecyclerOptions.Builder<FavoriteModel>()
                .setQuery(ref_fav.whereEqualTo("id_user", idUser), FavoriteModel.class)
                .build();

        rv_favorite.setLayoutManager(new GridLayoutManager(getContext(), 2));


        sl_favorite.stopShimmer();
    }

    @Override
    public void onStart() {
        super.onStart();
        adapterFavorite = new FirestoreRecyclerAdapter<FavoriteModel, FavoriteViewHolder>(options_fav) {
            @Override
            public void onDataChanged() {
                if (getItemCount() == 0) {
                    sl_favorite.setVisibility(View.GONE);
                    iv_not_found.setVisibility(View.VISIBLE);
                } else {
                    sl_favorite.setVisibility(View.GONE);
                    rv_favorite.setVisibility(View.VISIBLE);
                }
            }

            @Override
            protected void onBindViewHolder(@NonNull FavoriteViewHolder holder, int i, @NonNull FavoriteModel favoriteModel) {
                String idProduk = favoriteModel.getId_produk();
                holder.iv_favorit.setVisibility(View.VISIBLE);
                ref_produk.document(idProduk).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot doc = task.getResult();
                            if (doc.exists()) {
                                holder.tv_nama.setText(doc.get("nama").toString());
                                holder.tv_harga.setText("Rp. " + Currencyfy.currencyfy(Double.parseDouble(doc.get("harga").toString()), false, false));
                                Methods abs = new Methods();
                                abs.setImg(stRef_produk, holder.iv_produk, doc.get("foto_path").toString());
                                holder.tv_rating.setText(doc.get("rate_akum").toString());
                            }
                        }
                    }
                });

                holder.iv_favorit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ref_fav.document(getSnapshots().getSnapshot(i).getId()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                adapterFavorite.notifyItemRemoved(i);
                                adapterFavorite.notifyDataSetChanged();
                            }
                        });
                        ref_produk.document(idProduk).update("fav_akum", FieldValue.increment(-1));


                    }
                });
                holder.clProduk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle bundle = new Bundle();
                        bundle.putString(KEY_ID, favoriteModel.getId_produk());
                        DetailProdukFragment fragment = new DetailProdukFragment();
                        fragment.setArguments(bundle);
                        AppCompatActivity activity = (AppCompatActivity) v.getContext();
                        activity.getSupportFragmentManager().beginTransaction().replace(R.id.frame_container, fragment).addToBackStack(null).commit();
                    }
                });


            }

            @NonNull
            @Override
            public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_produk, parent, false);
                return new FavoriteViewHolder(view);
            }
        };


        adapterFavorite.startListening();
        rv_favorite.setAdapter(adapterFavorite);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapterFavorite != null) {
            adapterFavorite.stopListening();
        }
    }

    @Override
    public void onClick(View v) {

    }

    private class FavoriteViewHolder extends RecyclerView.ViewHolder {
        private final View view;
        ImageView iv_produk, iv_favorit;
        TextView tv_nama, tv_harga, tv_rating;
        ConstraintLayout clProduk;

        public FavoriteViewHolder(@NonNull View itemView) {
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
