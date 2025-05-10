package com.example.demo;

public class RekapBulanan {
    private String nama;
    private String bulan;
    private int masuk;
    private int izin;
    private int sakit;
    private int alfa;

    public RekapBulanan(String nama, String bulan, int masuk, int izin, int sakit, int alfa) {
        this.nama = nama;
    this.bulan = bulan;
    this.masuk = masuk;
    this.izin = izin;
    this.sakit = sakit;
    this.alfa = alfa;
    }

    public String getNama() { return nama; }
    public String getBulan() { return bulan; }
    public int getMasuk() { return masuk; }
    public int getIzin() { return izin; }
    public int getSakit() { return sakit; }
    public int getAlfa() { return alfa; }
}

