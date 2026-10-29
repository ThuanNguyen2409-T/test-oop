package petcare.model;

import petcare.interfaces.CoMa;
import petcare.util.DinhDangUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Lich hen cham soc thu cung.
 */
public class LichHen implements CoMa {
    public static final DateTimeFormatter DINH_DANG_NGAY_GIO = DinhDangUtil.DINH_DANG_NGAY_GIO;

    private final String ma;
    private String maKhachHang;
    private String maThuCung;
    private String maDichVu;
    private String maNhanVien;
    private LocalDateTime thoiGianBatDau;
    private LocalDateTime thoiGianKetThuc;
    private TrangThaiLichHen trangThai;
    private String ghiChu;

    public LichHen(String ma, String maKhachHang, String maThuCung, String maDichVu, String maNhanVien,
                   LocalDateTime thoiGianBatDau, LocalDateTime thoiGianKetThuc, TrangThaiLichHen trangThai, String ghiChu) {
        if (ma == null || ma.trim().isEmpty()) {
            throw new IllegalArgumentException("Ma lich hen khong duoc de trong.");
        }
        if (thoiGianBatDau == null || thoiGianKetThuc == null) {
            throw new IllegalArgumentException("Thoi gian bat dau va ket thuc khong duoc null.");
        }
        if (!thoiGianBatDau.isBefore(thoiGianKetThuc)) {
            throw new IllegalArgumentException("Thoi gian bat dau phai truoc thoi gian ket thuc.");
        }
        this.ma = ma.trim();
        this.maKhachHang = maKhachHang != null ? maKhachHang.trim() : "";
        this.maThuCung = maThuCung != null ? maThuCung.trim() : "";
        this.maDichVu = maDichVu != null ? maDichVu.trim() : "";
        this.maNhanVien = maNhanVien != null ? maNhanVien.trim() : "";
        this.thoiGianBatDau = thoiGianBatDau;
        this.thoiGianKetThuc = thoiGianKetThuc;
        this.trangThai = trangThai != null ? trangThai : TrangThaiLichHen.DAT_LICH;
        this.ghiChu = ghiChu != null ? ghiChu.trim() : "";
    }

    public static boolean coGiaoNhauKhoangThoiGian(LocalDateTime s1, LocalDateTime e1, LocalDateTime s2, LocalDateTime e2) {
        return s1.isBefore(e2) && s2.isBefore(e1);
    }

    public boolean coXungDot(LichHen other) {
        if (other == null) {
            return false;
        }
        if (this.trangThai == TrangThaiLichHen.DA_HUY || other.getTrangThai() == TrangThaiLichHen.DA_HUY) {
            return false;
        }
        if (this.ma.equalsIgnoreCase(other.getMa())) {
            return false;
        }
        boolean giaoNhau = coGiaoNhauKhoangThoiGian(this.thoiGianBatDau, this.thoiGianKetThuc,
                other.getThoiGianBatDau(), other.getThoiGianKetThuc());
        if (!giaoNhau) {
            return false;
        }
        boolean trungNhanVien = this.maNhanVien != null && this.maNhanVien.equalsIgnoreCase(other.getMaNhanVien());
        boolean trungThuCung = this.maThuCung != null && this.maThuCung.equalsIgnoreCase(other.getMaThuCung());
        return trungNhanVien || trungThuCung;
    }

    @Override
    public String getMa() {
        return ma;
    }

    public String getMaKhachHang() {
        return maKhachHang;
    }

    public void setMaKhachHang(String maKhachHang) {
        this.maKhachHang = maKhachHang != null ? maKhachHang.trim() : "";
    }

    public String getMaThuCung() {
        return maThuCung;
    }

    public void setMaThuCung(String maThuCung) {
        this.maThuCung = maThuCung != null ? maThuCung.trim() : "";
    }

    public String getMaDichVu() {
        return maDichVu;
    }

    public void setMaDichVu(String maDichVu) {
        this.maDichVu = maDichVu != null ? maDichVu.trim() : "";
    }

    public String getMaNhanVien() {
        return maNhanVien;
    }

    public void setMaNhanVien(String maNhanVien) {
        this.maNhanVien = maNhanVien != null ? maNhanVien.trim() : "";
    }

    public LocalDateTime getThoiGianBatDau() {
        return thoiGianBatDau;
    }

    public void setThoiGianBatDau(LocalDateTime thoiGianBatDau) {
        this.thoiGianBatDau = thoiGianBatDau;
    }

    public LocalDateTime getThoiGianKetThuc() {
        return thoiGianKetThuc;
    }

    public void setThoiGianKetThuc(LocalDateTime thoiGianKetThuc) {
        this.thoiGianKetThuc = thoiGianKetThuc;
    }

    public TrangThaiLichHen getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(TrangThaiLichHen trangThai) {
        this.trangThai = trangThai;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu != null ? ghiChu.trim() : "";
    }

    @Override
    public String toString() {
        return String.format("[%s] KH: %s | TC: %s | DV: %s | NV: %s | %s -> %s | TT: %s | Ghi chu: %s",
                ma, maKhachHang, maThuCung, maDichVu, maNhanVien,
                thoiGianBatDau.format(DinhDangUtil.DINH_DANG_NGAY_GIO),
                thoiGianKetThuc.format(DinhDangUtil.DINH_DANG_NGAY_GIO),
                trangThai.getMoTa(), ghiChu);
    }
}
