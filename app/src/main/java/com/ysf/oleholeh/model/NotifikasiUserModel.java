package com.ysf.oleholeh.model;

import java.util.Date;

public class NotifikasiUserModel {
    private String file_path, nama_notif, isi_notif, id_user;
    private Date waktu_notif;
    private boolean is_read;


    public NotifikasiUserModel() {
    }

    public NotifikasiUserModel(String file_path, String nama_notif, String isi_notif, String id_user, Date waktu_notif, boolean is_read) {
        this.file_path = file_path;
        this.nama_notif = nama_notif;
        this.isi_notif = isi_notif;
        this.id_user = id_user;
        this.waktu_notif = waktu_notif;
        this.is_read = is_read;
    }

    public String getFile_path() {
        return file_path;
    }

    public void setFile_path(String file_path) {
        this.file_path = file_path;
    }

    public String getNama_notif() {
        return nama_notif;
    }

    public void setNama_notif(String nama_notif) {
        this.nama_notif = nama_notif;
    }

    public String getIsi_notif() {
        return isi_notif;
    }

    public void setIsi_notif(String isi_notif) {
        this.isi_notif = isi_notif;
    }

    public String getId_user() {
        return id_user;
    }

    public void setId_user(String id_user) {
        this.id_user = id_user;
    }

    public Date getWaktu_notif() {
        return waktu_notif;
    }

    public void setWaktu_notif(Date waktu_notif) {
        this.waktu_notif = waktu_notif;
    }

    public boolean isIs_read() {
        return is_read;
    }

    public void setIs_read(boolean is_read) {
        this.is_read = is_read;
    }
}
