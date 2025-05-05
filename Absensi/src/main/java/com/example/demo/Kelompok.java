package com.example.demo;

import javafx.beans.property.StringProperty;

public class Kelompok {
    private final StringProperty nama;
    private final StringProperty shift;
    private final StringProperty hari;
    private final StringProperty rfid;
    
    public Kelompok(String nama, String shift, String hari, String rfid) {
        this.nama = new javafx.beans.property.SimpleStringProperty(nama);
        this.shift = new javafx.beans.property.SimpleStringProperty(shift);
        this.hari = new javafx.beans.property.SimpleStringProperty(hari);
        this.rfid = new javafx.beans.property.SimpleStringProperty(rfid); // RFID tetap disimpan tapi tidak ditampilkan

    }

   // Getter Methods
   
    public String getNama() { return nama.get(); }
    public StringProperty namaProperty() { return nama; }
    
    public String getshift() { return shift.get(); }
    public StringProperty shiftProperty() { return shift; }
    
    public String getHari() { return hari.get(); }
    public StringProperty hariProperty() { return hari; }

    public String getRfid() { return rfid.get(); }
    public StringProperty rfidProperty() { return rfid; }
    
    public void setNama(String nama) { this.nama.set(nama); }
    public void setshift(String shift) { this.shift.set(shift); }
    public void setHari(String Hari) { this.hari.set(Hari); }
   
}
