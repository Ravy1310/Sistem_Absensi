package com.example.demo;

public class RekapBulanan {
    private String nama;
    private String bulan;
    private int masuk;
    private int izin;
    private int sakit;
    private int alfa;
    private String durasi;

    public RekapBulanan(String nama, String bulan, int masuk, int izin, int sakit, int alfa, String durasi) {
        this.nama = nama;
        this.bulan = bulan;
        this.masuk = masuk;
        this.izin = izin;
        this.sakit = sakit;
        this.alfa = alfa;
        this.durasi = durasi;
    }

    // Constructor without durasi for backward compatibility
    public RekapBulanan(String nama, String bulan, int masuk, int izin, int sakit, int alfa) {
        this(nama, bulan, masuk, izin, sakit, alfa, "00:00");
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getBulan() {
        return bulan;
    }

    public void setBulan(String bulan) {
        this.bulan = bulan;
    }

    public int getMasuk() {
        return masuk;
    }

    public void setMasuk(int masuk) {
        this.masuk = masuk;
    }

    public int getIzin() {
        return izin;
    }

    public void setIzin(int izin) {
        this.izin = izin;
    }

    public int getSakit() {
        return sakit;
    }

    public void setSakit(int sakit) {
        this.sakit = sakit;
    }

    public int getAlfa() {
        return alfa;
    }

    public void setAlfa(int alfa) {
        this.alfa = alfa;
    }

    public String getDurasi() {
        return durasi;
    }

    public void setDurasi(String durasi) {
        this.durasi = durasi;
    }
}