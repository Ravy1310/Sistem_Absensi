package cnt.rfid;

import java.sql.Timestamp;

public class Absensi {
    private String id;  // ID adalah RFID Karyawan
    private String nama;
    private Timestamp waktuMasuk;
    private Timestamp waktuKeluar;

    // Constructor
    public Absensi(String id, String nama, Timestamp waktuMasuk, Timestamp waktuKeluar) {
        this.id = id;
        this.nama = nama;
        this.waktuMasuk = waktuMasuk;
        this.waktuKeluar = waktuKeluar;
    }

    // Getter & Setter
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public Timestamp getWaktuMasuk() {
        return waktuMasuk;
    }

    public void setWaktuMasuk(Timestamp waktuMasuk) {
        this.waktuMasuk = waktuMasuk;
    }

    public Timestamp getWaktuKeluar() {
        return waktuKeluar;
    }

    public void setWaktuKeluar(Timestamp waktuKeluar) {
        this.waktuKeluar = waktuKeluar;
    }
}
