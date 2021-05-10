package com.ysf.oleholeh.ui.pesananku.ui.main;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.jpvs0101.currencyfy.Currencyfy;
import com.ysf.oleholeh.R;
import com.ysf.oleholeh.ui.helper.Methods;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * <p>A fragment that shows a list of items as a modal bottom sheet.</p>
 * <p>You can show this modal bottom sheet from your activity like this:</p>
 * <pre>
 *     ItemListDiterimaDialogFragment.newInstance(30).show(getSupportFragmentManager(), "dialog");
 * </pre>
 */
public class ItemListDiterimaDialogFragment extends BottomSheetDialogFragment {

    // TODO: Customize parameter argument names
    private static final String ARG_ITEM_COUNT = "item_count";
    private static ArrayList<String> list_id_produk;
    List<RatingBar> list_rating;
    private static int banyak_data = 0;
    private static String id_pemesanan;
    private ExtendedFloatingActionButton btn_beri_nilai_semua;


    // TODO: Customize parameters
    public static ItemListDiterimaDialogFragment newInstance(int itemCount, ArrayList<String> getId_pemesanan, String getIdPemesanan) {
        final ItemListDiterimaDialogFragment fragment = new ItemListDiterimaDialogFragment();
        final Bundle args = new Bundle();
        args.putInt(ARG_ITEM_COUNT, itemCount);
        fragment.setArguments(args);

        list_id_produk = getId_pemesanan;
        banyak_data = itemCount;
        id_pemesanan = getIdPemesanan;


        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        if (list_rating != null){
            list_rating.clear();
        }else{
            list_rating = new ArrayList<>();
        }
        return inflater.inflate(R.layout.fragment_item_list_diterima_dialog_list_dialog, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        CollectionReference ref_pemesanan, ref_produk;
         ref_pemesanan = FirebaseFirestore.getInstance().collection(getResources().getString(R.string.pemesanan_ref));
         ref_produk = FirebaseFirestore.getInstance().collection(getResources().getString(R.string.produk_ref));
        final RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new ItemAdapter(getArguments().getInt(ARG_ITEM_COUNT)));

        btn_beri_nilai_semua = view.findViewById(R.id.btn_beri_nilai_semua);

        btn_beri_nilai_semua.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // cek
                int jumlah_rate = 0;
                for (int i = 0 ; i< list_rating.size(); i++){
                    if (list_rating.get(i).getRating() != 0){
                        jumlah_rate += 1;
                    }

                }
                if (jumlah_rate == banyak_data) {
                    ref_pemesanan.document(id_pemesanan).update("id_status_pemesanan", "5").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                // acum rate produk
                                for (int i = 0 ; i< list_rating.size(); i++){
                                    int finalI = i;
                                    Log.i(TAG, "list 1 "+ list_id_produk.get(i));

                                    ref_produk.document(list_id_produk.get(i)).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()){
                                                DocumentSnapshot doc = task.getResult();
                                                if (doc.exists()){
                                                    // get value lama
                                                    Double rate_lama = doc.getDouble("rate_akum");
                                                    // akum
                                                    float get_rate = list_rating.get(finalI).getRating();
                                                    Double rate_akhir = (rate_lama + get_rate)/2;
                                                    ref_produk.document(doc.getId()).update("rate_akum", rate_akhir);
                                                    Log.i(TAG, "list 2 "+ doc.getId());
                                                }
                                            }
                                        }
                                    });
                                }

                                dismiss();
                            }
                        }
                    });

                } else {
                    Toast toast =  Toast.makeText(getContext(), "Silahkan nilai untuk semua produk. Terimakasih. ", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }


            }
        });

    }

    private class ViewHolder extends RecyclerView.ViewHolder {

        final TextView tv_nama, tv_harga;
        final ImageView iv_foto;
        final RatingBar rb_produk;


        ViewHolder(LayoutInflater inflater, ViewGroup parent) {
            // TODO: Customize the item layout
            super(inflater.inflate(R.layout.fragment_item_list_diterima_dialog_list_dialog_item, parent, false));
            tv_nama = itemView.findViewById(R.id.tv_nama);
            tv_harga = itemView.findViewById(R.id.tv_harga);
            iv_foto = itemView.findViewById(R.id.iv_foto);
            rb_produk = itemView.findViewById(R.id.rb_produk);

        }
    }

    private class ItemAdapter extends RecyclerView.Adapter<ViewHolder> {

        private final int mItemCount;
        Methods methods = new Methods();
        private final CollectionReference ref_produk = FirebaseFirestore.getInstance().collection(getResources().getString(R.string.produk_ref));
        private final StorageReference strRef_Produk = FirebaseStorage.getInstance().getReference().child(getResources().getString(R.string.produk_ref));

        ItemAdapter(int itemCount) {
            mItemCount = itemCount;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {


            return new ViewHolder(LayoutInflater.from(parent.getContext()), parent);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            ref_produk.document(list_id_produk.get(position))
                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                            if (value.exists()) {
                                holder.tv_nama.setText(value.get("nama").toString());
                                holder.tv_harga.setText("Rp. " + Currencyfy.currencyfy(Double.parseDouble(value.get("harga").toString()), false, false));
                                methods.setImg(strRef_Produk, holder.iv_foto, value.get("foto_path").toString());
                            }
                        }
                    });

            list_rating.add(position, holder.rb_produk);

//            holder.rb_produk.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
//                @Override
//                public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
//
//                    if ((int) ratingBar.getRating() == 0) {
//                        list_rating.remove(position);
//                    } else {
//                        if (list_rating.size() != 0){
//                            if (list_rating.get(position) != null){
//                                list_rating.remove(position);
//                            }
//                        }
//                            list_rating.add(position, ratingBar.getRating());
//
//
//
//                    }
//
//                }
//            });


        }

        @Override
        public int getItemCount() {
            return mItemCount;
        }

    }

}