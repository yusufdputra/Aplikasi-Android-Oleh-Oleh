package com.ysf.oleholeh.model;

import java.sql.Timestamp;
import java.util.Date;

public class PemesananModel {
    private String id_user, id_metode_pembayaran, id_status_pemesanan, pesan, estimasi_sampai, id_konfirmasi, id_suplier;
    private int ongkir, total_bayar;
    private Date waktu_pesan, waktu_terima;


    public PemesananModel() {
    }

    public String getId_suplier() {
        return id_suplier;
    }

    public void setId_suplier(String id_suplier) {
        this.id_suplier = id_suplier;
    }

    public Date getWaktu_pesan() {
        return waktu_pesan;
    }

    public void setWaktu_pesan(Date waktu_pesan) {
        this.waktu_pesan = waktu_pesan;
    }

    public String getId_konfirmasi() {
        return id_konfirmasi;
    }

    public void setId_konfirmasi(String id_konfirmasi) {
        this.id_konfirmasi = id_konfirmasi;
    }

    public String getId_user() {
        return id_user;
    }

    public void setId_user(String id_user) {
        this.id_user = id_user;
    }

    public String getId_metode_pembayaran() {
        return id_metode_pembayaran;
    }

    public void setId_metode_pembayaran(String id_metode_pembayaran) {
        this.id_metode_pembayaran = id_metode_pembayaran;
    }

    public String getId_status_pemesanan() {
        return id_status_pemesanan;
    }

    public void setId_status_pemesanan(String id_status_pemesanan) {
        this.id_status_pemesanan = id_status_pemesanan;
    }

    public String getPesan() {
        return pesan;
    }

    public void setPesan(String pesan) {
        this.pesan = pesan;
    }


    public int getOngkir() {
        return ongkir;
    }

    public void setOngkir(int ongkir) {
        this.ongkir = ongkir;
    }

    public int getTotal_bayar() {
        return total_bayar;
    }

    public void setTotal_bayar(int total_bayar) {
        this.total_bayar = total_bayar;
    }

    public String getEstimasi_sampai() {
        return estimasi_sampai;
    }

    public void setEstimasi_sampai(String estimasi_sampai) {
        this.estimasi_sampai = estimasi_sampai;
    }

    public Date getWaktu_terima() {
        return waktu_terima;
    }

    public void setWaktu_terima(Date waktu_terima) {
        this.waktu_terima = waktu_terima;
    }
}
