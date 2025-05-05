package com.example.absensi.model;

import java.time.LocalDate;

public class Absensi {
    private String nama;
    private String hari;
    private LocalDate tanggal;
    private String jamMasuk;
    private String jamPulang;

    public Absensi(String nama, String hari, LocalDate tanggal, String jamMasuk, String jamPulang) {
        this.nama = nama;
        this.hari = hari;
        this.tanggal = tanggal;
        this.jamMasuk = jamMasuk;
        this.jamPulang = jamPulang;
    }

    // Getters
    public String getNama() { return nama; }
    public String getHari() { return hari; }
    public LocalDate getTanggal() { return tanggal; }
    public String getJamMasuk() { return jamMasuk; }
    public String getJamPulang() { return jamPulang; }

    // Optional: Setters kalau mau bisa diubah
    public void setNama(String nama) { this.nama = nama; }
    public void setHari(String hari) { this.hari = hari; }
    public void setTanggal(LocalDate tanggal) { this.tanggal = tanggal; }
    public void setJamMasuk(String jamMasuk) { this.jamMasuk = jamMasuk; }
    public void setJamPulang(String jamPulang) { this.jamPulang = jamPulang; }
}

