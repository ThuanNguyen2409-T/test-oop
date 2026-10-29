package petcare.model;

import petcare.interfaces.CoMa;
import petcare.interfaces.TinhTien;

import java.math.BigDecimal;

/**
 * Lop truu tuong dai dien cho cac san pham kinh doanh tai cua hang.
 */
public abstract class SanPham implements CoMa, TinhTien {
    private final String ma;
    private String ten;
    private BigDecimal donGia;
    private int soLuongTon;

    public SanPham(String ma, String ten, BigDecimal donGia, int soLuongTon) {
        if (ma == null || ma.trim().isEmpty()) {
            throw new IllegalArgumentException("Ma san pham khong duoc de trong.");
        }
        if (donGia == null || donGia.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Don gia san pham khong duoc am.");
        }
        if (soLuongTon < 0) {
            throw new IllegalArgumentException("So luong ton kho khong duoc am.");
        }
        this.ma = ma.trim();
        this.ten = ten != null ? ten.trim() : "";
        this.donGia = donGia;
        this.soLuongTon = soLuongTon;
    }

    @Override
    public String getMa() {
        return ma;
    }

    @Override
    public String getTen() {
        return ten;
    }

    public void setTen(String ten) {
        if (ten == null || ten.trim().isEmpty()) {
            throw new IllegalArgumentException("Ten san pham khong duoc de trong.");
        }
        this.ten = ten.trim();
    }

    @Override
    public BigDecimal getDonGia() {
        return donGia;
    }

    public void setDonGia(BigDecimal donGia) {
        if (donGia == null || donGia.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Don gia san pham khong duoc am.");
        }
        this.donGia = donGia;
    }

    public int getSoLuongTon() {
        return soLuongTon;
    }

    public void setSoLuongTon(int soLuongTon) {
        if (soLuongTon < 0) {
            throw new IllegalArgumentException("So luong ton kho khong duoc am.");
        }
        this.soLuongTon = soLuongTon;
    }

    public abstract String getLoaiSanPham();

    @Override
    public BigDecimal tinhThanhTien(int soLuong) {
        if (soLuong < 0) {
            throw new IllegalArgumentException("So luong tinh tien khong duoc am.");
        }
        return donGia.multiply(BigDecimal.valueOf(soLuong));
    }

    @Override
    public String toString() {
        return String.format("[%s] %s (%s) | Gia: %s | Ton kho: %d",
                ma, ten, getLoaiSanPham(), donGia, soLuongTon);
    }
}
