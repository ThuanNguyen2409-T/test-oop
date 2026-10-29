package petcare.model;

import petcare.interfaces.CoMa;
import petcare.interfaces.TinhTien;

import java.math.BigDecimal;

/**
 * Lop truu tuong dai dien cho cac dich vu cham soc thu cung.
 */
public abstract class DichVu implements CoMa, TinhTien {
    private final String ma;
    private String ten;
    private BigDecimal donGiaCoBan;
    private String moTa;

    public DichVu(String ma, String ten, BigDecimal donGiaCoBan, String moTa) {
        if (ma == null || ma.trim().isEmpty()) {
            throw new IllegalArgumentException("Ma dich vu khong duoc de trong.");
        }
        if (donGiaCoBan == null || donGiaCoBan.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Don gia co ban khong duoc am.");
        }
        this.ma = ma.trim();
        this.ten = ten != null ? ten.trim() : "";
        this.donGiaCoBan = donGiaCoBan;
        this.moTa = moTa != null ? moTa.trim() : "";
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
            throw new IllegalArgumentException("Ten dich vu khong duoc de trong.");
        }
        this.ten = ten.trim();
    }

    @Override
    public BigDecimal getDonGia() {
        return donGiaCoBan;
    }

    public void setDonGiaCoBan(BigDecimal donGiaCoBan) {
        if (donGiaCoBan == null || donGiaCoBan.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Don gia co ban khong duoc am.");
        }
        this.donGiaCoBan = donGiaCoBan;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa != null ? moTa.trim() : "";
    }

    public abstract BigDecimal tinhGia(int soLuongHoacSoNgay);

    public abstract String getDonViTinh();

    @Override
    public BigDecimal tinhThanhTien(int soLuong) {
        return tinhGia(soLuong);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s | Don gia co ban: %s/%s | Mo ta: %s",
                ma, ten, donGiaCoBan, getDonViTinh(), moTa);
    }
}
