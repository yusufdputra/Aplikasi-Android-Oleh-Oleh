package com.ysf.oleholeh.ui.checkout;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.jpvs0101.currencyfy.Currencyfy;
import com.ysf.oleholeh.R;
import com.ysf.oleholeh.ui.helper.KEY;
import com.ysf.oleholeh.ui.helper.Methods;

public class PembayaranFragment extends Fragment implements View.OnClickListener {

    private String getId_pemesanan;
    private String getId_pembayaran;
    private double getHarga_Bayar;
    private ImageView iv_close, iv_logo_bank;
    private Button btn_kirim_bukti;
    private TextView tv_nama_bank, tv_atas_nama, tv_no_rek, tv_copy_norek, tv_copy_harga, tv_total_bayar;

    private CollectionReference ref_pembayarans, ref_keranjang, ref_detail_pemesanan;
    private StorageReference strRef_logo_bank;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_konfirmasi_tf, container, false);
        init(root);
        seClickListener();

        if (getArguments() != null) {
            getId_pemesanan = getArguments().getString(KEY.KEY_ID);
            if (getId_pemesanan == null){
                btn_kirim_bukti.setVisibility(View.GONE);
            }
            getId_pembayaran = getArguments().getString(KEY.KEY_ID_METODE_PEMBAYARAN);
            getHarga_Bayar = getArguments().getDouble(KEY.KEY_HARGA);


        }

        setDetailPembayaran();
        return root;
    }


    private void init(View root) {
        iv_close = root.findViewById(R.id.iv_close);
        iv_logo_bank = root.findViewById(R.id.iv_logo_bank);
        tv_nama_bank = root.findViewById(R.id.tv_nama_bank);
        tv_atas_nama = root.findViewById(R.id.tv_atas_nama);
        tv_no_rek = root.findViewById(R.id.tv_no_rek);
        tv_copy_norek = root.findViewById(R.id.tv_copy_norek);
        tv_copy_harga = root.findViewById(R.id.tv_copy_harga);
        tv_total_bayar = root.findViewById(R.id.tv_total_bayar);
        btn_kirim_bukti = root.findViewById(R.id.btn_kirim_bukti);

        ref_keranjang = FirebaseFirestore.getInstance().collection(getResources().getString(R.string.keranjang_ref));
        ref_detail_pemesanan = FirebaseFirestore.getInstance().collection(getResources().getString(R.string.detail_pemesanan_ref));
        ref_pembayarans = FirebaseFirestore.getInstance().collection(getResources().getString(R.string.pembayaran_ref));
        strRef_logo_bank = FirebaseStorage.getInstance().getReference().child(getResources().getString(R.string.logo_bank_ref));

    }

    private void seClickListener() {
        iv_close.setOnClickListener(this);
        tv_copy_norek.setOnClickListener(this);
        tv_copy_harga.setOnClickListener(this);
        btn_kirim_bukti.setOnClickListener(this);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.iv_close:
                getActivity().finish();
                break;
            case R.id.tv_copy_norek:
                doCopyText(tv_no_rek.getText().toString());
                break;
            case R.id.tv_copy_harga:
                // get harga total
                String harga = tv_total_bayar.getText().toString();
                Integer getHarga = Integer.valueOf(harga.replaceAll("[^0-9]", ""));
                doCopyText(String.valueOf(getHarga));
                break;
            case R.id.btn_kirim_bukti:
                String noHp_target = "6282385786314";
                String text = "Hai, Saya ingin mengkonfirmasi pembayaran dengan ID PEMESANAN: _*"+getId_pemesanan+"*_. Tolong segera diproses. Terimakasih.";

                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setData(Uri.parse("https://wa.me/"+noHp_target+"?text="+text));
                startActivity(intent);
                break;
        }
    }

    private void doCopyText(String text) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(text);
            Toast.makeText(getActivity(), "Berhasil disalin.", Toast.LENGTH_LONG).show();
        } else {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", text);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(getActivity(), "Berhasil disalin.", Toast.LENGTH_LONG).show();
        }
    }


    private void setDetailPembayaran() {
        // set bank
        ref_pembayarans.document(getId_pembayaran)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
                        // get ikon
                        String file_path = doc.get("ikon").toString();
                        Methods methods = new Methods();
                        methods.setImg(strRef_logo_bank, iv_logo_bank, file_path);

                        tv_nama_bank.setText(doc.get("nama_bank").toString());
                        tv_atas_nama.setText(doc.get("atas_nama").toString());
                        tv_no_rek.setText(doc.get("rekening").toString());
                    }
                }
            }
        });

        //set nominal transfer
        tv_total_bayar.setText(Currencyfy.currencyfy(getHarga_Bayar, false, false));
    }

    @Override
    public void onStart() {
        super.onStart();

    }
}
