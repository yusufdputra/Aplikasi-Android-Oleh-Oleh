package com.ysf.oleholeh.model;

public class ProduksModel {
    String nama,  produksi, rasa, deskripsi, kategori, foto_path, id_suplier, nama_low_case;
    Double rate_akum;
    int fav_akum, terjual_akum, harga, stok ;

    public ProduksModel() {
    }

    public ProduksModel(String nama, String produksi, String rasa, String deskripsi, String kategori, String foto_path, double rate_akum, int fav_akum, int terjual_akum, int harga, int stok, String id_suplier, String nama_low_case) {
        this.nama = nama;
        this.produksi = produksi;
        this.rasa = rasa;
        this.deskripsi = deskripsi;
        this.kategori = kategori;
        this.foto_path = foto_path;
        this.rate_akum = rate_akum;
        this.fav_akum = fav_akum;
        this.terjual_akum = terjual_akum;
        this.harga = harga;
        this.stok = stok;
        this.id_suplier = id_suplier;
        this.nama_low_case = nama_low_case;
    }

    public String getId_suplier() {
        return id_suplier;
    }

    public void setId_suplier(String id_suplier) {
        this.id_suplier = id_suplier;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getProduksi() {
        return produksi;
    }

    public void setProduksi(String produksi) {
        this.produksi = produksi;
    }

    public String getRasa() {
        return rasa;
    }

    public void setRasa(String rasa) {
        this.rasa = rasa;
    }

    public String getDeskripsi() {
        return deskripsi;
    }

    public void setDeskripsi(String deskripsi) {
        this.deskripsi = deskripsi;
    }

    public String getKategori() {
        return kategori;
    }

    public void setKategori(String kategori) {
        this.kategori = kategori;
    }

    public String getFoto_path() {
        return foto_path;
    }

    public void setFoto_path(String foto_path) {
        this.foto_path = foto_path;
    }

    public Double getRate_akum() {
        return rate_akum;
    }

    public void setRate_akum(Double rate_akum) {
        this.rate_akum = rate_akum;
    }

    public int getFav_akum() {
        return fav_akum;
    }

    public void setFav_akum(int fav_akum) {
        this.fav_akum = fav_akum;
    }

    public int getTerjual_akum() {
        return terjual_akum;
    }

    public void setTerjual_akum(int terjual_akum) {
        this.terjual_akum = terjual_akum;
    }

    public int getHarga() {
        return harga;
    }

    public void setHarga(int harga) {
        this.harga = harga;
    }

    public int getStok() {
        return stok;
    }

    public void setStok(int stok) {
        this.stok = stok;
    }

    public String getNama_low_case() {
        return nama_low_case;
    }

    public void setNama_low_case(String nama_low_case) {
        this.nama_low_case = nama_low_case;
    }
}
