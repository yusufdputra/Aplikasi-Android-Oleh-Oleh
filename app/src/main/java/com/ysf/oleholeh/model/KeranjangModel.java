package com.ysf.oleholeh.model;

public class KeranjangModel {
    String id_user, id_produk;
    int kuantitas;
    boolean check;

    public KeranjangModel() {
    }

    public KeranjangModel(String id_user, String id_produk, int kuantitas, boolean check) {
        this.id_user = id_user;
        this.id_produk = id_produk;
        this.kuantitas = kuantitas;
        this.check = check;
    }

    public String getId_user() {
        return id_user;
    }

    public void setId_user(String id_user) {
        this.id_user = id_user;
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

    public boolean isCheck() {
        return check;
    }

    public void setCheck(boolean check) {
        this.check = check;
    }
}
