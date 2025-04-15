package com.example.demo;

import java.time.LocalDate;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Karyawan {
    private final StringProperty nama;
    private final StringProperty alamat;
    private final StringProperty noHp;
    private final StringProperty email;
    private final ObjectProperty<LocalDate> tanggalMasuk;
    private final StringProperty posisi;
    private final StringProperty rfid;

    public Karyawan(String nama, String alamat, String noHp, String email, LocalDate tanggalMasuk, String posisi, String rfid) {
        this.nama = new SimpleStringProperty(nama);
        this.alamat = new SimpleStringProperty(alamat);
        this.noHp = new SimpleStringProperty(noHp);
        this.email = new SimpleStringProperty(email);
        this.tanggalMasuk = new SimpleObjectProperty<>(tanggalMasuk);
        this.posisi = new SimpleStringProperty(posisi);
        this.rfid = new SimpleStringProperty(rfid); // RFID tetap disimpan tapi tidak ditampilkan
    }

    // Getter Methods
    public String getNama() { return nama.get(); }
    public StringProperty namaProperty() { return nama; }
    
    public String getAlamat() { return alamat.get(); }
    public StringProperty alamatProperty() { return alamat; }
    
    public String getNoHp() { return noHp.get(); }
    public StringProperty noHpProperty() { return noHp; }
    
    public String getEmail() { return email.get(); }
    public StringProperty emailProperty() { return email; }
    
    public LocalDate getTanggalMasuk() { return tanggalMasuk.get(); }
    public ObjectProperty<LocalDate> tanggalMasukProperty() { return tanggalMasuk; }
    
    public String getPosisi() { return posisi.get(); }
    public StringProperty posisiProperty() { return posisi; }
    
    public String getRfid() { return rfid.get(); }
    public StringProperty rfidProperty() { return rfid; }

    // Setter Methods
    public void setNama(String nama) { this.nama.set(nama); }
    public void setAlamat(String alamat) { this.alamat.set(alamat); }
    public void setNoHp(String noHp) { this.noHp.set(noHp); }
    public void setEmail(String email) { this.email.set(email); }
    public void setTanggalMasuk(LocalDate tanggalMasuk) { this.tanggalMasuk.set(tanggalMasuk); }
    public void setPosisi(String posisi) { this.posisi.set(posisi); }
    public void setRfid(String rfid) { this.rfid.set(rfid); }
}