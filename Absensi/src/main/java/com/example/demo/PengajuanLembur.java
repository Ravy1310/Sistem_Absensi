package com.example.demo;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class PengajuanLembur {
    private final StringProperty nama;
    private final StringProperty tanggal;
    private final StringProperty durasi;
    private final StringProperty rfid;

    public PengajuanLembur(String nama, String tanggal, String durasi, String rfid) {
        this.nama = new SimpleStringProperty(nama);
        this.tanggal = new SimpleStringProperty(tanggal);
        this.durasi = new SimpleStringProperty(durasi);
        this.rfid = new SimpleStringProperty(rfid); // RFID tetap disimpan tapi tidak ditampilkan
    }

    // Getter Methods
    public String getNama() { return nama.get(); }
    public StringProperty namaProperty() { return nama; }
    public String getTanggal() { return tanggal.get(); }
    public StringProperty tanggalProperty() { return tanggal; }
    public String getDurasi() { return durasi.get(); }
    public StringProperty durasiProperty() { return durasi; }
    public String getRfid() { return rfid.get(); }
    public StringProperty rfidProperty() { return rfid; }

    // Setter Methods
    public void setNama(String nama) { this.nama.set(nama); }
    public void setTanggal(String tanggal) { this.tanggal.set(tanggal); }
    public void setDurasi(String durasi) { this.durasi.set(durasi); }
    public void setRfid(String rfid) { this.rfid.set(rfid); } // Setter for RFID, if needed
}
