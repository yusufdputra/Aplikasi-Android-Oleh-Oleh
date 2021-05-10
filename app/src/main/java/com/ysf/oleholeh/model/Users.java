package com.ysf.oleholeh.model;

import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;

public class Users {
    String nama, email, nomor_hp, foto_path;
    String list_alamat;
    Float transaksi_akum;
    GeoPoint kordinat_alamat;

    public Users(String nama, String email, String nomor_hp, String foto_path, String list_alamat,  Float transaksi_akum, GeoPoint kordinat_alamat) {
        this.nama = nama;
        this.email = email;
        this.nomor_hp = nomor_hp;
        this.foto_path = foto_path;
        this.list_alamat = list_alamat;
        this.transaksi_akum = transaksi_akum;
        this.kordinat_alamat = kordinat_alamat;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNomor_hp() {
        return nomor_hp;
    }

    public void setNomor_hp(String nomor_hp) {
        this.nomor_hp = nomor_hp;
    }

    public String getFoto_path() {
        return foto_path;
    }

    public void setFoto_path(String foto_path) {
        this.foto_path = foto_path;
    }

    public String getList_alamat() {
        return list_alamat;
    }

    public void setList_alamat(String list_alamat) {
        this.list_alamat = list_alamat;
    }

    public Float getTransaksi_akum() {
        return transaksi_akum;
    }

    public void setTransaksi_akum(Float transaksi_akum) {
        this.transaksi_akum = transaksi_akum;
    }

    public GeoPoint getKordinat_alamat() {
        return kordinat_alamat;
    }

    public void setKordinat_alamat(GeoPoint kordinat_alamat) {
        this.kordinat_alamat = kordinat_alamat;
    }
}
