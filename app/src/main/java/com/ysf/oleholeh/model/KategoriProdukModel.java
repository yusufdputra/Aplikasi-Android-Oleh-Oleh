package com.ysf.oleholeh.model;

public class KategoriProdukModel {
    String nama, foto_path;

    public KategoriProdukModel() {
    }

    public KategoriProdukModel(String nama, String foto_path) {
        this.nama = nama;
        this.foto_path = foto_path;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getFoto_path() {
        return foto_path;
    }

    public void setFoto_path(String foto_path) {
        this.foto_path = foto_path;
    }
}
