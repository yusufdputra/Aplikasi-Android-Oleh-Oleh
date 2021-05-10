package com.ysf.oleholeh.model;

public class FavoriteModel {
    String id_user, id_produk;

    public FavoriteModel() {
    }

    public FavoriteModel(String id_user, String id_produk) {
        this.id_user = id_user;
        this.id_produk = id_produk;
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


}
