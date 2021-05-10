package com.ysf.oleholeh.model;

public class DetailPemesananModel {
    private String id_pemesanan, id_produk;
    private int kuantitas;

    public DetailPemesananModel() {
    }

    public DetailPemesananModel(String id_pemesanan, String id_produk, int kuantitas) {
        this.id_pemesanan = id_pemesanan;
        this.id_produk = id_produk;
        this.kuantitas = kuantitas;
    }

    public String getId_pemesanan() {
        return id_pemesanan;
    }

    public void setId_pemesanan(String id_pemesanan) {
        this.id_pemesanan = id_pemesanan;
    }

    public String getId_produk() {
        return id_produk;
    }

    public void setId_produk(String id_produk) {
        this.id_produk = id_produk;
    }

    public int getKuantitas() {
        return kuantitas;
    }

    public void setKuantitas(int kuantitas) {
        this.kuantitas = kuantitas;
    }


}
