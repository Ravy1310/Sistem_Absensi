package cnt.rfid;

import java.sql.Timestamp;

public class Absensi {
    private String id;  // ID adalah RFID Karyawan
    private String nama;
    private Timestamp waktuMasuk;
    private Timestamp waktuKeluar;
    private String hari;
    private String tanggal;
    private String totalJam;


    // Constructor
    public Absensi(String id, String nama, Timestamp waktuMasuk, Timestamp waktuKeluar, String hari, String tanggal, String totalJam) {
        this.id = id;
        this.nama = nama;
        this.waktuMasuk = waktuMasuk;
        this.waktuKeluar = waktuKeluar;
        this.hari = hari;
        this.tanggal = tanggal;
        this.totalJam = totalJam;
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

     public void setTanggal(String tanggal) {
        this.tanggal = tanggal;
    }

    public String getJamMasuk() {
        return jamMasuk;
    }
    
     public String getTotalJam() {
        return totalJam;
    }

    public void setTotalJam(String totalJam) {
        this.totalJam = totalJam;
    }
}
