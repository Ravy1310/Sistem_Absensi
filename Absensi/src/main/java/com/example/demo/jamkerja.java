package com.example.demo;

public class jamkerja {
    // private javafx.beans.property.StringProperty id = new javafx.beans.property.SimpleStringProperty();
    private javafx.beans.property.StringProperty shift = new javafx.beans.property.SimpleStringProperty();
    private javafx.beans.property.StringProperty mulai = new javafx.beans.property.SimpleStringProperty();
    private javafx.beans.property.StringProperty selesai = new javafx.beans.property.SimpleStringProperty();
    private javafx.beans.property.StringProperty durasi = new javafx.beans.property.SimpleStringProperty();

    // Constructor
    public jamkerja( String shift, String mulai, String selesai, String durasi) {
        // this.id.set(id);
        this.shift.set(shift);
        this.mulai.set(mulai);
        this.selesai.set(selesai);
        this.durasi.set(durasi);
    }

    // Getters
    // public String getId() {
    //     return id.get();
    // }

    public String getShift() {
        return shift.get();
    }

    public String getMulai() {
        return mulai.get();
    }

    public String getSelesai() {
        return selesai.get();
    }

    public String getDurasi() {
        return durasi.get();
    }

    // Setters
    public void setShift(String shift) {
        this.shift.set(shift);
    }

    public void setMulai(String mulai) {
        this.mulai.set(mulai);
    }

    public void setSelesai(String selesai) {
        this.selesai.set(selesai);
    }

    public void setDurasi(String durasi) {
        this.durasi.set(durasi);
    }

    // Property methods for JavaFX bindings
    public javafx.beans.property.StringProperty shiftProperty() {
        return shift;
    }

    public javafx.beans.property.StringProperty mulaiProperty() {
        return mulai;
    }

    public javafx.beans.property.StringProperty selesaiProperty() {
        return selesai;
    }

    public javafx.beans.property.StringProperty durasiProperty() {
        return durasi;
    }
}