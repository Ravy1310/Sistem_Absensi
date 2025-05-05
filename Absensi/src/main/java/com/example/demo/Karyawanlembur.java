package model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleStringProperty;

public class KaryawanLembur {

    private final IntegerProperty no;
    private final StringProperty nama;
    private final IntegerProperty jamLembur;

    public KaryawanLembur(int no, String nama, int jamLembur) {
        this.no = new SimpleIntegerProperty(no);
        this.nama = new SimpleStringProperty(nama);
        this.jamLembur = new SimpleIntegerProperty(jamLembur);
    }

    public int getNo() {
        return no.get();
    }

    public void setNo(int no) {
        this.no.set(no);
    }

    public IntegerProperty noProperty() {
        return no;
    }

    public String getNama() {
        return nama.get();
    }

    public void setNama(String nama) {
        this.nama.set(nama);
    }

    public StringProperty namaProperty() {
        return nama;
    }

    public int getJamLembur() {
        return jamLembur.get();
    }

    public void setJamLembur(int jamLembur) {
        this.jamLembur.set(jamLembur);
    }

    public IntegerProperty jamLemburProperty() {
        return jamLembur;
    }
}
