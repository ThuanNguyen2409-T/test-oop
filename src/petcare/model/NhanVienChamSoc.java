package petcare.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Nhan vien cham soc, grooming thu cung.
 */
public class NhanVienChamSoc extends NhanVien {
    private String chuyenMon;
    private BigDecimal phuCap;

    public NhanVienChamSoc(String ma, String hoTen, String soDienThoai, String email,
                           BigDecimal luongCoBan, double heSoLuong, String chuyenMon, BigDecimal phuCap) {
        super(ma, hoTen, soDienThoai, email, luongCoBan, heSoLuong);
        if (chuyenMon == null || chuyenMon.trim().isEmpty()) {
            throw new IllegalArgumentException("Chuyen mon khong duoc de trong.");
        }
        if (phuCap == null || phuCap.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Phu cap khong duoc am.");
        }
        this.chuyenMon = chuyenMon.trim();
        this.phuCap = phuCap;
    }

    public String getChuyenMon() {
        return chuyenMon;
    }

    public void setChuyenMon(String chuyenMon) {
        if (chuyenMon == null || chuyenMon.trim().isEmpty()) {
            throw new IllegalArgumentException("Chuyen mon khong duoc de trong.");
        }
        this.chuyenMon = chuyenMon.trim();
    }

    public BigDecimal getPhuCap() {
        return phuCap;
    }

    public void setPhuCap(BigDecimal phuCap) {
        if (phuCap == null || phuCap.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Phu cap khong duoc am.");
        }
        this.phuCap = phuCap;
    }

    @Override
    public String getVaiTro() {
        return "Nhan vien cham soc";
    }

    @Override
    public BigDecimal tinhLuong() {
        BigDecimal luongCoSo = getLuongCoBan().multiply(BigDecimal.valueOf(getHeSoLuong()));
        return luongCoSo.add(phuCap).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String toString() {
        return super.toString() + String.format(" | Chuyen mon: %s | Phu cap: %s", chuyenMon, phuCap);
    }
}
