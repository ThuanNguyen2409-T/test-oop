package petcare.model;

import java.math.BigDecimal;

/**
 * Dich vu khach san, luu tru thu cung theo ngay.
 */
public class DichVuLuuTru extends DichVu {
    private static final BigDecimal PHU_PHI_PHONG_VIP = new BigDecimal("100000");
    private String loaiPhong;

    public DichVuLuuTru(String ma, String ten, BigDecimal donGiaCoBan, String moTa, String loaiPhong) {
        super(ma, ten, donGiaCoBan, moTa);
        if (loaiPhong == null || loaiPhong.trim().isEmpty()) {
            throw new IllegalArgumentException("Loai phong khong duoc de trong.");
        }
        this.loaiPhong = loaiPhong.trim();
    }

    public String getLoaiPhong() {
        return loaiPhong;
    }

    public void setLoaiPhong(String loaiPhong) {
        if (loaiPhong == null || loaiPhong.trim().isEmpty()) {
            throw new IllegalArgumentException("Loai phong khong duoc de trong.");
        }
        this.loaiPhong = loaiPhong.trim();
    }

    @Override
    public String getDonViTinh() {
        return "Ngay";
    }

    @Override
    public BigDecimal tinhGia(int soLuongHoacSoNgay) {
        if (soLuongHoacSoNgay < 0) {
            throw new IllegalArgumentException("So ngay khong duoc am.");
        }
        BigDecimal giaMoiNgay = getDonGia();
        if (loaiPhong != null && loaiPhong.equalsIgnoreCase("VIP")) {
            giaMoiNgay = giaMoiNgay.add(PHU_PHI_PHONG_VIP);
        }
        return giaMoiNgay.multiply(BigDecimal.valueOf(soLuongHoacSoNgay));
    }

    @Override
    public String toString() {
        return super.toString() + String.format(" | Loai phong: %s", loaiPhong);
    }
}
