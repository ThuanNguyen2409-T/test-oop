package petcare.model;

import java.math.BigDecimal;

/**
 * Lop truu tuong dai dien cho nhan vien cua hang.
 */
public abstract class NhanVien extends Nguoi {
    private BigDecimal luongCoBan;
    private double heSoLuong;

    public NhanVien(String ma, String hoTen, String soDienThoai, String email, BigDecimal luongCoBan, double heSoLuong) {
        super(ma, hoTen, soDienThoai, email);
        if (luongCoBan == null || luongCoBan.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Luong co ban khong duoc am.");
        }
        if (!Double.isFinite(heSoLuong) || heSoLuong < 0) {
            throw new IllegalArgumentException("He so luong phai la so thuc huu han >= 0.");
        }
        this.luongCoBan = luongCoBan;
        this.heSoLuong = heSoLuong;
    }

    public BigDecimal getLuongCoBan() {
        return luongCoBan;
    }

    public void setLuongCoBan(BigDecimal luongCoBan) {
        if (luongCoBan == null || luongCoBan.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Luong co ban khong duoc am.");
        }
        this.luongCoBan = luongCoBan;
    }

    public double getHeSoLuong() {
        return heSoLuong;
    }

    public void setHeSoLuong(double heSoLuong) {
        if (!Double.isFinite(heSoLuong) || heSoLuong < 0) {
            throw new IllegalArgumentException("He so luong phai la so thuc huu han >= 0.");
        }
        this.heSoLuong = heSoLuong;
    }

    public abstract BigDecimal tinhLuong();

    @Override
    public String toString() {
        return super.toString() + String.format(" | Luong co ban: %s | He so: %.2f | Thuc nhan: %s",
                luongCoBan, heSoLuong, tinhLuong());
    }
}
