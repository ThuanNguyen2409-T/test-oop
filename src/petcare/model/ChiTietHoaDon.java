package petcare.model;

import java.math.BigDecimal;

/**
 * Dong chi tiet hoa don luu snapshot thong tin bat bien tai thoi diem ghi nhan.
 */
public final class ChiTietHoaDon {
    private final String maMuc;
    private final String tenMuc;
    private final LoaiMucHoaDon loaiMuc;
    private final BigDecimal donGia;
    private final int soLuong;
    private final BigDecimal thanhTien;

    public ChiTietHoaDon(String maMuc, String tenMuc, LoaiMucHoaDon loaiMuc,
                         BigDecimal donGia, int soLuong, BigDecimal thanhTien) {
        if (maMuc == null || maMuc.trim().isEmpty()) {
            throw new IllegalArgumentException("Ma muc khong duoc de trong.");
        }
        if (tenMuc == null || tenMuc.trim().isEmpty()) {
            throw new IllegalArgumentException("Ten muc khong duoc de trong.");
        }
        if (loaiMuc == null) {
            throw new IllegalArgumentException("Loai muc khong duoc null.");
        }
        if (donGia == null || donGia.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Don gia snapshot khong duoc am.");
        }
        if (soLuong <= 0) {
            throw new IllegalArgumentException("So luong phai lon hon 0.");
        }
        this.maMuc = maMuc.trim();
        this.tenMuc = tenMuc.trim();
        this.loaiMuc = loaiMuc;
        this.donGia = donGia;
        this.soLuong = soLuong;
        this.thanhTien = thanhTien != null ? thanhTien : donGia.multiply(BigDecimal.valueOf(soLuong));
    }

    /**
     * Tao snapshot chi tiet moi voi so luong duoc cap nhat,
     * bao toan tuyet doi don gia hieu dung (effective unit price) cu ma khong tham chieu lai danh muc.
     */
    public ChiTietHoaDon voiSoLuong(int soLuongMoi) {
        if (soLuongMoi <= 0) {
            throw new IllegalArgumentException("So luong moi phai lon hon 0.");
        }
        BigDecimal thanhTienMoi = this.donGia.multiply(BigDecimal.valueOf(soLuongMoi));
        return new ChiTietHoaDon(this.maMuc, this.tenMuc, this.loaiMuc, this.donGia, soLuongMoi, thanhTienMoi);
    }

    public String getMaMuc() {
        return maMuc;
    }

    public String getTenMuc() {
        return tenMuc;
    }

    public LoaiMucHoaDon getLoaiMuc() {
        return loaiMuc;
    }

    public BigDecimal getDonGia() {
        return donGia;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public BigDecimal getThanhTien() {
        return thanhTien;
    }

    @Override
    public String toString() {
        return String.format("  - [%s] %s (%s) x %d @ %s = %s",
                maMuc, tenMuc, loaiMuc.getMoTa(), soLuong, donGia, thanhTien);
    }
}
