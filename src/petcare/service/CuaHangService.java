package petcare.service;

import petcare.collection.DanhSach;
import petcare.interfaces.CoMa;
import petcare.interfaces.TinhTien;
import petcare.model.*;
import petcare.persistence.DuLieuException;
import petcare.persistence.FileStorage;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Lop dich vu quan ly toan bo logic nghiep vu cua cua hang cham soc thu cung.
 * Bao dam tinh nguyen tu giao dich trong bo nho (Transaction Rollback on Error).
 */
public class CuaHangService {
    private final Path duongDanTep;
    private final FileStorage fileStorage;
    private boolean daNapDuLieuThanhCong = false;

    private DanhSach<KhachHang> dsKhachHang = new DanhSach<>();
    private DanhSach<NhanVien> dsNhanVien = new DanhSach<>();
    private DanhSach<ThuCung> dsThuCung = new DanhSach<>();
    private DanhSach<DichVu> dsDichVu = new DanhSach<>();
    private DanhSach<SanPham> dsSanPham = new DanhSach<>();
    private DanhSach<LichHen> dsLichHen = new DanhSach<>();
    private DanhSach<HoaDon> dsHoaDon = new DanhSach<>();

    public CuaHangService(Path duongDanTep) {
        this.duongDanTep = duongDanTep;
        this.fileStorage = new FileStorage();
    }

    public boolean isDaNapDuLieuThanhCong() {
        return daNapDuLieuThanhCong;
    }

    // ================= TRANSACTION SNAPSHOT & ROLLBACK =================

    private FileStorage.StagingData taoBanSaoHienTai() {
        FileStorage.StagingData backup = new FileStorage.StagingData();

        for (KhachHang kh : dsKhachHang) {
            backup.khachHangs.add(new KhachHang(kh.getMa(), kh.getHoTen(), kh.getSoDienThoai(), kh.getEmail(), kh.getDiaChi(), kh.getNgayDangKy()));
        }
        for (NhanVien nv : dsNhanVien) {
            if (nv instanceof NhanVienChamSoc cs) {
                backup.nhanViens.add(new NhanVienChamSoc(cs.getMa(), cs.getHoTen(), cs.getSoDienThoai(), cs.getEmail(), cs.getLuongCoBan(), cs.getHeSoLuong(), cs.getChuyenMon(), cs.getPhuCap()));
            } else if (nv instanceof ThuNgan tn) {
                backup.nhanViens.add(new ThuNgan(tn.getMa(), tn.getHoTen(), tn.getSoDienThoai(), tn.getEmail(), tn.getLuongCoBan(), tn.getHeSoLuong(), tn.getCaLamViec(), tn.getHoaHong()));
            } else if (nv instanceof NhanVienQuanLy ql) {
                backup.nhanViens.add(new NhanVienQuanLy(ql.getMa(), ql.getHoTen(), ql.getSoDienThoai(), ql.getEmail(), ql.getLuongCoBan(), ql.getHeSoLuong(), ql.getPhuCapChucVu()));
            }
        }
        for (ThuCung tc : dsThuCung) {
            if (tc instanceof Cho cho) {
                backup.thuCungs.add(new Cho(cho.getMa(), cho.getTen(), cho.getMaKhachHang(), cho.getTuoi(), cho.getCanNang(), cho.getGiongCho()));
            } else if (tc instanceof Meo meo) {
                backup.thuCungs.add(new Meo(meo.getMa(), meo.getTen(), meo.getMaKhachHang(), meo.getTuoi(), meo.getCanNang(), meo.getMauLong()));
            }
        }
        for (DichVu dv : dsDichVu) {
            if (dv instanceof DichVuTam tam) {
                backup.dichVus.add(new DichVuTam(tam.getMa(), tam.getTen(), tam.getDonGia(), tam.getMoTa(), tam.isCoSayKho()));
            } else if (dv instanceof DichVuCatTia ct) {
                backup.dichVus.add(new DichVuCatTia(ct.getMa(), ct.getTen(), ct.getDonGia(), ct.getMoTa(), ct.getKieuCat()));
            } else if (dv instanceof DichVuLuuTru lt) {
                backup.dichVus.add(new DichVuLuuTru(lt.getMa(), lt.getTen(), lt.getDonGia(), lt.getMoTa(), lt.getLoaiPhong()));
            }
        }
        for (SanPham sp : dsSanPham) {
            if (sp instanceof ThucAn ta) {
                backup.sanPhams.add(new ThucAn(ta.getMa(), ta.getTen(), ta.getDonGia(), ta.getSoLuongTon(), ta.getHanSuDung(), ta.getKhoiLuongGram()));
            } else if (sp instanceof PhuKien pk) {
                backup.sanPhams.add(new PhuKien(pk.getMa(), pk.getTen(), pk.getDonGia(), pk.getSoLuongTon(), pk.getChatLieu(), pk.getKichCo()));
            }
        }
        for (LichHen lh : dsLichHen) {
            backup.lichHens.add(new LichHen(lh.getMa(), lh.getMaKhachHang(), lh.getMaThuCung(), lh.getMaDichVu(), lh.getMaNhanVien(), lh.getThoiGianBatDau(), lh.getThoiGianKetThuc(), lh.getTrangThai(), lh.getGhiChu()));
        }
        for (HoaDon hd : dsHoaDon) {
            backup.hoaDons.add(new HoaDon(hd.getMa(), hd.getMaKhachHang(), hd.getMaNhanVien(), hd.getNgayTao(), hd.getTrangThai(), hd.getNgayThanhToan(), hd.getTongTien(), hd.getDanhSachChiTiet()));
        }
        return backup;
    }

    private void khoiPhucBanSao(FileStorage.StagingData backup) {
        this.dsKhachHang = backup.khachHangs;
        this.dsNhanVien = backup.nhanViens;
        this.dsThuCung = backup.thuCungs;
        this.dsDichVu = backup.dichVus;
        this.dsSanPham = backup.sanPhams;
        this.dsLichHen = backup.lichHens;
        this.dsHoaDon = backup.hoaDons;
    }

    @FunctionalInterface
    private interface ThaoTacNghiepVu {
        void thucHien() throws ValidationException, DuLieuException;
    }

    private void thucHienGiaoDich(ThaoTacNghiepVu thaoTac) throws ValidationException, DuLieuException {
        FileStorage.StagingData backup = taoBanSaoHienTai();
        try {
            thaoTac.thucHien();
            luuDuLieu();
        } catch (ValidationException | DuLieuException e) {
            khoiPhucBanSao(backup);
            throw e;
        } catch (IllegalArgumentException | IllegalStateException e) {
            khoiPhucBanSao(backup);
            throw new ValidationException(e.getMessage(), e);
        } catch (Exception e) {
            khoiPhucBanSao(backup);
            throw new RuntimeException("Loi khong xac dinh trong giao dich: " + e.getMessage(), e);
        }
    }

    // ================= PERSISTENCE & STAGING =================

    public void napDuLieu() throws DuLieuException, ValidationException {
        FileStorage.StagingData staging = fileStorage.docTuFile(duongDanTep);
        kiemTraHopLeToanBo(staging);

        // Commit du lieu hop le vao he thong
        this.dsKhachHang = staging.khachHangs;
        this.dsNhanVien = staging.nhanViens;
        this.dsThuCung = staging.thuCungs;
        this.dsDichVu = staging.dichVus;
        this.dsSanPham = staging.sanPhams;
        this.dsLichHen = staging.lichHens;
        this.dsHoaDon = staging.hoaDons;
        this.daNapDuLieuThanhCong = true;
    }

    public void luuDuLieu() throws DuLieuException, ValidationException {
        if (!daNapDuLieuThanhCong) {
            throw new IllegalStateException("He thong chua nap du lieu thanh cong tu dia, tu choi ghi de de bao ve tep goc!");
        }

        FileStorage.StagingData staging = new FileStorage.StagingData();
        staging.khachHangs = this.dsKhachHang;
        staging.nhanViens = this.dsNhanVien;
        staging.thuCungs = this.dsThuCung;
        staging.dichVus = this.dsDichVu;
        staging.sanPhams = this.dsSanPham;
        staging.lichHens = this.dsLichHen;
        staging.hoaDons = this.dsHoaDon;

        // Xac thuc toan bo tinh hop le truoc khi ghi dia (Validate Full Staging Before Each Save)
        kiemTraHopLeToanBo(staging);

        fileStorage.ghiRaFile(duongDanTep, staging);
    }

    public void kiemTraHopLeToanBo(FileStorage.StagingData staging) throws ValidationException {
        // 1. Kiem tra trung lap ma dinh danh trong tung danh muc (khong phan biet hoa thuong)
        kiemTraTrungMa("Khach hang", staging.khachHangs);
        kiemTraTrungMa("Nhan vien", staging.nhanViens);
        kiemTraTrungMa("Thu cung", staging.thuCungs);
        kiemTraTrungMa("Dich vu", staging.dichVus);
        kiemTraTrungMa("San pham", staging.sanPhams);
        kiemTraTrungMa("Lich hen", staging.lichHens);
        kiemTraTrungMa("Hoa don", staging.hoaDons);

        // 2. Kiem tra gia tri so hoc va cac rang buoc subtype
        for (KhachHang kh : staging.khachHangs) {
            if (kh.getMa() == null || kh.getMa().trim().isEmpty()) {
                throw new ValidationException("Ma khach hang khong duoc de trong.");
            }
            if (kh.getHoTen() == null || kh.getHoTen().trim().isEmpty()) {
                throw new ValidationException("Khach hang " + kh.getMa() + " co ho ten trong.");
            }
            if (kh.getSoDienThoai() == null || !Nguoi.hopLeSoDienThoai(kh.getSoDienThoai())) {
                throw new ValidationException("So dien thoai khach hang khong hop le tai ma: " + kh.getMa());
            }
        }

        for (NhanVien nv : staging.nhanViens) {
            if (nv.getMa() == null || nv.getMa().trim().isEmpty()) {
                throw new ValidationException("Ma nhan vien khong duoc de trong.");
            }
            if (nv.getHoTen() == null || nv.getHoTen().trim().isEmpty()) {
                throw new ValidationException("Nhan vien " + nv.getMa() + " co ho ten trong.");
            }
            if (nv.getSoDienThoai() == null || !Nguoi.hopLeSoDienThoai(nv.getSoDienThoai())) {
                throw new ValidationException("So dien thoai nhan vien khong hop le tai ma: " + nv.getMa());
            }
            if (nv.getLuongCoBan() == null || nv.getLuongCoBan().compareTo(BigDecimal.ZERO) < 0 || !Double.isFinite(nv.getHeSoLuong()) || nv.getHeSoLuong() < 0) {
                throw new ValidationException("Luong co ban hoac he so luong am tai nhan vien: " + nv.getMa());
            }
            if (nv instanceof NhanVienChamSoc cs) {
                if (cs.getChuyenMon() == null || cs.getChuyenMon().trim().isEmpty()) {
                    throw new ValidationException("Nhan vien cham soc " + cs.getMa() + " co chuyen mon trong.");
                }
                if (cs.getPhuCap() == null || cs.getPhuCap().compareTo(BigDecimal.ZERO) < 0) {
                    throw new ValidationException("Nhan vien cham soc " + cs.getMa() + " co phu cap am.");
                }
            } else if (nv instanceof ThuNgan tn) {
                if (tn.getCaLamViec() == null || tn.getCaLamViec().trim().isEmpty()) {
                    throw new ValidationException("Thu ngan " + tn.getMa() + " co ca lam viec trong.");
                }
                if (tn.getHoaHong() == null || tn.getHoaHong().compareTo(BigDecimal.ZERO) < 0) {
                    throw new ValidationException("Thu ngan " + tn.getMa() + " co hoa hong am.");
                }
            } else if (nv instanceof NhanVienQuanLy ql) {
                if (ql.getPhuCapChucVu() == null || ql.getPhuCapChucVu().compareTo(BigDecimal.ZERO) < 0) {
                    throw new ValidationException("Nhan vien quan ly " + ql.getMa() + " co phu cap chuc vu am.");
                }
            }
        }

        for (ThuCung tc : staging.thuCungs) {
            if (tc.getMa() == null || tc.getMa().trim().isEmpty()) {
                throw new ValidationException("Ma thu cung khong duoc de trong.");
            }
            if (tc.getTen() == null || tc.getTen().trim().isEmpty()) {
                throw new ValidationException("Thu cung " + tc.getMa() + " co ten trong.");
            }
            if (tc.getTuoi() < 0) {
                throw new ValidationException("Tuoi thu cung khong hop le (< 0) tai ma: " + tc.getMa());
            }
            if (!Double.isFinite(tc.getCanNang()) || tc.getCanNang() <= 0) {
                throw new ValidationException("Can nang thu cung phai la so thuc huu han > 0 tai ma: " + tc.getMa());
            }
            if (timTheoMa(staging.khachHangs, tc.getMaKhachHang()) == null) {
                throw new ValidationException("Thu cung " + tc.getMa() + " tham chieu toi chu so huu khong ton tai: " + tc.getMaKhachHang());
            }
            if (tc instanceof Cho cho) {
                if (cho.getGiongCho() == null || cho.getGiongCho().trim().isEmpty()) {
                    throw new ValidationException("Cho " + cho.getMa() + " co giong cho trong.");
                }
            } else if (tc instanceof Meo meo) {
                if (meo.getMauLong() == null || meo.getMauLong().trim().isEmpty()) {
                    throw new ValidationException("Meo " + meo.getMa() + " co mau long trong.");
                }
            }
        }

        for (DichVu dv : staging.dichVus) {
            if (dv.getMa() == null || dv.getMa().trim().isEmpty()) {
                throw new ValidationException("Ma dich vu khong duoc de trong.");
            }
            if (dv.getTen() == null || dv.getTen().trim().isEmpty()) {
                throw new ValidationException("Dich vu " + dv.getMa() + " co ten trong.");
            }
            if (dv.getDonGia() == null || dv.getDonGia().compareTo(BigDecimal.ZERO) < 0) {
                throw new ValidationException("Don gia dich vu khong duoc am tai ma: " + dv.getMa());
            }
            if (dv instanceof DichVuCatTia ct) {
                if (ct.getKieuCat() == null || ct.getKieuCat().trim().isEmpty()) {
                    throw new ValidationException("Dich vu cat tia " + ct.getMa() + " co kieu cat trong.");
                }
            } else if (dv instanceof DichVuLuuTru lt) {
                if (lt.getLoaiPhong() == null || lt.getLoaiPhong().trim().isEmpty()) {
                    throw new ValidationException("Dich vu luu tru " + lt.getMa() + " co loai phong trong.");
                }
            }
        }

        for (SanPham sp : staging.sanPhams) {
            if (sp.getMa() == null || sp.getMa().trim().isEmpty()) {
                throw new ValidationException("Ma san pham khong duoc de trong.");
            }
            if (sp.getTen() == null || sp.getTen().trim().isEmpty()) {
                throw new ValidationException("San pham " + sp.getMa() + " co ten trong.");
            }
            if (sp.getDonGia() == null || sp.getDonGia().compareTo(BigDecimal.ZERO) < 0) {
                throw new ValidationException("Don gia san pham khong duoc am tai ma: " + sp.getMa());
            }
            if (sp.getSoLuongTon() < 0) {
                throw new ValidationException("So luong ton kho khong duoc am tai ma: " + sp.getMa());
            }
            if (sp instanceof ThucAn ta) {
                if (ta.getKhoiLuongGram() <= 0) {
                    throw new ValidationException("Thuc an " + ta.getMa() + " co khoi luong <= 0g.");
                }
            } else if (sp instanceof PhuKien pk) {
                if (pk.getChatLieu() == null || pk.getChatLieu().trim().isEmpty()) {
                    throw new ValidationException("Phu kien " + pk.getMa() + " co chat lieu trong.");
                }
                if (pk.getKichCo() == null || pk.getKichCo().trim().isEmpty()) {
                    throw new ValidationException("Phu kien " + pk.getMa() + " co kich co trong.");
                }
            }
        }

        // 3. Kiem tra toan ven tham chieu lich hen
        for (int i = 0; i < staging.lichHens.size(); i++) {
            LichHen lh = staging.lichHens.get(i);
            if (lh.getMa() == null || lh.getMa().trim().isEmpty()) {
                throw new ValidationException("Ma lich hen khong duoc de trong.");
            }
            if (!lh.getThoiGianBatDau().isBefore(lh.getThoiGianKetThuc())) {
                throw new ValidationException("Lich hen " + lh.getMa() + " co thoi gian bat dau phai truoc thoi gian ket thuc.");
            }
            KhachHang kh = timTheoMa(staging.khachHangs, lh.getMaKhachHang());
            if (kh == null) {
                throw new ValidationException("Lich hen " + lh.getMa() + " tham chieu khach hang khong ton tai: " + lh.getMaKhachHang());
            }
            ThuCung tc = timTheoMa(staging.thuCungs, lh.getMaThuCung());
            if (tc == null) {
                throw new ValidationException("Lich hen " + lh.getMa() + " tham chieu thu cung khong ton tai: " + lh.getMaThuCung());
            }
            if (!tc.getMaKhachHang().equalsIgnoreCase(lh.getMaKhachHang())) {
                throw new ValidationException("Lich hen " + lh.getMa() + ": Thu cung " + tc.getMa() + " khong thuoc quyen so huu cua khach hang " + lh.getMaKhachHang());
            }
            if (timTheoMa(staging.dichVus, lh.getMaDichVu()) == null) {
                throw new ValidationException("Lich hen " + lh.getMa() + " tham chieu dich vu khong ton tai: " + lh.getMaDichVu());
            }
            NhanVien nv = timTheoMa(staging.nhanViens, lh.getMaNhanVien());
            if (nv == null) {
                throw new ValidationException("Lich hen " + lh.getMa() + " tham chieu nhan vien khong ton tai: " + lh.getMaNhanVien());
            }
            if (!(nv instanceof NhanVienChamSoc)) {
                throw new ValidationException("Lich hen " + lh.getMa() + " phai duoc phan cong cho Nhan vien cham soc (NhanVienChamSoc): " + lh.getMaNhanVien());
            }

            // Kiem tra xung dot thoi gian voi cac lich hen khac
            for (int j = i + 1; j < staging.lichHens.size(); j++) {
                LichHen other = staging.lichHens.get(j);
                if (lh.coXungDot(other)) {
                    throw new ValidationException("Xung dot lich hen giua " + lh.getMa() + " va " + other.getMa());
                }
            }
        }

        // 4. Kiem tra toan ven tham chieu hoa don, dong muc chi tiet & tinh toan snapshot
        for (HoaDon hd : staging.hoaDons) {
            if (hd.getMa() == null || hd.getMa().trim().isEmpty()) {
                throw new ValidationException("Ma hoa don khong duoc de trong.");
            }
            if (timTheoMa(staging.khachHangs, hd.getMaKhachHang()) == null) {
                throw new ValidationException("Hoa don " + hd.getMa() + " tham chieu khach hang khong ton tai: " + hd.getMaKhachHang());
            }
            if (timTheoMa(staging.nhanViens, hd.getMaNhanVien()) == null) {
                throw new ValidationException("Hoa don " + hd.getMa() + " tham chieu nhan vien khong ton tai: " + hd.getMaNhanVien());
            }

            BigDecimal tongTinhLai = BigDecimal.ZERO;
            for (ChiTietHoaDon ct : hd.getDanhSachChiTiet()) {
                if (ct.getSoLuong() <= 0) {
                    throw new ValidationException("Hoa don " + hd.getMa() + " co so luong muc <= 0 tai: " + ct.getMaMuc());
                }
                if (ct.getDonGia().compareTo(BigDecimal.ZERO) < 0) {
                    throw new ValidationException("Hoa don " + hd.getMa() + " co don gia am tai muc: " + ct.getMaMuc());
                }

                // Kiem tra tham chieu va loai muc (kind) ton tai trong danh muc staging
                if (ct.getLoaiMuc() == LoaiMucHoaDon.SAN_PHAM) {
                    SanPham sp = timTheoMa(staging.sanPhams, ct.getMaMuc());
                    if (sp == null) {
                        throw new ValidationException(String.format("Hoa don %s tham chieu san pham khong ton tai trong danh muc: %s",
                                hd.getMa(), ct.getMaMuc()));
                    }
                } else if (ct.getLoaiMuc() == LoaiMucHoaDon.DICH_VU) {
                    DichVu dv = timTheoMa(staging.dichVus, ct.getMaMuc());
                    if (dv == null) {
                        throw new ValidationException(String.format("Hoa don %s tham chieu dich vu khong ton tai trong danh muc: %s",
                                hd.getMa(), ct.getMaMuc()));
                    }
                } else {
                    throw new ValidationException(String.format("Hoa don %s co loai muc khong hop le tai: %s",
                            hd.getMa(), ct.getMaMuc()));
                }

                BigDecimal thanhTienDung = ct.getDonGia().multiply(BigDecimal.valueOf(ct.getSoLuong()));
                if (ct.getThanhTien().compareTo(thanhTienDung) != 0) {
                    throw new ValidationException(String.format("Chi tiet muc %s trong hoa don %s co thanh tien sai lech (luu: %s, dung donGia*soLuong: %s)",
                            ct.getMaMuc(), hd.getMa(), ct.getThanhTien(), thanhTienDung));
                }
                tongTinhLai = tongTinhLai.add(thanhTienDung);
            }

            if (hd.getTongTien().compareTo(tongTinhLai) != 0) {
                throw new ValidationException(String.format("Hoa don %s co tong tien khong khop voi tong chi tiet (luu: %s, tong chi tiet: %s)",
                        hd.getMa(), hd.getTongTien(), tongTinhLai));
            }

            if (hd.getTrangThai() == TrangThaiHoaDon.DA_THANH_TOAN) {
                if (hd.getDanhSachChiTiet().isEmpty()) {
                    throw new ValidationException("Hoa don da thanh toan " + hd.getMa() + " khong duoc de trong.");
                }
                if (hd.getNgayThanhToan() == null) {
                    throw new ValidationException("Hoa don da thanh toan " + hd.getMa() + " bat buoc phai co ngay thanh toan.");
                }
                if (hd.getNgayThanhToan().isBefore(hd.getNgayTao())) {
                    throw new ValidationException("Hoa don da thanh toan " + hd.getMa() + " co ngay thanh toan truoc ngay tao.");
                }
            } else {
                if (hd.getNgayThanhToan() != null) {
                    throw new ValidationException("Hoa don chua thanh toan " + hd.getMa() + " khong duoc co ngay thanh toan.");
                }
            }
        }
    }

    private <T extends CoMa> void kiemTraTrungMa(String tenThucThe, DanhSach<T> list) throws ValidationException {
        for (int i = 0; i < list.size(); i++) {
            String maI = list.get(i).getMa();
            for (int j = i + 1; j < list.size(); j++) {
                String maJ = list.get(j).getMa();
                if (maI.equalsIgnoreCase(maJ)) {
                    throw new ValidationException("Phat hien trung lap ma " + tenThucThe + ": " + maI);
                }
            }
        }
    }

    // ================= SINH MA TU DONG =================

    public static <T extends CoMa> String sinhMaMoi(DanhSach<T> list, String prefix) {
        int maxNum = 0;
        for (T item : list) {
            String ma = item.getMa();
            if (ma != null && ma.toUpperCase().startsWith(prefix.toUpperCase())) {
                String numStr = ma.substring(prefix.length());
                try {
                    int n = Integer.parseInt(numStr);
                    if (n > maxNum) {
                        maxNum = n;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return String.format("%s%03d", prefix.toUpperCase(), maxNum + 1);
    }

    public static <T extends CoMa> T timTheoMa(DanhSach<T> list, String ma) {
        if (ma == null) {
            return null;
        }
        for (T item : list) {
            if (ma.equalsIgnoreCase(item.getMa())) {
                return item;
            }
        }
        return null;
    }

    // ================= KHACH HANG CRUD =================

    public DanhSach<KhachHang> getDsKhachHang() {
        return dsKhachHang;
    }

    public void themKhachHang(KhachHang kh) throws ValidationException, DuLieuException {
        thucHienGiaoDich(() -> {
            if (timTheoMa(dsKhachHang, kh.getMa()) != null) {
                throw new ValidationException("Ma khach hang da ton tai: " + kh.getMa());
            }
            if (kh.getHoTen() == null || kh.getHoTen().trim().isEmpty()) {
                throw new ValidationException("Ho ten khach hang khong duoc de trong.");
            }
            if (!Nguoi.hopLeSoDienThoai(kh.getSoDienThoai())) {
                throw new ValidationException("So dien thoai khong dung dinh dang: " + kh.getSoDienThoai());
            }
            dsKhachHang.add(kh);
        });
    }

    public void suaKhachHang(String ma, String hoTen, String sdt, String email, String diaChi) throws ValidationException, DuLieuException {
        thucHienGiaoDich(() -> {
            KhachHang kh = timTheoMa(dsKhachHang, ma);
            if (kh == null) {
                throw new ValidationException("Khong tim thay khach hang voi ma: " + ma);
            }
            if (hoTen == null || hoTen.trim().isEmpty()) {
                throw new ValidationException("Ho ten khong duoc de trong.");
            }
            if (!Nguoi.hopLeSoDienThoai(sdt)) {
                throw new ValidationException("So dien thoai khong dung dinh dang: " + sdt);
            }
            kh.setHoTen(hoTen);
            kh.setSoDienThoai(sdt);
            kh.setEmail(email);
            kh.setDiaChi(diaChi);
        });
    }

    public void xoaKhachHang(String ma) throws ValidationException, DuLieuException {
        thucHienGiaoDich(() -> {
            KhachHang kh = timTheoMa(dsKhachHang, ma);
            if (kh == null) {
                throw new ValidationException("Khong tim thay khach hang voi ma: " + ma);
            }
            for (ThuCung tc : dsThuCung) {
                if (ma.equalsIgnoreCase(tc.getMaKhachHang())) {
                    throw new ValidationException("Khong the xoa khach hang dang co thu cung trong he thong (Thu cung: " + tc.getMa() + ").");
                }
            }
            for (LichHen lh : dsLichHen) {
                if (ma.equalsIgnoreCase(lh.getMaKhachHang())) {
                    throw new ValidationException("Khong the xoa khach hang co lich hen trong he thong (Lich hen: " + lh.getMa() + ").");
                }
            }
            for (HoaDon hd : dsHoaDon) {
                if (ma.equalsIgnoreCase(hd.getMaKhachHang())) {
                    throw new ValidationException("Khong the xoa khach hang co hoa don trong he thong (Hoa don: " + hd.getMa() + ").");
                }
            }
            dsKhachHang.remove(kh);
        });
    }

    public DanhSach<KhachHang> timKiemKhachHang(String tuKhoa) {
        DanhSach<KhachHang> ketQua = new DanhSach<>();
        String k = tuKhoa.toLowerCase();
        for (KhachHang kh : dsKhachHang) {
            if (kh.getMa().toLowerCase().contains(k)
                    || kh.getHoTen().toLowerCase().contains(k)
                    || kh.getSoDienThoai().toLowerCase().contains(k)) {
                ketQua.add(kh);
            }
        }
        return ketQua;
    }

    // ================= THU CUNG CRUD =================

    public DanhSach<ThuCung> getDsThuCung() {
        return dsThuCung;
    }

    public void themThuCung(ThuCung tc) throws ValidationException, DuLieuException {
        thucHienGiaoDich(() -> {
            if (timTheoMa(dsThuCung, tc.getMa()) != null) {
                throw new ValidationException("Ma thu cung da ton tai: " + tc.getMa());
            }
            if (timTheoMa(dsKhachHang, tc.getMaKhachHang()) == null) {
                throw new ValidationException("Chu so huu khong ton tai: " + tc.getMaKhachHang());
            }
            if (tc.getTen() == null || tc.getTen().trim().isEmpty()) {
                throw new ValidationException("Ten thu cung khong duoc de trong.");
            }
            if (tc.getTuoi() < 0 || !Double.isFinite(tc.getCanNang()) || tc.getCanNang() <= 0) {
                throw new ValidationException("Tuoi hoac can nang thu cung khong hop le.");
            }
            dsThuCung.add(tc);
        });
    }

    public void suaThuCung(String ma, String tenMoi, int tuoiMoi, double canNangMoi, String extraMoi) throws ValidationException, DuLieuException {
        thucHienGiaoDich(() -> {
            ThuCung tc = timTheoMa(dsThuCung, ma);
            if (tc == null) {
                throw new ValidationException("Khong tim thay thu cung: " + ma);
            }
            if (tenMoi == null || tenMoi.trim().isEmpty()) {
                throw new ValidationException("Ten thu cung khong duoc de trong.");
            }
            if (tuoiMoi < 0 || !Double.isFinite(canNangMoi) || canNangMoi <= 0) {
                throw new ValidationException("Tuoi phai >= 0 va can nang phai la so thuc huu han > 0.");
            }
            tc.setTen(tenMoi);
            tc.setTuoi(tuoiMoi);
            tc.setCanNang(canNangMoi);
            if (tc instanceof Cho cho) {
                cho.setGiongCho(extraMoi);
            } else if (tc instanceof Meo meo) {
                meo.setMauLong(extraMoi);
            }
        });
    }

    public void xoaThuCung(String ma) throws ValidationException, DuLieuException {
        thucHienGiaoDich(() -> {
            ThuCung tc = timTheoMa(dsThuCung, ma);
            if (tc == null) {
                throw new ValidationException("Khong tim thay thu cung: " + ma);
            }
            for (LichHen lh : dsLichHen) {
                if (ma.equalsIgnoreCase(lh.getMaThuCung())) {
                    throw new ValidationException("Khong the xoa thu cung dang co lich hen (Lich hen: " + lh.getMa() + ").");
                }
            }
            dsThuCung.remove(tc);
        });
    }

    public DanhSach<ThuCung> timKiemThuCung(String tuKhoa) {
        DanhSach<ThuCung> ketQua = new DanhSach<>();
        String k = tuKhoa.toLowerCase();
        for (ThuCung tc : dsThuCung) {
            String extra = (tc instanceof Cho cho) ? cho.getGiongCho() : ((Meo) tc).getMauLong();
            if (tc.getMa().toLowerCase().contains(k)
                    || tc.getTen().toLowerCase().contains(k)
                    || tc.getMaKhachHang().toLowerCase().contains(k)
                    || extra.toLowerCase().contains(k)) {
                ketQua.add(tc);
            }
        }
        return ketQua;
    }

    // ================= NHAN VIEN CRUD =================

    public DanhSach<NhanVien> getDsNhanVien() {
        return dsNhanVien;
    }

    public void themNhanVien(NhanVien nv) throws ValidationException, DuLieuException {
        thucHienGiaoDich(() -> {
            if (timTheoMa(dsNhanVien, nv.getMa()) != null) {
                throw new ValidationException("Ma nhan vien da ton tai: " + nv.getMa());
            }
            if (nv.getHoTen() == null || nv.getHoTen().trim().isEmpty()) {
                throw new ValidationException("Ho ten nhan vien khong duoc de trong.");
            }
            if (!Nguoi.hopLeSoDienThoai(nv.getSoDienThoai())) {
                throw new ValidationException("So dien thoai khong dung dinh dang: " + nv.getSoDienThoai());
            }
            if (nv.getLuongCoBan().compareTo(BigDecimal.ZERO) < 0 || !Double.isFinite(nv.getHeSoLuong()) || nv.getHeSoLuong() < 0) {
                throw new ValidationException("Luong co ban hoac he so luong am.");
            }
            dsNhanVien.add(nv);
        });
    }

    public void suaNhanVienChamSoc(String ma, String hoTen, String sdt, String email,
                                   BigDecimal luongCB, double heSo, String chuyenMon, BigDecimal phuCap) throws ValidationException, DuLieuException {
        thucHienGiaoDich(() -> {
            NhanVien nv = timTheoMa(dsNhanVien, ma);
            if (!(nv instanceof NhanVienChamSoc cs)) {
                throw new ValidationException("Khong tim thay nhan vien cham soc voi ma: " + ma);
            }
            cs.setHoTen(hoTen);
            cs.setSoDienThoai(sdt);
            cs.setEmail(email);
            cs.setLuongCoBan(luongCB);
            cs.setHeSoLuong(heSo);
            cs.setChuyenMon(chuyenMon);
            cs.setPhuCap(phuCap);
        });
    }

    public void suaThuNgan(String ma, String hoTen, String sdt, String email,
                           BigDecimal luongCB, double heSo, String caLamViec, BigDecimal hoaHong) throws ValidationException, DuLieuException {
        thucHienGiaoDich(() -> {
            NhanVien nv = timTheoMa(dsNhanVien, ma);
            if (!(nv instanceof ThuNgan tn)) {
                throw new ValidationException("Khong tim thay thu ngan voi ma: " + ma);
            }
            tn.setHoTen(hoTen);
            tn.setSoDienThoai(sdt);
            tn.setEmail(email);
            tn.setLuongCoBan(luongCB);
            tn.setHeSoLuong(heSo);
            tn.setCaLamViec(caLamViec);
            tn.setHoaHong(hoaHong);
        });
    }

    public void suaNhanVienQuanLy(String ma, String hoTen, String sdt, String email,
                                  BigDecimal luongCB, double heSo, BigDecimal phuCapCV) throws ValidationException, DuLieuException {
        thucHienGiaoDich(() -> {
            NhanVien nv = timTheoMa(dsNhanVien, ma);
            if (!(nv instanceof NhanVienQuanLy ql)) {
                throw new ValidationException("Khong tim thay quan ly voi ma: " + ma);
            }
            ql.setHoTen(hoTen);
            ql.setSoDienThoai(sdt);
            ql.setEmail(email);
            ql.setLuongCoBan(luongCB);
            ql.setHeSoLuong(heSo);
            ql.setPhuCapChucVu(phuCapCV);
        });
    }

    public void xoaNhanVien(String ma) throws ValidationException, DuLieuException {
        thucHienGiaoDich(() -> {
            NhanVien nv = timTheoMa(dsNhanVien, ma);
            if (nv == null) {
                throw new ValidationException("Khong tim thay nhan vien: " + ma);
            }
            for (LichHen lh : dsLichHen) {
                if (ma.equalsIgnoreCase(lh.getMaNhanVien())) {
                    throw new ValidationException("Khong the xoa nhan vien dang co lich hen (Lich hen: " + lh.getMa() + ").");
                }
            }
            for (HoaDon hd : dsHoaDon) {
                if (ma.equalsIgnoreCase(hd.getMaNhanVien())) {
                    throw new ValidationException("Khong the xoa nhan vien co hoa don (Hoa don: " + hd.getMa() + ").");
                }
            }
            dsNhanVien.remove(nv);
        });
    }

    public DanhSach<NhanVien> timKiemNhanVien(String tuKhoa) {
        DanhSach<NhanVien> ketQua = new DanhSach<>();
        String k = tuKhoa.toLowerCase();
        for (NhanVien nv : dsNhanVien) {
            if (nv.getMa().toLowerCase().contains(k)
                    || nv.getHoTen().toLowerCase().contains(k)
                    || nv.getSoDienThoai().toLowerCase().contains(k)
                    || nv.getVaiTro().toLowerCase().contains(k)) {
                ketQua.add(nv);
            }
        }
        return ketQua;
    }

    // ================= DICH VU CRUD =================

    public DanhSach<DichVu> getDsDichVu() {
        return dsDichVu;
    }

    public void themDichVu(DichVu dv) throws ValidationException, DuLieuException {
        thucHienGiaoDich(() -> {
            if (timTheoMa(dsDichVu, dv.getMa()) != null) {
                throw new ValidationException("Ma dich vu da ton tai: " + dv.getMa());
            }
            if (dv.getTen() == null || dv.getTen().trim().isEmpty()) {
                throw new ValidationException("Ten dich vu khong duoc de trong.");
            }
            if (dv.getDonGia().compareTo(BigDecimal.ZERO) < 0) {
                throw new ValidationException("Don gia dich vu khong duoc am.");
            }
            dsDichVu.add(dv);
        });
    }

    public void suaDichVuTam(String ma, String tenMoi, BigDecimal giaMoi, String moTaMoi, boolean coSayKho) throws ValidationException, DuLieuException {
        thucHienGiaoDich(() -> {
            DichVu dv = timTheoMa(dsDichVu, ma);
            if (!(dv instanceof DichVuTam tam)) {
                throw new ValidationException("Khong tim thay dich vu tam voi ma: " + ma);
            }
            tam.setTen(tenMoi);
            tam.setDonGiaCoBan(giaMoi);
            tam.setMoTa(moTaMoi);
            tam.setCoSayKho(coSayKho);
        });
    }

    public void suaDichVuCatTia(String ma, String tenMoi, BigDecimal giaMoi, String moTaMoi, String kieuCat) throws ValidationException, DuLieuException {
        thucHienGiaoDich(() -> {
            DichVu dv = timTheoMa(dsDichVu, ma);
            if (!(dv instanceof DichVuCatTia ct)) {
                throw new ValidationException("Khong tim thay dich vu cat tia voi ma: " + ma);
            }
            ct.setTen(tenMoi);
            ct.setDonGiaCoBan(giaMoi);
            ct.setMoTa(moTaMoi);
            ct.setKieuCat(kieuCat);
        });
    }

    public void suaDichVuLuuTru(String ma, String tenMoi, BigDecimal giaMoi, String moTaMoi, String loaiPhong) throws ValidationException, DuLieuException {
        thucHienGiaoDich(() -> {
            DichVu dv = timTheoMa(dsDichVu, ma);
            if (!(dv instanceof DichVuLuuTru lt)) {
                throw new ValidationException("Khong tim thay dich vu luu tru voi ma: " + ma);
            }
            lt.setTen(tenMoi);
            lt.setDonGiaCoBan(giaMoi);
            lt.setMoTa(moTaMoi);
            lt.setLoaiPhong(loaiPhong);
        });
    }

    public void xoaDichVu(String ma) throws ValidationException, DuLieuException {
        thucHienGiaoDich(() -> {
            DichVu dv = timTheoMa(dsDichVu, ma);
            if (dv == null) {
                throw new ValidationException("Khong tim thay dich vu: " + ma);
            }
            for (LichHen lh : dsLichHen) {
                if (ma.equalsIgnoreCase(lh.getMaDichVu())) {
                    throw new ValidationException("Khong the xoa dich vu dang co lich hen (Lich hen: " + lh.getMa() + ").");
                }
            }
            for (HoaDon hd : dsHoaDon) {
                for (ChiTietHoaDon ct : hd.getDanhSachChiTiet()) {
                    if (ct.getLoaiMuc() == LoaiMucHoaDon.DICH_VU && ma.equalsIgnoreCase(ct.getMaMuc())) {
                        throw new ValidationException("Khong the xoa dich vu " + ma + " vi da duoc ghi nhan trong hoa don: " + hd.getMa() + " (" + hd.getTrangThai().getMoTa() + ").");
                    }
                }
            }
            dsDichVu.remove(dv);
        });
    }

    public DanhSach<DichVu> timKiemDichVu(String tuKhoa) {
        DanhSach<DichVu> ketQua = new DanhSach<>();
        String k = tuKhoa.toLowerCase();
        for (DichVu dv : dsDichVu) {
            String extra = "";
            if (dv instanceof DichVuTam tam) extra = tam.isCoSayKho() ? "say kho" : "";
            else if (dv instanceof DichVuCatTia ct) extra = ct.getKieuCat();
            else if (dv instanceof DichVuLuuTru lt) extra = lt.getLoaiPhong();

            if (dv.getMa().toLowerCase().contains(k)
                    || dv.getTen().toLowerCase().contains(k)
                    || dv.getMoTa().toLowerCase().contains(k)
                    || extra.toLowerCase().contains(k)) {
                ketQua.add(dv);
            }
        }
        return ketQua;
    }

    // ================= SAN PHAM CRUD =================

    public DanhSach<SanPham> getDsSanPham() {
        return dsSanPham;
    }

    public void themSanPham(SanPham sp) throws ValidationException, DuLieuException {
        thucHienGiaoDich(() -> {
            if (timTheoMa(dsSanPham, sp.getMa()) != null) {
                throw new ValidationException("Ma san pham da ton tai: " + sp.getMa());
            }
            if (sp.getTen() == null || sp.getTen().trim().isEmpty()) {
                throw new ValidationException("Ten san pham khong duoc de trong.");
            }
            if (sp.getDonGia().compareTo(BigDecimal.ZERO) < 0 || sp.getSoLuongTon() < 0) {
                throw new ValidationException("Don gia hoac so luong ton khong hop le.");
            }
            dsSanPham.add(sp);
        });
    }

    public void suaThucAn(String ma, String tenMoi, BigDecimal giaMoi, int tonMoi, LocalDate hsdMoi, int gramMoi) throws ValidationException, DuLieuException {
        thucHienGiaoDich(() -> {
            SanPham sp = timTheoMa(dsSanPham, ma);
            if (!(sp instanceof ThucAn ta)) {
                throw new ValidationException("Khong tim thay san pham thuc an voi ma: " + ma);
            }
            ta.setTen(tenMoi);
            ta.setDonGia(giaMoi);
            ta.setSoLuongTon(tonMoi);
            ta.setHanSuDung(hsdMoi);
            ta.setKhoiLuongGram(gramMoi);
        });
    }

    public void suaPhuKien(String ma, String tenMoi, BigDecimal giaMoi, int tonMoi, String chatLieuMoi, String kichCoMoi) throws ValidationException, DuLieuException {
        thucHienGiaoDich(() -> {
            SanPham sp = timTheoMa(dsSanPham, ma);
            if (!(sp instanceof PhuKien pk)) {
                throw new ValidationException("Khong tim thay phu kien voi ma: " + ma);
            }
            pk.setTen(tenMoi);
            pk.setDonGia(giaMoi);
            pk.setSoLuongTon(tonMoi);
            pk.setChatLieu(chatLieuMoi);
            pk.setKichCo(kichCoMoi);
        });
    }

    public void xoaSanPham(String ma) throws ValidationException, DuLieuException {
        thucHienGiaoDich(() -> {
            SanPham sp = timTheoMa(dsSanPham, ma);
            if (sp == null) {
                throw new ValidationException("Khong tim thay san pham: " + ma);
            }
            for (HoaDon hd : dsHoaDon) {
                for (ChiTietHoaDon ct : hd.getDanhSachChiTiet()) {
                    if (ct.getLoaiMuc() == LoaiMucHoaDon.SAN_PHAM && ma.equalsIgnoreCase(ct.getMaMuc())) {
                        throw new ValidationException("Khong the xoa san pham " + ma + " vi da duoc ghi nhan trong hoa don: " + hd.getMa() + " (" + hd.getTrangThai().getMoTa() + ").");
                    }
                }
            }
            dsSanPham.remove(sp);
        });
    }

    public DanhSach<SanPham> timKiemSanPham(String tuKhoa) {
        DanhSach<SanPham> ketQua = new DanhSach<>();
        String k = tuKhoa.toLowerCase();
        for (SanPham sp : dsSanPham) {
            String extra = (sp instanceof ThucAn ta) ? ta.getKhoiLuongGram() + "g" : ((PhuKien) sp).getChatLieu() + " " + ((PhuKien) sp).getKichCo();
            if (sp.getMa().toLowerCase().contains(k)
                    || sp.getTen().toLowerCase().contains(k)
                    || extra.toLowerCase().contains(k)) {
                ketQua.add(sp);
            }
        }
        return ketQua;
    }

    // ================= LICH HEN MANAGEMENT =================

    public DanhSach<LichHen> getDsLichHen() {
        return dsLichHen;
    }

    public void datLichHen(LichHen lh) throws ValidationException, DuLieuException {
        thucHienGiaoDich(() -> {
            if (timTheoMa(dsLichHen, lh.getMa()) != null) {
                throw new ValidationException("Ma lich hen da ton tai: " + lh.getMa());
            }
            kiemTraLichHenHopLe(lh, null);
            dsLichHen.add(lh);
        });
    }

    public void suaLichHen(String ma, String maDichVu, String maNhanVien,
                           LocalDateTime batDau, LocalDateTime ketThuc, String ghiChu) throws ValidationException, DuLieuException {
        thucHienGiaoDich(() -> {
            LichHen lh = timTheoMa(dsLichHen, ma);
            if (lh == null) {
                throw new ValidationException("Khong tim thay lich hen: " + ma);
            }
            if (lh.getTrangThai() == TrangThaiLichHen.DA_HUY) {
                throw new ValidationException("Khong the sua lich hen da huy!");
            }

            LichHen banSaoTam = new LichHen(ma, lh.getMaKhachHang(), lh.getMaThuCung(), maDichVu, maNhanVien,
                    batDau, ketThuc, lh.getTrangThai(), ghiChu);
            kiemTraLichHenHopLe(banSaoTam, ma);

            lh.setMaDichVu(maDichVu);
            lh.setMaNhanVien(maNhanVien);
            lh.setThoiGianBatDau(batDau);
            lh.setThoiGianKetThuc(ketThuc);
            lh.setGhiChu(ghiChu);
        });
    }

    public void capNhatTrangThaiLichHen(String ma, TrangThaiLichHen trangThaiMoi) throws ValidationException, DuLieuException {
        thucHienGiaoDich(() -> {
            LichHen lh = timTheoMa(dsLichHen, ma);
            if (lh == null) {
                throw new ValidationException("Khong tim thay lich hen: " + ma);
            }
            // Neu chuyen tu DA_HUY sang active, bat buoc phai kiem tra xung dot thoi gian lai
            if (lh.getTrangThai() == TrangThaiLichHen.DA_HUY && trangThaiMoi != TrangThaiLichHen.DA_HUY) {
                LichHen ungVien = new LichHen(lh.getMa(), lh.getMaKhachHang(), lh.getMaThuCung(), lh.getMaDichVu(), lh.getMaNhanVien(),
                        lh.getThoiGianBatDau(), lh.getThoiGianKetThuc(), trangThaiMoi, lh.getGhiChu());
                kiemTraLichHenHopLe(ungVien, ma);
            }
            lh.setTrangThai(trangThaiMoi);
        });
    }

    public void xoaLichHen(String ma) throws ValidationException, DuLieuException {
        thucHienGiaoDich(() -> {
            LichHen lh = timTheoMa(dsLichHen, ma);
            if (lh == null) {
                throw new ValidationException("Khong tim thay lich hen: " + ma);
            }
            dsLichHen.remove(lh);
        });
    }

    public void kiemTraLichHenHopLe(LichHen lh, String maBoQua) throws ValidationException {
        if (!lh.getThoiGianBatDau().isBefore(lh.getThoiGianKetThuc())) {
            throw new ValidationException("Thoi gian bat dau phai truoc thoi gian ket thuc!");
        }
        KhachHang kh = timTheoMa(dsKhachHang, lh.getMaKhachHang());
        if (kh == null) {
            throw new ValidationException("Khach hang khong ton tai: " + lh.getMaKhachHang());
        }
        ThuCung tc = timTheoMa(dsThuCung, lh.getMaThuCung());
        if (tc == null) {
            throw new ValidationException("Thu cung khong ton tai: " + lh.getMaThuCung());
        }
        if (!tc.getMaKhachHang().equalsIgnoreCase(lh.getMaKhachHang())) {
            throw new ValidationException("Thu cung " + tc.getMa() + " khong phai cua khach hang " + kh.getMa());
        }
        DichVu dv = timTheoMa(dsDichVu, lh.getMaDichVu());
        if (dv == null) {
            throw new ValidationException("Dich vu khong ton tai: " + lh.getMaDichVu());
        }
        NhanVien nv = timTheoMa(dsNhanVien, lh.getMaNhanVien());
        if (nv == null) {
            throw new ValidationException("Nhan vien khong ton tai: " + lh.getMaNhanVien());
        }
        if (!(nv instanceof NhanVienChamSoc)) {
            throw new ValidationException("Nhan vien phu trach lich hen phai la Nhan vien cham soc (NhanVienChamSoc): " + lh.getMaNhanVien());
        }

        // Kiem tra xung dot thoi gian voi cac lich hen khac
        for (LichHen other : dsLichHen) {
            if (maBoQua != null && other.getMa().equalsIgnoreCase(maBoQua)) {
                continue;
            }
            if (lh.coXungDot(other)) {
                throw new ValidationException(String.format("Xung dot lich hen voi ma %s (tu %s den %s). Nhan vien hoac thu cung da co lich hen vao gio nay!",
                        other.getMa(),
                        other.getThoiGianBatDau().format(LichHen.DINH_DANG_NGAY_GIO),
                        other.getThoiGianKetThuc().format(LichHen.DINH_DANG_NGAY_GIO)));
            }
        }
    }

    public DanhSach<LichHen> timKiemLichHen(String tuKhoa) {
        DanhSach<LichHen> ketQua = new DanhSach<>();
        String k = tuKhoa.toLowerCase();
        for (LichHen lh : dsLichHen) {
            if (lh.getMa().toLowerCase().contains(k)
                    || lh.getMaKhachHang().toLowerCase().contains(k)
                    || lh.getMaThuCung().toLowerCase().contains(k)
                    || lh.getMaDichVu().toLowerCase().contains(k)
                    || lh.getMaNhanVien().toLowerCase().contains(k)
                    || lh.getGhiChu().toLowerCase().contains(k)) {
                ketQua.add(lh);
            }
        }
        return ketQua;
    }

    // ================= HOA DON MANAGEMENT =================

    public DanhSach<HoaDon> getDsHoaDon() {
        return dsHoaDon;
    }

    public DanhSach<HoaDon> timKiemHoaDon(String tuKhoa) {
        DanhSach<HoaDon> ketQua = new DanhSach<>();
        String k = tuKhoa.trim().toLowerCase(java.util.Locale.ROOT);
        for (HoaDon hd : dsHoaDon) {
            if (hd.toString().toLowerCase(java.util.Locale.ROOT).contains(k)) {
                ketQua.add(hd);
            }
        }
        return ketQua;
    }

    public void taoHoaDonNhap(HoaDon hd) throws ValidationException, DuLieuException {
        thucHienGiaoDich(() -> {
            if (timTheoMa(dsHoaDon, hd.getMa()) != null) {
                throw new ValidationException("Ma hoa don da ton tai: " + hd.getMa());
            }
            if (hd.getTrangThai() != TrangThaiHoaDon.CHUA_THANH_TOAN) {
                throw new ValidationException("Hoa don tao moi phai o trang thai Chua thanh toan (Nhap): " + hd.getMa());
            }
            if (hd.getNgayThanhToan() != null) {
                throw new ValidationException("Hoa don tao moi khong duoc chua ngay thanh toan: " + hd.getMa());
            }
            if (timTheoMa(dsKhachHang, hd.getMaKhachHang()) == null) {
                throw new ValidationException("Khach hang khong ton tai: " + hd.getMaKhachHang());
            }
            if (timTheoMa(dsNhanVien, hd.getMaNhanVien()) == null) {
                throw new ValidationException("Nhan vien khong ton tai: " + hd.getMaNhanVien());
            }
            dsHoaDon.add(hd);
        });
    }

    public void themMucVaoHoaDon(String maHoaDon, String maMuc, int soLuong) throws ValidationException, DuLieuException {
        thucHienGiaoDich(() -> {
            HoaDon hd = timTheoMa(dsHoaDon, maHoaDon);
            if (hd == null) {
                throw new ValidationException("Khong tim thay hoa don: " + maHoaDon);
            }
            if (!hd.isChuaThanhToan()) {
                throw new ValidationException("Khong the them muc vao hoa don da thanh toan!");
            }
            if (soLuong <= 0) {
                throw new ValidationException("So luong phai lon hon 0.");
            }

            TinhTien item = timTheoMa(dsSanPham, maMuc);
            LoaiMucHoaDon loaiMuc = LoaiMucHoaDon.SAN_PHAM;
            if (item == null) {
                item = timTheoMa(dsDichVu, maMuc);
                loaiMuc = LoaiMucHoaDon.DICH_VU;
            }
            if (item == null) {
                throw new ValidationException("Khong tim thay san pham hoac dich vu voi ma: " + maMuc);
            }

            // Snapshot don gia hieu dung tinh cho 1 don vi (bao gom ca phu phi da hinh cua dich vu)
            BigDecimal donGiaSnapshot = item.tinhThanhTien(1);
            BigDecimal thanhTienSnapshot = donGiaSnapshot.multiply(BigDecimal.valueOf(soLuong));
            ChiTietHoaDon ct = new ChiTietHoaDon(item.getMa(), item.getTen(), loaiMuc, donGiaSnapshot, soLuong, thanhTienSnapshot);
            hd.themChiTiet(ct);
        });
    }

    public void xoaMucKhoiHoaDon(String maHoaDon, int viTriIndex) throws ValidationException, DuLieuException {
        thucHienGiaoDich(() -> {
            HoaDon hd = timTheoMa(dsHoaDon, maHoaDon);
            if (hd == null) {
                throw new ValidationException("Khong tim thay hoa don: " + maHoaDon);
            }
            if (!hd.isChuaThanhToan()) {
                throw new ValidationException("Khong the xoa muc khoi hoa don da thanh toan!");
            }
            hd.xoaChiTiet(viTriIndex);
        });
    }

    public void suaSoLuongMucHoaDon(String maHoaDon, int viTriIndex, int soLuongMoi) throws ValidationException, DuLieuException {
        thucHienGiaoDich(() -> {
            HoaDon hd = timTheoMa(dsHoaDon, maHoaDon);
            if (hd == null) {
                throw new ValidationException("Khong tim thay hoa don: " + maHoaDon);
            }
            if (!hd.isChuaThanhToan()) {
                throw new ValidationException("Khong the sua muc trong hoa don da thanh toan!");
            }
            if (soLuongMoi <= 0) {
                throw new ValidationException("So luong moi phai > 0.");
            }
            hd.capNhatSoLuongChiTiet(viTriIndex, soLuongMoi);
        });
    }

    public void xoaHoaDon(String maHoaDon) throws ValidationException, DuLieuException {
        thucHienGiaoDich(() -> {
            HoaDon hd = timTheoMa(dsHoaDon, maHoaDon);
            if (hd == null) {
                throw new ValidationException("Khong tim thay hoa don: " + maHoaDon);
            }
            if (!hd.isChuaThanhToan()) {
                throw new ValidationException("Khong the xoa hoa don da thanh toan!");
            }
            dsHoaDon.remove(hd);
        });
    }

    public void thanhToanHoaDon(String maHoaDon) throws ValidationException, DuLieuException {
        thucHienGiaoDich(() -> {
            HoaDon hd = timTheoMa(dsHoaDon, maHoaDon);
            if (hd == null) {
                throw new ValidationException("Khong tim thay hoa don: " + maHoaDon);
            }
            if (!hd.isChuaThanhToan()) {
                throw new ValidationException("Hoa don da duoc thanh toan truoc do! Khong the thanh toan lai.");
            }
            if (hd.getDanhSachChiTiet().isEmpty()) {
                throw new ValidationException("Hoa don chua co muc hang nao. Khong the thanh toan!");
            }

            // Gom nhom tong so luong can tru cho tung san pham (chong tran so hoc va khong phan biet hoa thuong)
            Map<String, Long> tongSoLuongCanTru = new HashMap<>();
            for (ChiTietHoaDon ct : hd.getDanhSachChiTiet()) {
                if (ct.getLoaiMuc() == LoaiMucHoaDon.SAN_PHAM) {
                    String maSP = ct.getMaMuc().toUpperCase();
                    long hienTai = tongSoLuongCanTru.getOrDefault(maSP, 0L);
                    long tongMoi = Math.addExact(hienTai, (long) ct.getSoLuong());
                    if (tongMoi > Integer.MAX_VALUE) {
                        throw new ValidationException("Tong so luong can mua vuot qua gioi han kieu so: " + maSP);
                    }
                    tongSoLuongCanTru.put(maSP, tongMoi);
                }
            }

            // Kiem tra ton kho tat ca san pham truoc (Pha 1)
            for (Map.Entry<String, Long> entry : tongSoLuongCanTru.entrySet()) {
                String maSP = entry.getKey();
                int canTru = entry.getValue().intValue();
                SanPham sp = timTheoMa(dsSanPham, maSP);
                if (sp == null) {
                    throw new ValidationException("San pham trong hoa don khong con ton tai trong danh muc: " + maSP);
                }
                if (sp.getSoLuongTon() < canTru) {
                    throw new ValidationException(String.format("San pham [%s - %s] khong du ton kho! Ton kho hien tai: %d, Tong can: %d.",
                            sp.getMa(), sp.getTen(), sp.getSoLuongTon(), canTru));
                }
            }

            // Thoa man tat ca ton kho -> Thuc hien tru kho mot lan duy nhat (Pha 2)
            for (Map.Entry<String, Long> entry : tongSoLuongCanTru.entrySet()) {
                String maSP = entry.getKey();
                int canTru = entry.getValue().intValue();
                SanPham sp = timTheoMa(dsSanPham, maSP);
                sp.setSoLuongTon(sp.getSoLuongTon() - canTru);
            }

            LocalDateTime thoiGian = LocalDateTime.now();
            if (thoiGian.isBefore(hd.getNgayTao())) {
                thoiGian = hd.getNgayTao();
            }
            hd.danhDauDaThanhToan(thoiGian);
        });
    }

    // ================= THONG KE & BAO CAO =================

    public BigDecimal tinhTongDoanhThuDaThanhToan() {
        BigDecimal tong = BigDecimal.ZERO;
        for (HoaDon hd : dsHoaDon) {
            if (hd.getTrangThai() == TrangThaiHoaDon.DA_THANH_TOAN) {
                tong = tong.add(hd.getTongTien());
            }
        }
        return tong;
    }

    public DanhSach<SanPham> layDanhSachSanPhamTonThap(int nguongTon) {
        DanhSach<SanPham> kq = new DanhSach<>();
        for (SanPham sp : dsSanPham) {
            if (sp.getSoLuongTon() <= nguongTon) {
                kq.add(sp);
            }
        }
        return kq;
    }

    public DanhSach<LichHen> layLichHenTheoNgay(LocalDate ngay) {
        DanhSach<LichHen> kq = new DanhSach<>();
        for (LichHen lh : dsLichHen) {
            if (lh.getThoiGianBatDau().toLocalDate().equals(ngay)) {
                kq.add(lh);
            }
        }
        return kq;
    }
}
