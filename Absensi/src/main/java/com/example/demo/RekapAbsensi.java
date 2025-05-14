package com.example.demo;   

public class RekapAbsensi {
    // Fields umum untuk kedua tabel
    private String nama;
    private String shift;
    private String tanggal;
    private String jamMasuk;
    private String jamKeluar;
    private String durasiKerja;
    private String status;
    private String jam;
    // Field tambahan untuk rekapAbsensi
    private String namaHari;
    private String jenisAbsen; // Hanya untuk absensi

    // Constructor tanpa parameter
    public RekapAbsensi() {
    }

    // Constructor dengan parameter (opsional)
    public RekapAbsensi(String nama, String shift, String tanggal,String DurasiKerja, String jam, String jamMasuk, String jamKeluar, String status, String namaHari, String jenisAbsen) {
        this.jam = jam;
        this.nama = nama;
        this.shift = shift;
        this.tanggal = tanggal;
        this.durasiKerja = DurasiKerja;
        this.jamMasuk = jamMasuk;
        this.jamKeluar = jamKeluar;
        this.status = status;
        this.namaHari = namaHari;
        this.jenisAbsen = jenisAbsen;
    }

    // Getter dan Setter untuk nama
    public String getNama() {
        return nama;
    }

    public String getJam() {
        return jam;
    }
    public void setNama(String nama) {
        this.nama = nama;
    }

    // Getter dan Setter untuk shift
    public String getShift() {
        return shift;
    }

    public void setShift(String shift) {
        this.shift = shift;
    }

    // Getter dan Setter untuk tanggal
    public String getTanggal() {
        return tanggal;
    }

    public void setTanggal(String tanggal) {
        this.tanggal = tanggal;
    }

    // Getter dan Setter untuk jamMasuk
    public String getJamMasuk() {
        return jamMasuk;
    }

    public void setJamMasuk(String jamMasuk) {
        this.jamMasuk = jamMasuk;
    }

    // Getter dan Setter untuk jamKeluar
    public String getJamKeluar() {
        return jamKeluar;
    }

    public void setJamKeluar(String jamKeluar) {
        this.jamKeluar = jamKeluar;
    }

    // Getter dan Setter untuk status
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Getter dan Setter untuk namaHari (rekapAbsensi)
    public String getNamaHari() {
        return namaHari;
    }

    public void setNamaHari(String namaHari) {
        this.namaHari = namaHari;
    }

    // Getter dan Setter untuk jenisAbsen (absensi)
    public String getJenisAbsen() {
        return jenisAbsen;
    }

    public void setJenisAbsen(String jenisAbsen) {
        this.jenisAbsen = jenisAbsen;
    }

    public void getDurasiKerja(String durasiKerja) {
        this.durasiKerja = durasiKerja;
    }
    public String getDurasiKerja() {
        return durasiKerja;
    }

    public void setDurasiKerja(String durasiKerja) {
        this.durasiKerja = durasiKerja;
    }
}