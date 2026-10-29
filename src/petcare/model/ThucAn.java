package petcare.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * San pham thuc an danh cho thu cung.
 */
public class ThucAn extends SanPham {
    private LocalDate hanSuDung;
    private int khoiLuongGram;

    public ThucAn(String ma, String ten, BigDecimal donGia, int soLuongTon, LocalDate hanSuDung, int khoiLuongGram) {
        super(ma, ten, donGia, soLuongTon);
        if (hanSuDung == null) {
            throw new IllegalArgumentException("Han su dung khong duoc null.");
        }
        if (khoiLuongGram <= 0) {
            throw new IllegalArgumentException("Khoi luong phai lon hon 0 gram.");
        }
        this.hanSuDung = hanSuDung;
        this.khoiLuongGram = khoiLuongGram;
    }

    public LocalDate getHanSuDung() {
        return hanSuDung;
    }

    public void setHanSuDung(LocalDate hanSuDung) {
        if (hanSuDung == null) {
            throw new IllegalArgumentException("Han su dung khong duoc null.");
        }
        this.hanSuDung = hanSuDung;
    }

    public int getKhoiLuongGram() {
        return khoiLuongGram;
    }

    public void setKhoiLuongGram(int khoiLuongGram) {
        if (khoiLuongGram <= 0) {
            throw new IllegalArgumentException("Khoi luong phai lon hon 0 gram.");
        }
        this.khoiLuongGram = khoiLuongGram;
    }

    @Override
    public String getLoaiSanPham() {
        return "Thuc an";
    }

    @Override
    public String toString() {
        return super.toString() + String.format(" | HSD: %s | Trong luong: %dg", hanSuDung, khoiLuongGram);
    }
}
