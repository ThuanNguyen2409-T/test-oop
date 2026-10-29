package petcare.model;

import petcare.collection.DanhSach;
import petcare.interfaces.CoMa;
import petcare.util.DinhDangUtil;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Hoa don ban hang va dich vu cham soc thu cung voi tinh dong goi du lieu bat bien sau khi thanh toan.
 */
public class HoaDon implements CoMa {
    public static final BigDecimal TI_LE_THUE_VAT = BigDecimal.ZERO;

    private final String ma;
    private String maKhachHang;
    private String maNhanVien;
    private LocalDateTime ngayTao;
    private TrangThaiHoaDon trangThai;
    private LocalDateTime ngayThanhToan;
    private final DanhSach<ChiTietHoaDon> danhSachChiTiet;
    private BigDecimal tongTien;

    public HoaDon(String ma, String maKhachHang, String maNhanVien, LocalDateTime ngayTao,
                  TrangThaiHoaDon trangThai, LocalDateTime ngayThanhToan, BigDecimal tongTien,
                  DanhSach<ChiTietHoaDon> chiTiets) {
        if (ma == null || ma.trim().isEmpty()) {
            throw new IllegalArgumentException("Ma hoa don khong duoc de trong.");
        }
        this.ma = ma.trim();
        this.maKhachHang = maKhachHang != null ? maKhachHang.trim() : "";
        this.maNhanVien = maNhanVien != null ? maNhanVien.trim() : "";
        this.ngayTao = ngayTao != null ? ngayTao : LocalDateTime.now();
        this.trangThai = trangThai != null ? trangThai : TrangThaiHoaDon.CHUA_THANH_TOAN;
        this.ngayThanhToan = ngayThanhToan;
        this.danhSachChiTiet = new DanhSach<>();
        if (chiTiets != null) {
            for (ChiTietHoaDon ct : chiTiets) {
                this.danhSachChiTiet.add(ct);
            }
        }
        if (tongTien != null) {
            this.tongTien = tongTien;
        } else {
            capNhatTongTien();
        }
    }

    public HoaDon(String ma, String maKhachHang, String maNhanVien, LocalDateTime ngayTao,
                  TrangThaiHoaDon trangThai, LocalDateTime ngayThanhToan, BigDecimal tongTien) {
        this(ma, maKhachHang, maNhanVien, ngayTao, trangThai, ngayThanhToan, tongTien, null);
    }

    public boolean isChuaThanhToan() {
        return this.trangThai == TrangThaiHoaDon.CHUA_THANH_TOAN;
    }

    public void themChiTiet(ChiTietHoaDon chiTiet) {
        if (!isChuaThanhToan()) {
            throw new IllegalStateException("Khong the them muc vao hoa don da thanh toan!");
        }
        if (chiTiet == null) {
            throw new IllegalArgumentException("Chi tiet muc khong duoc null.");
        }
        danhSachChiTiet.add(chiTiet);
        capNhatTongTien();
    }

    public void xoaChiTiet(int index) {
        if (!isChuaThanhToan()) {
            throw new IllegalStateException("Khong the xoa muc khoi hoa don da thanh toan!");
        }
        danhSachChiTiet.remove(index);
        capNhatTongTien();
    }

    public void capNhatSoLuongChiTiet(int index, int soLuongMoi) {
        if (!isChuaThanhToan()) {
            throw new IllegalStateException("Khong the sua muc trong hoa don da thanh toan!");
        }
        ChiTietHoaDon cu = danhSachChiTiet.get(index);
        ChiTietHoaDon moi = cu.voiSoLuong(soLuongMoi);
        danhSachChiTiet.set(index, moi);
        capNhatTongTien();
    }

    /**
     * Phuong thuc ho tro nap tu file; bat buoc chan neu hoa don o trang thai da thanh toan.
     */
    public void themChiTietKhiNap(ChiTietHoaDon chiTiet) {
        if (!isChuaThanhToan()) {
            throw new IllegalStateException("Khong the them chi tiet vao hoa don da thanh toan!");
        }
        if (chiTiet == null) {
            throw new IllegalArgumentException("Chi tiet muc khong duoc null.");
        }
        danhSachChiTiet.add(chiTiet);
    }

    public void capNhatTongTien() {
        if (!isChuaThanhToan()) {
            return;
        }
        BigDecimal tong = BigDecimal.ZERO;
        for (ChiTietHoaDon ct : danhSachChiTiet) {
            tong = tong.add(ct.getThanhTien());
        }
        this.tongTien = tong;
    }

    public void danhDauDaThanhToan(LocalDateTime thoiGian) {
        if (!isChuaThanhToan()) {
            throw new IllegalStateException("Hoa don da o trang thai thanh toan truoc do!");
        }
        if (danhSachChiTiet.isEmpty()) {
            throw new IllegalStateException("Khong the thanh toan hoa don khong co muc hang nao!");
        }
        if (thoiGian == null || thoiGian.isBefore(ngayTao)) {
            throw new IllegalArgumentException("Thoi gian thanh toan phai >= thoi gian tao hoa don!");
        }
        this.trangThai = TrangThaiHoaDon.DA_THANH_TOAN;
        this.ngayThanhToan = thoiGian;
    }

    @Override
    public String getMa() {
        return ma;
    }

    public String getMaKhachHang() {
        return maKhachHang;
    }

    public void setMaKhachHang(String maKhachHang) {
        if (!isChuaThanhToan()) {
            throw new IllegalStateException("Khong the thay doi khach hang cua hoa don da thanh toan!");
        }
        this.maKhachHang = maKhachHang != null ? maKhachHang.trim() : "";
    }

    public String getMaNhanVien() {
        return maNhanVien;
    }

    public void setMaNhanVien(String maNhanVien) {
        if (!isChuaThanhToan()) {
            throw new IllegalStateException("Khong the thay doi nhan vien cua hoa don da thanh toan!");
        }
        this.maNhanVien = maNhanVien != null ? maNhanVien.trim() : "";
    }

    public LocalDateTime getNgayTao() {
        return ngayTao;
    }

    public TrangThaiHoaDon getTrangThai() {
        return trangThai;
    }

    public LocalDateTime getNgayThanhToan() {
        return ngayThanhToan;
    }

    /**
     * Tra ve ban sao phong thu (defensive copy) cua danh sach chi tiet.
     */
    public DanhSach<ChiTietHoaDon> getDanhSachChiTiet() {
        DanhSach<ChiTietHoaDon> banSao = new DanhSach<>(danhSachChiTiet.size());
        for (ChiTietHoaDon ct : danhSachChiTiet) {
            banSao.add(ct);
        }
        return banSao;
    }

    public int getSoLuongChiTiet() {
        return danhSachChiTiet.size();
    }

    public ChiTietHoaDon getChiTiet(int index) {
        return danhSachChiTiet.get(index);
    }

    public BigDecimal getTongTien() {
        return tongTien;
    }

    @Override
    public String toString() {
        return String.format("[%s] KH: %s | NV: %s | Ngay tao: %s | TT: %s | Tong: %s",
                ma, maKhachHang, maNhanVien,
                DinhDangUtil.dinhDangNgayGio(ngayTao),
                trangThai.getMoTa(),
                tongTien);
    }
}
