package com.ysf.oleholeh.ui.notifications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ysf.oleholeh.R;
import com.ysf.oleholeh.model.NotifikasiUserModel;
import com.ysf.oleholeh.ui.helper.Methods;

import java.text.SimpleDateFormat;
import java.util.Date;

public class NotificationsFragment extends Fragment implements View.OnClickListener {

    private ShimmerFrameLayout sl_notifikasi;
    private RecyclerView rv_notifikasi;
    private ImageView iv_not_found;

    private FirestoreRecyclerAdapter<NotifikasiUserModel, NotifikasiViewHolder> adapterNotifikasi;
    private CollectionReference ref_notif;
    private FirestoreRecyclerOptions options_notif_user;
    private StorageReference stRef_notif;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_notifications, container, false);

        init(root);
        seClickListener();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String idUser = currentUser.getUid();
        getListFavorite(idUser);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        sl_notifikasi.startShimmer();
        super.onViewCreated(view, savedInstanceState);
    }

    private void init(View root) {
        sl_notifikasi = root.findViewById(R.id.SL_notifikasi);
        rv_notifikasi = root.findViewById(R.id.rv_notifikasi);
        iv_not_found = root.findViewById(R.id.iv_not_found);

        ref_notif = FirebaseFirestore.getInstance().collection(getResources().getString(R.string.notif_user_ref));
        stRef_notif = FirebaseStorage.getInstance().getReference().child(getResources().getString(R.string.notif_user_ref));
    }

    private void seClickListener() {

    }

    private void getListFavorite(String idUser) {
        options_notif_user = new FirestoreRecyclerOptions.Builder<NotifikasiUserModel>()
                .setQuery(ref_notif.whereEqualTo("id_user", idUser), NotifikasiUserModel.class)
                .build();

        rv_notifikasi.setLayoutManager(new LinearLayoutManager(getActivity()));

        if (options_notif_user.getSnapshots() != null) {
            sl_notifikasi.stopShimmer();
            sl_notifikasi.setVisibility(View.GONE);
            rv_notifikasi.setVisibility(View.VISIBLE);
        } else {
            sl_notifikasi.stopShimmer();
            rv_notifikasi.setVisibility(View.GONE);
            sl_notifikasi.setVisibility(View.GONE);
            iv_not_found.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        Methods methods = new Methods();
        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a / d-MM-y");
        adapterNotifikasi = new FirestoreRecyclerAdapter<NotifikasiUserModel, NotifikasiViewHolder>(options_notif_user) {
            @Override
            protected void onBindViewHolder(@NonNull NotifikasiViewHolder holder, int i, @NonNull NotifikasiUserModel notifikasiUserModel) {

                if (!notifikasiUserModel.isIs_read()) {
                    holder.ll_item_notifikasi.setBackground(getResources().getDrawable(R.drawable.bg_notif_no_read));
                }

                methods.setImg(stRef_notif, holder.iv_notif, notifikasiUserModel.getFile_path());

                holder.tv_judul_notif.setText(notifikasiUserModel.getNama_notif());
                holder.tv_isi_notif.setText(notifikasiUserModel.getIsi_notif());

                // waktu
                Date timestamp = notifikasiUserModel.getWaktu_notif();
                holder.tv_waktu_notif.setText(dateFormat.format(timestamp));

                holder.ll_item_notifikasi.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ref_notif.document(getSnapshots().getSnapshot(i).getId()).update("is_read", true);
                    }
                });
            }

            @NonNull
            @Override
            public NotifikasiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notifikasi, parent, false);
                return new NotifikasiViewHolder(view);
            }
        };

        adapterNotifikasi.startListening();
        rv_notifikasi.setAdapter(adapterNotifikasi);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapterNotifikasi != null) {
            adapterNotifikasi.stopListening();
        }
    }

    @Override
    public void onClick(View v) {

    }

    private class NotifikasiViewHolder extends RecyclerView.ViewHolder {
        private final View view;
        ImageView iv_notif;
        TextView tv_judul_notif, tv_isi_notif, tv_waktu_notif;
        LinearLayout ll_item_notifikasi;

        public NotifikasiViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            iv_notif = itemView.findViewById(R.id.iv_notif);
            tv_judul_notif = itemView.findViewById(R.id.tv_judul_notif);
            tv_isi_notif = itemView.findViewById(R.id.tv_isi_notif);
            tv_waktu_notif = itemView.findViewById(R.id.tv_waktu_notif);
            ll_item_notifikasi = itemView.findViewById(R.id.ll_item_notifikasi);
        }
    }
}
