package cnt.rfid;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Karyawan {
    private final StringProperty nama;
    private final StringProperty email;
    private final StringProperty posisi;
    private final StringProperty alamat;
    private final StringProperty no_hp;
    private final StringProperty tanggal_masuk;

    public Karyawan(String nama, String email, String posisi, String alamat, String no_hp, String tanggal_masuk) {
        this.nama = new SimpleStringProperty(nama);
        this.email = new SimpleStringProperty(email);
        this.posisi = new SimpleStringProperty(posisi);
        this.alamat = new SimpleStringProperty(alamat);
        this.no_hp = new SimpleStringProperty(no_hp);
        this.tanggal_masuk = new SimpleStringProperty(tanggal_masuk);
    }

    public StringProperty namaProperty() { return nama; }
    public StringProperty emailProperty() { return email; }
    public StringProperty posisiProperty() { return posisi; }
    public StringProperty alamatProperty() { return alamat; }
    public StringProperty noHpProperty() { return no_hp; }
    public StringProperty tanggalMasukProperty() { return tanggal_masuk; }

    public String getNama() { return nama.get(); }
    public String getEmail() { return email.get(); }
    public String getPosisi() { return posisi.get(); }
    public String getAlamat() { return alamat.get(); }
    public String getNoHp() { return no_hp.get(); }
    public String getTanggalMasuk() { return tanggal_masuk.get(); }
}
