package com.ysf.oleholeh.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.jpvs0101.currencyfy.Currencyfy;
import com.ysf.oleholeh.R;
import com.ysf.oleholeh.ui.helper.Methods;

import java.util.ArrayList;

public class ItemCheckOutAdapter extends RecyclerView.Adapter<ItemCheckOutAdapter.viewHolder> {
    private ArrayList<String> id_keranjang = new ArrayList<>();
    private final Context mContext;
    private CollectionReference ref_produk = FirebaseFirestore.getInstance().collection("produk") , ref_keranjang = FirebaseFirestore.getInstance().collection("keranjang");
    private StorageReference stRef_produk = FirebaseStorage.getInstance().getReference().child("produk");

    public ItemCheckOutAdapter(Context applicationContext, ArrayList<String> id_keranjang) {
        this.id_keranjang = id_keranjang;
        this.mContext = applicationContext;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_checkout, parent, false);

        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        ref_keranjang.document(id_keranjang.get(position)).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()){
                        holder.tv_kuantitas.setText(doc.get("kuantitas").toString() + " Pcs");
                        // get produk
                        ref_produk.document(doc.get("id_produk").toString()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot doc = task.getResult();
                                    if (doc.exists()) {
                                        holder.tv_nama.setText(doc.get("nama").toString());
                                        holder.tv_harga.setText("Rp. " + Currencyfy.currencyfy(Double.parseDouble(doc.get("harga").toString()), false, false));
                                        Methods abs = new Methods();
                                        abs.setImg(stRef_produk, holder.iv_foto, doc.get("foto_path").toString());

                                    }
                                }
                            }
                        });
                    }
                }

            }
        });
    }

    @Override
    public int getItemCount() {
        return id_keranjang.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder {
        ImageView iv_foto;
        TextView tv_nama, tv_harga, tv_kuantitas;
        public viewHolder(@NonNull View itemView) {
            super(itemView);
            iv_foto = itemView.findViewById(R.id.iv_foto);
            tv_nama = itemView.findViewById(R.id.tv_nama);
            tv_harga = itemView.findViewById(R.id.tv_harga);
            tv_kuantitas = itemView.findViewById(R.id.tv_kuantitas);
        }
    }
}
