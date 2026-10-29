package petcare.model;

import java.time.LocalDate;

/**
 * Lop dai dien cho khach hang cua cua hang thu cung.
 */
public class KhachHang extends Nguoi {
    private String diaChi;
    private LocalDate ngayDangKy;

    public KhachHang(String ma, String hoTen, String soDienThoai, String email, String diaChi, LocalDate ngayDangKy) {
        super(ma, hoTen, soDienThoai, email);
        this.diaChi = diaChi;
        this.ngayDangKy = ngayDangKy;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }

    public LocalDate getNgayDangKy() {
        return ngayDangKy;
    }

    public void setNgayDangKy(LocalDate ngayDangKy) {
        this.ngayDangKy = ngayDangKy;
    }

    @Override
    public String getVaiTro() {
        return "Khach hang";
    }

    @Override
    public String toString() {
        return super.toString() + String.format(" | Dia chi: %s | Ngay DK: %s", diaChi, ngayDangKy);
    }
}
