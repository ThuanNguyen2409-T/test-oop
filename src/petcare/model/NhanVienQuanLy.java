package petcare.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Nhan vien quan ly cua hang.
 */
public class NhanVienQuanLy extends NhanVien {
    private BigDecimal phuCapChucVu;

    public NhanVienQuanLy(String ma, String hoTen, String soDienThoai, String email,
                          BigDecimal luongCoBan, double heSoLuong, BigDecimal phuCapChucVu) {
        super(ma, hoTen, soDienThoai, email, luongCoBan, heSoLuong);
        if (phuCapChucVu == null || phuCapChucVu.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Phu cap chuc vu khong duoc am.");
        }
        this.phuCapChucVu = phuCapChucVu;
    }

    public BigDecimal getPhuCapChucVu() {
        return phuCapChucVu;
    }

    public void setPhuCapChucVu(BigDecimal phuCapChucVu) {
        if (phuCapChucVu == null || phuCapChucVu.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Phu cap chuc vu khong duoc am.");
        }
        this.phuCapChucVu = phuCapChucVu;
    }

    @Override
    public String getVaiTro() {
        return "Quan ly";
    }

    @Override
    public BigDecimal tinhLuong() {
        BigDecimal luongCoSo = getLuongCoBan().multiply(BigDecimal.valueOf(getHeSoLuong()));
        return luongCoSo.add(phuCapChucVu).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String toString() {
        return super.toString() + String.format(" | Phu cap chuc vu: %s", phuCapChucVu);
    }
}
