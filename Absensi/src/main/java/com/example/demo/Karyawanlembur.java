package com.example.demo;

public class KaryawanLembur {
    private String nama;
    private String rfid;
    private int totalJam;
    private int gajiLembur;

    public KaryawanLembur(String nama, String rfid, int totalJam, int gajiLembur) {
        this.nama = nama;
        this.rfid = rfid;
        this.totalJam = totalJam;
        this.gajiLembur = gajiLembur;
    }

    public String getNama() { return nama; }
    public String getRfid() { return rfid; }
    public int getTotalJam() { return totalJam; }
    public int getGajiLembur() { return gajiLembur; }
}
