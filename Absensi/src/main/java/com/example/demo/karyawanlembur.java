package model;

import javafx.beans.property.*;

public class KaryawanLembur {
    private final IntegerProperty no;
    private final StringProperty nama;
    private final IntegerProperty jamLembur;

    public KaryawanLembur(int no, String nama, int jamLembur) {
        this.no = new SimpleIntegerProperty(no);
        this.nama = new SimpleStringProperty(nama);
        this.jamLembur = new SimpleIntegerProperty(jamLembur);
    }

    public int getNo() { return no.get(); }
    public IntegerProperty noProperty() { return no; }

    public String getNama() { return nama.get(); }
    public StringProperty namaProperty() { return nama; }

    public int getJamLembur() { return jamLembur.get(); }
    public IntegerProperty jamLemburProperty() { return jamLembur; }
}
