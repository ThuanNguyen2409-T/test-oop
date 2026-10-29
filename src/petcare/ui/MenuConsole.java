package petcare.ui;

import petcare.collection.DanhSach;
import petcare.model.*;
import petcare.persistence.DuLieuException;
import petcare.service.CuaHangService;
import petcare.service.ValidationException;
import petcare.util.DinhDangUtil;
import petcare.util.InputUtil;
import petcare.util.KetThucNhapException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Scanner;

/**
 * Giao dien dong lenh (Console) quan ly cua hang cham soc thu cung.
 */
public class MenuConsole {
    private final CuaHangService service;
    private final Scanner scanner;

    public MenuConsole(CuaHangService service) {
        this.service = service;
        this.scanner = new Scanner(System.in);
    }

    public void chay() {
        boolean tiepTuc = true;
        while (tiepTuc) {
            inTieuDe("HE THONG QUAN LY CUA HANG CHAM SOC THU CUNG");
            System.out.println("1. Quan ly Khach hang");
            System.out.println("2. Quan ly Thu cung");
            System.out.println("3. Quan ly Nhan vien");
            System.out.println("4. Quan ly Dich vu");
            System.out.println("5. Quan ly San pham");
            System.out.println("6. Quan ly Lich hen");
            System.out.println("7. Quan ly Hoa don & Thanh toan");
            System.out.println("8. Bao cao & Thong ke");
            System.out.println("0. Thoat chuong trinh");
            System.out.println("----------------------------------------------");

            int chon = InputUtil.nhapSoNguyen(scanner, "Chon chuc nang (0-8): ", 0, 8);
            try {
                switch (chon) {
                    case 1 -> menuKhachHang();
                    case 2 -> menuThuCung();
                    case 3 -> menuNhanVien();
                    case 4 -> menuDichVu();
                    case 5 -> menuSanPham();
                    case 6 -> menuLichHen();
                    case 7 -> menuHoaDon();
                    case 8 -> menuBaoCao();
                    case 0 -> {
                        tiepTuc = false;
                        System.out.println("\nCam on ban da su dung phan mem! Tam biet.");
                    }
                }
            } catch (KetThucNhapException e) {
                throw e;
            } catch (Exception e) {
                System.out.println("  [Loi he thong]: " + e.getMessage());
            }
        }
    }

    private void inTieuDe(String tieuDe) {
        System.out.println("\n==============================================");
        System.out.println("   " + tieuDe);
        System.out.println("==============================================");
    }

    // ================= 1. KHACH HANG =================
    private void menuKhachHang() {
        boolean back = false;
        while (!back) {
            inTieuDe("QUAN LY KHACH HANG");
            System.out.println("1. Danh sach khach hang");
            System.out.println("2. Them khach hang moi");
            System.out.println("3. Sua thong tin khach hang");
            System.out.println("4. Xoa khach hang");
            System.out.println("5. Tim kiem khach hang");
            System.out.println("0. Quay lai menu chinh");

            int chon = InputUtil.nhapSoNguyen(scanner, "Chon (0-5): ", 0, 5);
            try {
                switch (chon) {
                    case 1 -> {
                        DanhSach<KhachHang> list = service.getDsKhachHang();
                        System.out.printf("\nTong so: %d khach hang%n", list.size());
                        for (KhachHang kh : list) {
                            System.out.println("  " + kh);
                        }
                    }
                    case 2 -> {
                        String ma = CuaHangService.sinhMaMoi(service.getDsKhachHang(), "KH");
                        System.out.println("Ma khach hang moi: " + ma);
                        String hoTen = InputUtil.nhapChuoi(scanner, "Nhap ho ten: ");
                        String sdt = InputUtil.nhapChuoi(scanner, "Nhap so dien thoai: ");
                        String email = InputUtil.nhapChuoi(scanner, "Nhap email: ");
                        String diaChi = InputUtil.nhapChuoi(scanner, "Nhap dia chi: ");
                        LocalDate ngayDK = LocalDate.now();
                        service.themKhachHang(new KhachHang(ma, hoTen, sdt, email, diaChi, ngayDK));
                        System.out.println("  => Them khach hang thanh cong!");
                    }
                    case 3 -> {
                        String ma = InputUtil.nhapChuoi(scanner, "Nhap ma khach hang can sua: ");
                        KhachHang cu = CuaHangService.timTheoMa(service.getDsKhachHang(), ma);
                        if (cu == null) {
                            System.out.println("  [Loi] Khong tim thay khach hang!");
                            break;
                        }
                        String hoTen = InputUtil.nhapChuoiTuyChon(scanner, "Ho ten moi [" + cu.getHoTen() + "]: ", cu.getHoTen());
                        String sdt = InputUtil.nhapChuoiTuyChon(scanner, "SDT moi [" + cu.getSoDienThoai() + "]: ", cu.getSoDienThoai());
                        String email = InputUtil.nhapChuoiTuyChon(scanner, "Email moi [" + cu.getEmail() + "]: ", cu.getEmail());
                        String diaChi = InputUtil.nhapChuoiTuyChon(scanner, "Dia chi moi [" + cu.getDiaChi() + "]: ", cu.getDiaChi());
                        service.suaKhachHang(ma, hoTen, sdt, email, diaChi);
                        System.out.println("  => Cap nhat khach hang thanh cong!");
                    }
                    case 4 -> {
                        String ma = InputUtil.nhapChuoi(scanner, "Nhap ma khach hang can xoa: ");
                        if (InputUtil.nhapCoKhong(scanner, "Ban co chac chan muon xoa khach hang " + ma + "?")) {
                            service.xoaKhachHang(ma);
                            System.out.println("  => Xoa khach hang thanh cong!");
                        }
                    }
                    case 5 -> {
                        String k = InputUtil.nhapChuoi(scanner, "Nhap tu khoa tim kiem (ma, ten, sdt): ");
                        DanhSach<KhachHang> kq = service.timKiemKhachHang(k);
                        System.out.printf("Tim thay %d ket qua:%n", kq.size());
                        for (KhachHang kh : kq) {
                            System.out.println("  " + kh);
                        }
                    }
                    case 0 -> back = true;
                }
            } catch (KetThucNhapException e) {
                throw e;
            } catch (ValidationException e) {
                System.out.println("  [Loi nghiep vu]: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("  [Loi]: " + e.getMessage());
            }
        }
    }

    // ================= 2. THU CUNG =================
    private void menuThuCung() {
        boolean back = false;
        while (!back) {
            inTieuDe("QUAN LY THU CUNG");
            System.out.println("1. Danh sach thu cung");
            System.out.println("2. Them thu cung moi");
            System.out.println("3. Sua thong tin thu cung");
            System.out.println("4. Xoa thu cung");
            System.out.println("5. Tim kiem thu cung");
            System.out.println("0. Quay lai");

            int chon = InputUtil.nhapSoNguyen(scanner, "Chon (0-5): ", 0, 5);
            try {
                switch (chon) {
                    case 1 -> {
                        DanhSach<ThuCung> list = service.getDsThuCung();
                        System.out.printf("\nTong so: %d thu cung%n", list.size());
                        for (ThuCung tc : list) {
                            System.out.println("  " + tc);
                        }
                    }
                    case 2 -> {
                        String ma = CuaHangService.sinhMaMoi(service.getDsThuCung(), "TC");
                        System.out.println("Ma thu cung moi: " + ma);
                        int loai = InputUtil.nhapSoNguyen(scanner, "Chon loai (1. Cho, 2. Meo): ", 1, 2);
                        String ten = InputUtil.nhapChuoi(scanner, "Nhap ten thu cung: ");
                        String maKH = InputUtil.nhapChuoi(scanner, "Nhap ma khach hang chu so huu: ");
                        int tuoi = InputUtil.nhapSoNguyen(scanner, "Nhap tuoi (nam): ", 0, 50);
                        double canNang = InputUtil.nhapSoThuc(scanner, "Nhap can nang (kg): ", 0.1);

                        if (loai == 1) {
                            String giong = InputUtil.nhapChuoi(scanner, "Nhap giong cho (VD: Poodle, Corgi): ");
                            service.themThuCung(new Cho(ma, ten, maKH, tuoi, canNang, giong));
                        } else {
                            String mauLong = InputUtil.nhapChuoi(scanner, "Nhap mau long (VD: Trang vang, Tam the): ");
                            service.themThuCung(new Meo(ma, ten, maKH, tuoi, canNang, mauLong));
                        }
                        System.out.println("  => Them thu cung thanh cong!");
                    }
                    case 3 -> {
                        String ma = InputUtil.nhapChuoi(scanner, "Nhap ma thu cung can sua: ");
                        ThuCung cu = CuaHangService.timTheoMa(service.getDsThuCung(), ma);
                        if (cu == null) {
                            System.out.println("  [Loi] Khong tim thay thu cung!");
                            break;
                        }
                        String ten = InputUtil.nhapChuoiTuyChon(scanner, "Ten moi [" + cu.getTen() + "]: ", cu.getTen());
                        int tuoi = InputUtil.nhapSoNguyen(scanner, "Tuoi moi [" + cu.getTuoi() + "]: ", 0, 50);
                        double canNang = InputUtil.nhapSoThuc(scanner, "Can nang moi [" + cu.getCanNang() + "]: ", 0.1);
                        String extraCu = (cu instanceof Cho cho) ? cho.getGiongCho() : ((Meo) cu).getMauLong();
                        String extraLabel = (cu instanceof Cho) ? "Giong cho moi" : "Mau long moi";
                        String extraMoi = InputUtil.nhapChuoiTuyChon(scanner, extraLabel + " [" + extraCu + "]: ", extraCu);
                        service.suaThuCung(ma, ten, tuoi, canNang, extraMoi);
                        System.out.println("  => Sua thu cung thanh cong!");
                    }
                    case 4 -> {
                        String ma = InputUtil.nhapChuoi(scanner, "Nhap ma thu cung can xoa: ");
                        if (InputUtil.nhapCoKhong(scanner, "Ban co chac muon xoa thu cung " + ma + "?")) {
                            service.xoaThuCung(ma);
                            System.out.println("  => Xoa thu cung thanh cong!");
                        }
                    }
                    case 5 -> {
                        String k = InputUtil.nhapChuoi(scanner, "Nhap tu khoa tim kiem (ma, ten, ma chu, giong/mau): ");
                        DanhSach<ThuCung> kq = service.timKiemThuCung(k);
                        System.out.printf("Tim thay %d thu cung:%n", kq.size());
                        for (ThuCung tc : kq) {
                            System.out.println("  " + tc);
                        }
                    }
                    case 0 -> back = true;
                }
            } catch (KetThucNhapException e) {
                throw e;
            } catch (ValidationException e) {
                System.out.println("  [Loi nghiep vu]: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("  [Loi]: " + e.getMessage());
            }
        }
    }

    // ================= 3. NHAN VIEN =================
    private void menuNhanVien() {
        boolean back = false;
        while (!back) {
            inTieuDe("QUAN LY NHAN VIEN");
            System.out.println("1. Danh sach nhan vien (kem tinh luong da hinh)");
            System.out.println("2. Them nhan vien moi");
            System.out.println("3. Sua nhan vien (ho tro dac tinh tung vai tro)");
            System.out.println("4. Xoa nhan vien");
            System.out.println("5. Tim kiem nhan vien");
            System.out.println("0. Quay lai");

            int chon = InputUtil.nhapSoNguyen(scanner, "Chon (0-5): ", 0, 5);
            try {
                switch (chon) {
                    case 1 -> {
                        DanhSach<NhanVien> list = service.getDsNhanVien();
                        System.out.printf("\nTong so: %d nhan vien%n", list.size());
                        for (NhanVien nv : list) {
                            System.out.println("  " + nv);
                        }
                    }
                    case 2 -> {
                        String ma = CuaHangService.sinhMaMoi(service.getDsNhanVien(), "NV");
                        System.out.println("Ma nhan vien moi: " + ma);
                        System.out.println("Chon vi tri: 1. Nhan vien cham soc | 2. Thu ngan | 3. Quan ly");
                        int vt = InputUtil.nhapSoNguyen(scanner, "Chon (1-3): ", 1, 3);
                        String hoTen = InputUtil.nhapChuoi(scanner, "Nhap ho ten: ");
                        String sdt = InputUtil.nhapChuoi(scanner, "Nhap so dien thoai: ");
                        String email = InputUtil.nhapChuoi(scanner, "Nhap email: ");
                        BigDecimal luongCB = InputUtil.nhapSoTien(scanner, "Nhap luong co ban: ", BigDecimal.ZERO);
                        double heSo = InputUtil.nhapSoThuc(scanner, "Nhap he so luong: ", 0.0);

                        if (vt == 1) {
                            String chuyenMon = InputUtil.nhapChuoi(scanner, "Nhap chuyen mon (VD: Cat tia, Spa): ");
                            BigDecimal phuCap = InputUtil.nhapSoTien(scanner, "Nhap phu cap nghe: ", BigDecimal.ZERO);
                            service.themNhanVien(new NhanVienChamSoc(ma, hoTen, sdt, email, luongCB, heSo, chuyenMon, phuCap));
                        } else if (vt == 2) {
                            String ca = InputUtil.nhapChuoi(scanner, "Nhap ca lam viec (VD: Ca sang, Ca chieu): ");
                            BigDecimal hoaHong = InputUtil.nhapSoTien(scanner, "Nhap hoa hong doanh so: ", BigDecimal.ZERO);
                            service.themNhanVien(new ThuNgan(ma, hoTen, sdt, email, luongCB, heSo, ca, hoaHong));
                        } else {
                            BigDecimal phuCapCV = InputUtil.nhapSoTien(scanner, "Nhap phu cap chuc vu: ", BigDecimal.ZERO);
                            service.themNhanVien(new NhanVienQuanLy(ma, hoTen, sdt, email, luongCB, heSo, phuCapCV));
                        }
                        System.out.println("  => Them nhan vien thanh cong!");
                    }
                    case 3 -> {
                        String ma = InputUtil.nhapChuoi(scanner, "Nhap ma nhan vien can sua: ");
                        NhanVien cu = CuaHangService.timTheoMa(service.getDsNhanVien(), ma);
                        if (cu == null) {
                            System.out.println("  [Loi] Khong tim thay nhan vien!");
                            break;
                        }
                        String hoTen = InputUtil.nhapChuoiTuyChon(scanner, "Ho ten [" + cu.getHoTen() + "]: ", cu.getHoTen());
                        String sdt = InputUtil.nhapChuoiTuyChon(scanner, "SDT [" + cu.getSoDienThoai() + "]: ", cu.getSoDienThoai());
                        String email = InputUtil.nhapChuoiTuyChon(scanner, "Email [" + cu.getEmail() + "]: ", cu.getEmail());
                        BigDecimal luongCB = InputUtil.nhapSoTien(scanner, "Luong co ban moi: ", BigDecimal.ZERO);
                        double heSo = InputUtil.nhapSoThuc(scanner, "He so luong moi: ", 0.0);

                        if (cu instanceof NhanVienChamSoc cs) {
                            String chuyenMon = InputUtil.nhapChuoiTuyChon(scanner, "Chuyen mon moi [" + cs.getChuyenMon() + "]: ", cs.getChuyenMon());
                            BigDecimal phuCap = InputUtil.nhapSoTien(scanner, "Phu cap moi [" + cs.getPhuCap() + "]: ", BigDecimal.ZERO);
                            service.suaNhanVienChamSoc(ma, hoTen, sdt, email, luongCB, heSo, chuyenMon, phuCap);
                        } else if (cu instanceof ThuNgan tn) {
                            String ca = InputUtil.nhapChuoiTuyChon(scanner, "Ca lam viec moi [" + tn.getCaLamViec() + "]: ", tn.getCaLamViec());
                            BigDecimal hoaHong = InputUtil.nhapSoTien(scanner, "Hoa hong moi [" + tn.getHoaHong() + "]: ", BigDecimal.ZERO);
                            service.suaThuNgan(ma, hoTen, sdt, email, luongCB, heSo, ca, hoaHong);
                        } else if (cu instanceof NhanVienQuanLy ql) {
                            BigDecimal phuCapCV = InputUtil.nhapSoTien(scanner, "Phu cap chuc vu moi [" + ql.getPhuCapChucVu() + "]: ", BigDecimal.ZERO);
                            service.suaNhanVienQuanLy(ma, hoTen, sdt, email, luongCB, heSo, phuCapCV);
                        }
                        System.out.println("  => Cap nhat nhan vien thanh cong!");
                    }
                    case 4 -> {
                        String ma = InputUtil.nhapChuoi(scanner, "Nhap ma nhan vien can xoa: ");
                        if (InputUtil.nhapCoKhong(scanner, "Ban co chac muon xoa nhan vien " + ma + "?")) {
                            service.xoaNhanVien(ma);
                            System.out.println("  => Xoa nhan vien thanh cong!");
                        }
                    }
                    case 5 -> {
                        String k = InputUtil.nhapChuoi(scanner, "Nhap tu khoa tim kiem: ");
                        DanhSach<NhanVien> kq = service.timKiemNhanVien(k);
                        System.out.printf("Tim thay %d nhan vien:%n", kq.size());
                        for (NhanVien nv : kq) {
                            System.out.println("  " + nv);
                        }
                    }
                    case 0 -> back = true;
                }
            } catch (KetThucNhapException e) {
                throw e;
            } catch (ValidationException e) {
                System.out.println("  [Loi nghiep vu]: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("  [Loi]: " + e.getMessage());
            }
        }
    }

    // ================= 4. DICH VU =================
    private void menuDichVu() {
        boolean back = false;
        while (!back) {
            inTieuDe("QUAN LY DICH VU");
            System.out.println("1. Danh sach dich vu");
            System.out.println("2. Them dich vu moi");
            System.out.println("3. Sua dich vu (ho tro dac tinh tung phan loai)");
            System.out.println("4. Xoa dich vu");
            System.out.println("5. Tim kiem dich vu");
            System.out.println("0. Quay lai");

            int chon = InputUtil.nhapSoNguyen(scanner, "Chon (0-5): ", 0, 5);
            try {
                switch (chon) {
                    case 1 -> {
                        DanhSach<DichVu> list = service.getDsDichVu();
                        System.out.printf("\nTong so: %d dich vu%n", list.size());
                        for (DichVu dv : list) {
                            System.out.println("  " + dv);
                        }
                    }
                    case 2 -> {
                        String ma = CuaHangService.sinhMaMoi(service.getDsDichVu(), "DV");
                        System.out.println("Ma dich vu moi: " + ma);
                        System.out.println("Chon loai: 1. Tam ve sinh | 2. Cat tia tao kieu | 3. Luu tru khach san");
                        int loai = InputUtil.nhapSoNguyen(scanner, "Chon (1-3): ", 1, 3);
                        String ten = InputUtil.nhapChuoi(scanner, "Nhap ten dich vu: ");
                        BigDecimal gia = InputUtil.nhapSoTien(scanner, "Nhap don gia co ban: ", BigDecimal.ZERO);
                        String moTa = InputUtil.nhapChuoi(scanner, "Nhap mo ta: ");

                        if (loai == 1) {
                            boolean sayKho = InputUtil.nhapCoKhong(scanner, "Co say kho khong?");
                            service.themDichVu(new DichVuTam(ma, ten, gia, moTa, sayKho));
                        } else if (loai == 2) {
                            String kieuCat = InputUtil.nhapChuoi(scanner, "Nhap kieu cat (VD: Tieu chuan, Tao kieu): ");
                            service.themDichVu(new DichVuCatTia(ma, ten, gia, moTa, kieuCat));
                        } else {
                            String loaiPhong = InputUtil.nhapChuoi(scanner, "Nhap loai phong (VD: Thuong, VIP): ");
                            service.themDichVu(new DichVuLuuTru(ma, ten, gia, moTa, loaiPhong));
                        }
                        System.out.println("  => Them dich vu thanh cong!");
                    }
                    case 3 -> {
                        String ma = InputUtil.nhapChuoi(scanner, "Nhap ma dich vu can sua: ");
                        DichVu cu = CuaHangService.timTheoMa(service.getDsDichVu(), ma);
                        if (cu == null) {
                            System.out.println("  [Loi] Khong tim thay dich vu!");
                            break;
                        }
                        String tenMoi = InputUtil.nhapChuoiTuyChon(scanner, "Ten moi [" + cu.getTen() + "]: ", cu.getTen());
                        BigDecimal giaMoi = InputUtil.nhapSoTien(scanner, "Don gia co ban moi: ", BigDecimal.ZERO);
                        String moTaMoi = InputUtil.nhapChuoiTuyChon(scanner, "Mo ta moi [" + cu.getMoTa() + "]: ", cu.getMoTa());

                        if (cu instanceof DichVuTam tam) {
                            boolean sayKhoMoi = InputUtil.nhapCoKhong(scanner, "Co say kho khong? (hien tai: " + tam.isCoSayKho() + ")");
                            service.suaDichVuTam(ma, tenMoi, giaMoi, moTaMoi, sayKhoMoi);
                        } else if (cu instanceof DichVuCatTia ct) {
                            String kieuMoi = InputUtil.nhapChuoiTuyChon(scanner, "Kieu cat moi [" + ct.getKieuCat() + "]: ", ct.getKieuCat());
                            service.suaDichVuCatTia(ma, tenMoi, giaMoi, moTaMoi, kieuMoi);
                        } else if (cu instanceof DichVuLuuTru lt) {
                            String phongMoi = InputUtil.nhapChuoiTuyChon(scanner, "Loai phong moi [" + lt.getLoaiPhong() + "]: ", lt.getLoaiPhong());
                            service.suaDichVuLuuTru(ma, tenMoi, giaMoi, moTaMoi, phongMoi);
                        }
                        System.out.println("  => Sua dich vu thanh cong!");
                    }
                    case 4 -> {
                        String ma = InputUtil.nhapChuoi(scanner, "Nhap ma dich vu can xoa: ");
                        if (InputUtil.nhapCoKhong(scanner, "Ban co chac muon xoa dich vu " + ma + "?")) {
                            service.xoaDichVu(ma);
                            System.out.println("  => Xoa dich vu thanh cong!");
                        }
                    }
                    case 5 -> {
                        String k = InputUtil.nhapChuoi(scanner, "Nhap tu khoa tim kiem (ma, ten, mo ta, dac tinh phu): ");
                        DanhSach<DichVu> kq = service.timKiemDichVu(k);
                        System.out.printf("Tim thay %d dich vu:%n", kq.size());
                        for (DichVu dv : kq) {
                            System.out.println("  " + dv);
                        }
                    }
                    case 0 -> back = true;
                }
            } catch (KetThucNhapException e) {
                throw e;
            } catch (ValidationException e) {
                System.out.println("  [Loi nghiep vu]: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("  [Loi]: " + e.getMessage());
            }
        }
    }

    // ================= 5. SAN PHAM =================
    private void menuSanPham() {
        boolean back = false;
        while (!back) {
            inTieuDe("QUAN LY SAN PHAM");
            System.out.println("1. Danh sach san pham");
            System.out.println("2. Them san pham moi");
            System.out.println("3. Sua san pham (ho tro dac tinh tung phan loai)");
            System.out.println("4. Xoa san pham");
            System.out.println("5. Tim kiem san pham");
            System.out.println("0. Quay lai");

            int chon = InputUtil.nhapSoNguyen(scanner, "Chon (0-5): ", 0, 5);
            try {
                switch (chon) {
                    case 1 -> {
                        DanhSach<SanPham> list = service.getDsSanPham();
                        System.out.printf("\nTong so: %d san pham%n", list.size());
                        for (SanPham sp : list) {
                            System.out.println("  " + sp);
                        }
                    }
                    case 2 -> {
                        String ma = CuaHangService.sinhMaMoi(service.getDsSanPham(), "SP");
                        System.out.println("Ma san pham moi: " + ma);
                        System.out.println("Chon loai: 1. Thuc an | 2. Phu kien");
                        int loai = InputUtil.nhapSoNguyen(scanner, "Chon (1-2): ", 1, 2);
                        String ten = InputUtil.nhapChuoi(scanner, "Nhap ten san pham: ");
                        BigDecimal donGia = InputUtil.nhapSoTien(scanner, "Nhap don gia: ", BigDecimal.ZERO);
                        int tonKho = InputUtil.nhapSoNguyen(scanner, "Nhap so luong ton kho: ", 0, 10000);

                        if (loai == 1) {
                            LocalDate hsd = InputUtil.nhapNgay(scanner, "Nhap han su dung (uuuu-MM-dd): ");
                            int gram = InputUtil.nhapSoNguyen(scanner, "Nhap trong luong (gram): ", 1, 100000);
                            service.themSanPham(new ThucAn(ma, ten, donGia, tonKho, hsd, gram));
                        } else {
                            String chatLieu = InputUtil.nhapChuoi(scanner, "Nhap chat lieu (VD: Vai, Nhua, Kim loai): ");
                            String kichCo = InputUtil.nhapChuoi(scanner, "Nhap kich co (VD: S, M, L): ");
                            service.themSanPham(new PhuKien(ma, ten, donGia, tonKho, chatLieu, kichCo));
                        }
                        System.out.println("  => Them san pham thanh cong!");
                    }
                    case 3 -> {
                        String ma = InputUtil.nhapChuoi(scanner, "Nhap ma san pham can sua: ");
                        SanPham cu = CuaHangService.timTheoMa(service.getDsSanPham(), ma);
                        if (cu == null) {
                            System.out.println("  [Loi] Khong tim thay san pham!");
                            break;
                        }
                        String tenMoi = InputUtil.nhapChuoiTuyChon(scanner, "Ten moi [" + cu.getTen() + "]: ", cu.getTen());
                        BigDecimal giaMoi = InputUtil.nhapSoTien(scanner, "Don gia moi: ", BigDecimal.ZERO);
                        int tonMoi = InputUtil.nhapSoNguyen(scanner, "Ton kho moi: ", 0, 10000);

                        if (cu instanceof ThucAn ta) {
                            LocalDate hsdMoi = InputUtil.nhapNgay(scanner, "HSD moi (uuuu-MM-dd): ");
                            int gramMoi = InputUtil.nhapSoNguyen(scanner, "Trong luong moi (gram): ", 1, 100000);
                            service.suaThucAn(ma, tenMoi, giaMoi, tonMoi, hsdMoi, gramMoi);
                        } else if (cu instanceof PhuKien pk) {
                            String chatLieuMoi = InputUtil.nhapChuoiTuyChon(scanner, "Chat lieu moi [" + pk.getChatLieu() + "]: ", pk.getChatLieu());
                            String kichCoMoi = InputUtil.nhapChuoiTuyChon(scanner, "Kich co moi [" + pk.getKichCo() + "]: ", pk.getKichCo());
                            service.suaPhuKien(ma, tenMoi, giaMoi, tonMoi, chatLieuMoi, kichCoMoi);
                        }
                        System.out.println("  => Sua san pham thanh cong!");
                    }
                    case 4 -> {
                        String ma = InputUtil.nhapChuoi(scanner, "Nhap ma san pham can xoa: ");
                        if (InputUtil.nhapCoKhong(scanner, "Ban co chac muon xoa san pham " + ma + "?")) {
                            service.xoaSanPham(ma);
                            System.out.println("  => Xoa san pham thanh cong!");
                        }
                    }
                    case 5 -> {
                        String k = InputUtil.nhapChuoi(scanner, "Nhap tu khoa tim kiem (ma, ten, trong luong, chat lieu): ");
                        DanhSach<SanPham> kq = service.timKiemSanPham(k);
                        System.out.printf("Tim thay %d san pham:%n", kq.size());
                        for (SanPham sp : kq) {
                            System.out.println("  " + sp);
                        }
                    }
                    case 0 -> back = true;
                }
            } catch (KetThucNhapException e) {
                throw e;
            } catch (ValidationException e) {
                System.out.println("  [Loi nghiep vu]: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("  [Loi]: " + e.getMessage());
            }
        }
    }

    // ================= 6. LICH HEN =================
    private void menuLichHen() {
        boolean back = false;
        while (!back) {
            inTieuDe("QUAN LY LICH HEN");
            System.out.println("1. Danh sach tat ca lich hen");
            System.out.println("2. Dat lich hen moi (yeu cau NV cham soc & kiem tra xung dot)");
            System.out.println("3. Sua lich hen");
            System.out.println("4. Cap nhat trang thai lich hen (Dat lich / Hoan thanh / Da huy)");
            System.out.println("5. Huy lich hen");
            System.out.println("6. Xoa lich hen");
            System.out.println("7. Tim kiem lich hen theo tu khoa");
            System.out.println("0. Quay lai");

            int chon = InputUtil.nhapSoNguyen(scanner, "Chon (0-7): ", 0, 7);
            try {
                switch (chon) {
                    case 1 -> {
                        DanhSach<LichHen> list = service.getDsLichHen();
                        System.out.printf("\nTong so: %d lich hen%n", list.size());
                        for (LichHen lh : list) {
                            System.out.println("  " + lh);
                        }
                    }
                    case 2 -> {
                        String ma = CuaHangService.sinhMaMoi(service.getDsLichHen(), "LH");
                        System.out.println("Ma lich hen moi: " + ma);
                        String maKH = InputUtil.nhapChuoi(scanner, "Nhap ma khach hang: ");
                        String maTC = InputUtil.nhapChuoi(scanner, "Nhap ma thu cung: ");
                        String maDV = InputUtil.nhapChuoi(scanner, "Nhap ma dich vu: ");
                        String maNV = InputUtil.nhapChuoi(scanner, "Nhap ma nhan vien cham soc (NhanVienChamSoc): ");
                        LocalDateTime batDau = InputUtil.nhapNgayGio(scanner, "Thoi gian bat dau (uuuu-MM-dd HH:mm): ");
                        LocalDateTime ketThuc = InputUtil.nhapNgayGio(scanner, "Thoi gian ket thuc (uuuu-MM-dd HH:mm): ");
                        String ghiChu = InputUtil.nhapChuoiTuyChon(scanner, "Ghi chu (co the bo trong): ", "");

                        LichHen lh = new LichHen(ma, maKH, maTC, maDV, maNV, batDau, ketThuc, TrangThaiLichHen.DAT_LICH, ghiChu);
                        service.datLichHen(lh);
                        System.out.println("  => Dat lich hen thanh cong!");
                    }
                    case 3 -> {
                        String ma = InputUtil.nhapChuoi(scanner, "Nhap ma lich hen can sua: ");
                        LichHen cu = CuaHangService.timTheoMa(service.getDsLichHen(), ma);
                        if (cu == null) {
                            System.out.println("  [Loi] Khong tim thay lich hen!");
                            break;
                        }
                        String maDVMoi = InputUtil.nhapChuoiTuyChon(scanner, "Ma dich vu moi [" + cu.getMaDichVu() + "]: ", cu.getMaDichVu());
                        String maNVMoi = InputUtil.nhapChuoiTuyChon(scanner, "Ma nhan vien cham soc moi [" + cu.getMaNhanVien() + "]: ", cu.getMaNhanVien());
                        LocalDateTime batDau = InputUtil.nhapNgayGio(scanner, "Thoi gian bat dau moi: ");
                        LocalDateTime ketThuc = InputUtil.nhapNgayGio(scanner, "Thoi gian ket thuc moi: ");
                        String ghiChu = InputUtil.nhapChuoiTuyChon(scanner, "Ghi chu moi [" + cu.getGhiChu() + "]: ", cu.getGhiChu());

                        service.suaLichHen(ma, maDVMoi, maNVMoi, batDau, ketThuc, ghiChu);
                        System.out.println("  => Sua lich hen thanh cong!");
                    }
                    case 4 -> {
                        String ma = InputUtil.nhapChuoi(scanner, "Nhap ma lich hen: ");
                        System.out.println("Chon trang thai: 1. DAT_LICH | 2. HOAN_THANH | 3. DA_HUY");
                        int tt = InputUtil.nhapSoNguyen(scanner, "Chon (1-3): ", 1, 3);
                        TrangThaiLichHen trangThaiMoi = (tt == 1) ? TrangThaiLichHen.DAT_LICH : (tt == 2 ? TrangThaiLichHen.HOAN_THANH : TrangThaiLichHen.DA_HUY);
                        service.capNhatTrangThaiLichHen(ma, trangThaiMoi);
                        System.out.println("  => Cap nhat trang thai lich hen thanh cong!");
                    }
                    case 5 -> {
                        String ma = InputUtil.nhapChuoi(scanner, "Nhap ma lich hen muon huy: ");
                        service.capNhatTrangThaiLichHen(ma, TrangThaiLichHen.DA_HUY);
                        System.out.println("  => Da huy lich hen thanh cong! Khoang gio nay da duoc giai phong cho nhan vien & thu cung.");
                    }
                    case 6 -> {
                        String ma = InputUtil.nhapChuoi(scanner, "Nhap ma lich hen can xoa: ");
                        if (InputUtil.nhapCoKhong(scanner, "Ban co chac muon xoa lich hen " + ma + "?")) {
                            service.xoaLichHen(ma);
                            System.out.println("  => Xoa lich hen thanh cong!");
                        }
                    }
                    case 7 -> {
                        String k = InputUtil.nhapChuoi(scanner, "Nhap tu khoa tim kiem (ma, khach, thu cung, NV, dich vu, ghi chu): ");
                        DanhSach<LichHen> kq = service.timKiemLichHen(k);
                        System.out.printf("Tim thay %d lich hen:%n", kq.size());
                        for (LichHen lh : kq) {
                            System.out.println("  " + lh);
                        }
                    }
                    case 0 -> back = true;
                }
            } catch (KetThucNhapException e) {
                throw e;
            } catch (ValidationException e) {
                System.out.println("  [Loi nghiep vu]: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("  [Loi]: " + e.getMessage());
            }
        }
    }

    // ================= 7. HOA DON =================
    private void menuHoaDon() {
        boolean back = false;
        while (!back) {
            inTieuDe("QUAN LY HOA DON & THANH TOAN");
            System.out.println("1. Danh sach hoa don");
            System.out.println("2. Tao hoa don nhap moi");
            System.out.println("3. Xem chi tiet hoa don");
            System.out.println("4. Them muc vao hoa don nhap (San pham / Dich vu)");
            System.out.println("5. Sua so luong muc trong hoa don nhap");
            System.out.println("6. Xoa muc khoi hoa don nhap");
            System.out.println("7. Xoa hoa don nhap");
            System.out.println("8. Thanh toan hoa don (kiem tra toan bo ton kho & tru kho)");
            System.out.println("9. Tim kiem hoa don (ma, ma khach, ma nhan vien, ngay, trang thai)");
            System.out.println("0. Quay lai");

            int chon = InputUtil.nhapSoNguyen(scanner, "Chon (0-9): ", 0, 9);
            try {
                switch (chon) {
                    case 1 -> {
                        DanhSach<HoaDon> list = service.getDsHoaDon();
                        System.out.printf("\nTong so: %d hoa don%n", list.size());
                        for (HoaDon hd : list) {
                            System.out.println("  " + hd);
                        }
                    }
                    case 2 -> {
                        String ma = CuaHangService.sinhMaMoi(service.getDsHoaDon(), "HD");
                        System.out.println("Ma hoa don moi: " + ma);
                        String maKH = InputUtil.nhapChuoi(scanner, "Nhap ma khach hang: ");
                        String maNV = InputUtil.nhapChuoi(scanner, "Nhap ma nhan vien lap hoa don: ");
                        HoaDon hd = new HoaDon(ma, maKH, maNV, LocalDateTime.now(), TrangThaiHoaDon.CHUA_THANH_TOAN, null, BigDecimal.ZERO);
                        service.taoHoaDonNhap(hd);
                        System.out.println("  => Tao hoa don nhap thanh cong!");
                    }
                    case 3 -> {
                        String ma = InputUtil.nhapChuoi(scanner, "Nhap ma hoa don can xem: ");
                        HoaDon hd = CuaHangService.timTheoMa(service.getDsHoaDon(), ma);
                        if (hd == null) {
                            System.out.println("  [Loi] Khong tim thay hoa don!");
                            break;
                        }
                        inChiTietHoaDonDayDu(hd);
                    }
                    case 4 -> {
                        String maHD = InputUtil.nhapChuoi(scanner, "Nhap ma hoa don nhap: ");
                        String maMuc = InputUtil.nhapChuoi(scanner, "Nhap ma san pham hoac dich vu: ");
                        int soLuong = InputUtil.nhapSoNguyen(scanner, "Nhap so luong: ", 1, 1000);
                        service.themMucVaoHoaDon(maHD, maMuc, soLuong);
                        System.out.println("  => Them muc vao hoa don thanh cong!");
                    }
                    case 5 -> {
                        String maHD = InputUtil.nhapChuoi(scanner, "Nhap ma hoa don: ");
                        HoaDon hd = CuaHangService.timTheoMa(service.getDsHoaDon(), maHD);
                        if (hd == null) {
                            System.out.println("  [Loi] Khong tim thay hoa don!");
                            break;
                        }
                        inChiTietHoaDonDayDu(hd);
                        if (hd.getDanhSachChiTiet().isEmpty()) {
                            break;
                        }
                        int stt = InputUtil.nhapSoNguyen(scanner, "Nhap STT muc can sua (1-" + hd.getDanhSachChiTiet().size() + "): ", 1, hd.getDanhSachChiTiet().size());
                        int soLuongMoi = InputUtil.nhapSoNguyen(scanner, "Nhap so luong moi: ", 1, 1000);
                        service.suaSoLuongMucHoaDon(maHD, stt - 1, soLuongMoi);
                        System.out.println("  => Cap nhat so luong thanh cong!");
                    }
                    case 6 -> {
                        String maHD = InputUtil.nhapChuoi(scanner, "Nhap ma hoa don: ");
                        HoaDon hd = CuaHangService.timTheoMa(service.getDsHoaDon(), maHD);
                        if (hd == null) {
                            System.out.println("  [Loi] Khong tim thay hoa don!");
                            break;
                        }
                        inChiTietHoaDonDayDu(hd);
                        if (hd.getDanhSachChiTiet().isEmpty()) {
                            break;
                        }
                        int stt = InputUtil.nhapSoNguyen(scanner, "Nhap STT muc can xoa (1-" + hd.getDanhSachChiTiet().size() + "): ", 1, hd.getDanhSachChiTiet().size());
                        service.xoaMucKhoiHoaDon(maHD, stt - 1);
                        System.out.println("  => Xoa muc thanh cong!");
                    }
                    case 7 -> {
                        String maHD = InputUtil.nhapChuoi(scanner, "Nhap ma hoa don can xoa: ");
                        if (InputUtil.nhapCoKhong(scanner, "Ban chac chan muon xoa hoa don " + maHD + "?")) {
                            service.xoaHoaDon(maHD);
                            System.out.println("  => Xoa hoa don nhap thanh cong!");
                        }
                    }
                    case 8 -> {
                        String maHD = InputUtil.nhapChuoi(scanner, "Nhap ma hoa don can thanh toan: ");
                        HoaDon hd = CuaHangService.timTheoMa(service.getDsHoaDon(), maHD);
                        if (hd == null) {
                            System.out.println("  [Loi] Khong tim thay hoa don!");
                            break;
                        }
                        inChiTietHoaDonDayDu(hd);
                        if (InputUtil.nhapCoKhong(scanner, "Xac nhan thanh toan hoa don " + maHD + "?")) {
                            service.thanhToanHoaDon(maHD);
                            System.out.println("  => THANH TOAN THANH CONG! Kho hang da duoc tru tu dong va hoa don duoc chuyen sang trang thai bat bien.");
                        }
                    }
                    case 9 -> {
                        String k = InputUtil.nhapChuoi(scanner, "Nhap tu khoa tim kiem hoa don: ");
                        DanhSach<HoaDon> ketQua = service.timKiemHoaDon(k);
                        System.out.printf("Tim thay %d hoa don:%n", ketQua.size());
                        for (HoaDon hd : ketQua) {
                            System.out.println("  " + hd);
                        }
                    }
                    case 0 -> back = true;
                }
            } catch (KetThucNhapException e) {
                throw e;
            } catch (ValidationException e) {
                System.out.println("  [Loi nghiep vu]: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("  [Loi]: " + e.getMessage());
            }
        }
    }

    private void inChiTietHoaDonDayDu(HoaDon hd) {
        System.out.println("----------------------------------------------");
        System.out.println("HOA DON: " + hd.getMa() + " | Trang thai: " + hd.getTrangThai().getMoTa());
        System.out.println("Khach hang: " + hd.getMaKhachHang() + " | Nhan vien: " + hd.getMaNhanVien());
        System.out.println("Ngay tao: " + DinhDangUtil.dinhDangNgayGio(hd.getNgayTao())
                + " | Ngay thanh toan: " + DinhDangUtil.dinhDangNgayGio(hd.getNgayThanhToan()));
        System.out.println("Cac dong mat hang/dich vu (Snapshot):");
        int idx = 1;
        for (ChiTietHoaDon ct : hd.getDanhSachChiTiet()) {
            System.out.printf("  %d. %s%n", idx++, ct);
        }
        System.out.println("TONG TIEN: " + DinhDangUtil.dinhDangTien(hd.getTongTien()));
        System.out.println("----------------------------------------------");
    }

    // ================= 8. BAO CAO & THONG KE =================
    private void menuBaoCao() {
        boolean back = false;
        while (!back) {
            inTieuDe("BAO CAO & THONG KE");
            System.out.println("1. Tong doanh thu thuc te (Cac hoa don da thanh toan)");
            System.out.println("2. Danh sach san pham ton kho thap (canh bao nhap hang)");
            System.out.println("3. Danh sach lich hen theo ngay");
            System.out.println("0. Quay lai");

            int chon = InputUtil.nhapSoNguyen(scanner, "Chon (0-3): ", 0, 3);
            try {
                switch (chon) {
                    case 1 -> {
                        BigDecimal dt = service.tinhTongDoanhThuDaThanhToan();
                        System.out.println("\n>>> TONG DOANH THU DA THANH TOAN: " + DinhDangUtil.dinhDangTien(dt));
                    }
                    case 2 -> {
                        int nguong = InputUtil.nhapSoNguyen(scanner, "Nhap nguong ton kho canh bao (VD: 5): ", 0, 1000);
                        DanhSach<SanPham> tonThap = service.layDanhSachSanPhamTonThap(nguong);
                        System.out.printf("\nCo %d san pham ton kho <= %d:%n", tonThap.size(), nguong);
                        for (SanPham sp : tonThap) {
                            System.out.printf("  - [%s] %s | Ton: %d | Gia: %s%n",
                                    sp.getMa(), sp.getTen(), sp.getSoLuongTon(), DinhDangUtil.dinhDangTien(sp.getDonGia()));
                        }
                    }
                    case 3 -> {
                        LocalDate ngay = InputUtil.nhapNgay(scanner, "Nhap ngay can tra cuu (uuuu-MM-dd): ");
                        DanhSach<LichHen> dsNgay = service.layLichHenTheoNgay(ngay);
                        System.out.printf("\nCo %d lich hen trong ngay %s:%n", dsNgay.size(), ngay);
                        for (LichHen lh : dsNgay) {
                            System.out.println("  " + lh);
                        }
                    }
                    case 0 -> back = true;
                }
            } catch (KetThucNhapException e) {
                throw e;
            } catch (Exception e) {
                System.out.println("  [Loi]: " + e.getMessage());
            }
        }
    }
}
