package petcare.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Nhan vien thu ngan phu trach hoa don va thanh toan.
 */
public class ThuNgan extends NhanVien {
    private String caLamViec;
    private BigDecimal hoaHong;

    public ThuNgan(String ma, String hoTen, String soDienThoai, String email,
                   BigDecimal luongCoBan, double heSoLuong, String caLamViec, BigDecimal hoaHong) {
        super(ma, hoTen, soDienThoai, email, luongCoBan, heSoLuong);
        if (caLamViec == null || caLamViec.trim().isEmpty()) {
            throw new IllegalArgumentException("Ca lam viec khong duoc de trong.");
        }
        if (hoaHong == null || hoaHong.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Hoa hong khong duoc am.");
        }
        this.caLamViec = caLamViec.trim();
        this.hoaHong = hoaHong;
    }

    public String getCaLamViec() {
        return caLamViec;
    }

    public void setCaLamViec(String caLamViec) {
        if (caLamViec == null || caLamViec.trim().isEmpty()) {
            throw new IllegalArgumentException("Ca lam viec khong duoc de trong.");
        }
        this.caLamViec = caLamViec.trim();
    }

    public BigDecimal getHoaHong() {
        return hoaHong;
    }

    public void setHoaHong(BigDecimal hoaHong) {
        if (hoaHong == null || hoaHong.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Hoa hong khong duoc am.");
        }
        this.hoaHong = hoaHong;
    }

    @Override
    public String getVaiTro() {
        return "Thu ngan";
    }

    @Override
    public BigDecimal tinhLuong() {
        BigDecimal luongCoSo = getLuongCoBan().multiply(BigDecimal.valueOf(getHeSoLuong()));
        return luongCoSo.add(hoaHong).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String toString() {
        return super.toString() + String.format(" | Ca: %s | Hoa hong: %s", caLamViec, hoaHong);
    }
}
