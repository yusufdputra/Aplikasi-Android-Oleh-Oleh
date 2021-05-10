package com.ysf.oleholeh.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ysf.oleholeh.R;

import java.util.ArrayList;

public class CustomSpinnerBankAdapter extends BaseAdapter {
    private ArrayList<String> list_id_pembayaran;
    private ArrayList<String> list_nama_pembayaran;
    private Context mcontext;
    private LayoutInflater layoutInflater;

    public CustomSpinnerBankAdapter(Context applicationContext, ArrayList<String> id_list_bank, ArrayList<String> list_bank) {
        this.list_id_pembayaran = id_list_bank;
        this.list_nama_pembayaran = list_bank;
        this.mcontext = applicationContext;
        this.layoutInflater = LayoutInflater.from(applicationContext);
    }


    @Override
    public int getCount() {
        return list_id_pembayaran.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = layoutInflater.inflate(R.layout.spinner_pembayarans, null);
        TextView nama_bank = (TextView) convertView.findViewById(R.id.tv_nama_bank);

        nama_bank.setText(list_nama_pembayaran.get(position));

        return convertView;
    }
}
