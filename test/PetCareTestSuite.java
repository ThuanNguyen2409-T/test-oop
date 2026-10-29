import petcare.collection.DanhSach;
import petcare.interfaces.TinhTien;
import petcare.model.*;
import petcare.persistence.DuLieuException;
import petcare.service.CuaHangService;
import petcare.service.ValidationException;
import petcare.util.DinhDangUtil;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Comparator;

/**
 * Bo kiem thu tu dong dua tren assert (java -ea) khong phu thuoc thu vien ben ngoai.
 * Bao gom day du cac test hoi quy (regression tests) xac thuc tinh dung dan cua toan bo nghiep vu.
 */
public class PetCareTestSuite {

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("  BAT DAU CHAY PET-01 REGRESSION SUITE (java -ea) ");
        System.out.println("=================================================");

        boolean assertionsEnabled = false;
        assert assertionsEnabled = true;
        if (!assertionsEnabled) {
            System.err.println("[CANH BAO] Chua bat che do assertion! Vui long chay voi: java -ea -cp bin PetCareTestSuite");
            System.exit(1);
        }

        try {
            testDanhSachArrayResizeAndDelete();
            System.out.println("[PASS] 1. Test DanhSach<T> dong: mo rong kich thuoc, truy cap va xoa phan tu.");

            testPolymorphismTotals();
            System.out.println("[PASS] 2. Test Da hinh (Polymorphism): tinh gia dich vu, tinh luong va interface TinhTien.");

            testFullSaveLoadSubtypesAndSnapshots();
            System.out.println("[PASS] 3. Test Luu & Nap tep tin toan dien: luu giu day du subtype va co lap snapshot.");

            testDuplicateIdAfterReload();
            System.out.println("[PASS] 4. Test Sinh ma duy nhat sau reload va chan trung ma ID.");

            testServiceInvoiceEditAndCatalogPriceEditAndReloadRegression();
            System.out.println("[PASS] 5. Test Hoi quy: snapshot don gia hieu dung (kem phu phi), sua so luong va co lap gia danh muc.");

            testInvoiceTotalsAndPaidMetadataValidation();
            System.out.println("[PASS] 6. Test Xac thuc tong tien, thanh tien dong va tinh toan ven metadata hoa don.");

            testAppointmentStaffRoleAndDuplicateIdAndReactivationConflict();
            System.out.println("[PASS] 7. Test Lich hen: bat buoc NV cham soc, chan trung ID, chan kich hoat lich xung dot, cho phep gio lien ke.");

            testStrictFileParsingAndSecurityRejection();
            System.out.println("[PASS] 8. Test Parser nghiem ngat: bat buoc header, tu choi section la, cot thua/thieu, boolean sai, escape loi.");

            testStrictDateAndNonFiniteRejection();
            System.out.println("[PASS] 9. Test Strict date uuuu-MM-dd (2026-02-30) va tu choi so thuc khong huu han (Infinity/NaN).");

            testForcedSaveFailureRollback();
            System.out.println("[PASS] 10. Test Nguyen tu giao dich trong bo nho: rollback trang thai khi ghi dia that bai.");

            testPaymentInsufficientStockAndDuplicateLinesAndRepeatPayment();
            System.out.println("[PASS] 11. Test Thanh toan: gom dong trung lap, kiem tra ton kho toan bo, tru 1 lan va chan tra lap.");

            testAppointmentConflictsAndCrudConstraints();
            System.out.println("[PASS] 12. Test Xung dot lich hen va rang buoc toan ven tham chieu CRUD (chan xoa ca khi co snapshot).");

            testOwnerReviewProbeRegressionAndPaidInvoiceImmutability();
            System.out.println("[PASS] 13. Test ReviewProbe hoi quy: tham chieu dong hoa don, dung loai muc va khoa bat bien hoa don da thanh toan.");

            testPreSaveStagingValidationAndNegativeFileIntegrity();
            System.out.println("[PASS] 14. Test Validate Staging truoc moi lan luu, tu choi draft sai metadata va rollback toan ven khi edit loi.");

            System.out.println("=================================================");
            System.out.println("  TAT CA 14 NHOM KIEM THU DA PASS THANH CONG!   ");
            System.out.println("=================================================");
        } catch (Throwable t) {
            System.err.println("\n[FAIL] KIEM THU THAT BAI!");
            t.printStackTrace();
            System.exit(1);
        }
    }

    private static void testDanhSachArrayResizeAndDelete() {
        DanhSach<String> ds = new DanhSach<>(2);
        assert ds.size() == 0;
        assert ds.isEmpty();

        ds.add("Item1");
        ds.add("Item2");
        assert ds.size() == 2;
        assert ds.getSucChua() == 2;

        ds.add("Item3");
        assert ds.size() == 3;
        assert ds.getSucChua() >= 3;
        assert "Item1".equals(ds.get(0));
        assert "Item2".equals(ds.get(1));
        assert "Item3".equals(ds.get(2));

        String removed = ds.remove(1);
        assert "Item2".equals(removed);
        assert ds.size() == 2;
        assert "Item3".equals(ds.get(1));

        boolean xoaDuoc = ds.remove("Item1");
        assert xoaDuoc;
        assert ds.size() == 1;
        assert "Item3".equals(ds.get(0));

        int dem = 0;
        for (String s : ds) {
            dem++;
            assert "Item3".equals(s);
        }
        assert dem == 1;

        ds.clear();
        assert ds.isEmpty();
    }

    private static void testPolymorphismTotals() {
        DichVu tamSayKho = new DichVuTam("DV_T1", "Tam say kho", new BigDecimal("150000"), "Mo ta", true);
        DichVu tamThuong = new DichVuTam("DV_T2", "Tam thuong", new BigDecimal("150000"), "Mo ta", false);
        DichVu catTiaTaoKieu = new DichVuCatTia("DV_C1", "Cat tia tao kieu", new BigDecimal("200000"), "Mo ta", "Tao kieu VIP");
        DichVu catTiaTieuChuan = new DichVuCatTia("DV_C2", "Cat tieu chuan", new BigDecimal("200000"), "Mo ta", "Tieu chuan");
        DichVu luuTruVip = new DichVuLuuTru("DV_L1", "Khach san VIP", new BigDecimal("100000"), "Mo ta", "VIP");

        assert new BigDecimal("170000").compareTo(tamSayKho.tinhGia(1)) == 0;
        assert new BigDecimal("340000").compareTo(tamSayKho.tinhGia(2)) == 0;
        assert new BigDecimal("150000").compareTo(tamThuong.tinhGia(1)) == 0;

        assert new BigDecimal("250000").compareTo(catTiaTaoKieu.tinhGia(1)) == 0;
        assert new BigDecimal("200000").compareTo(catTiaTieuChuan.tinhGia(1)) == 0;
        assert new BigDecimal("600000").compareTo(luuTruVip.tinhGia(3)) == 0;

        SanPham thucAn = new ThucAn("SP_T1", "Hat kho", new BigDecimal("100000"), 10, LocalDate.now().plusMonths(6), 500);
        TinhTien item1 = tamSayKho;
        TinhTien item2 = thucAn;
        assert new BigDecimal("170000").compareTo(item1.tinhThanhTien(1)) == 0;
        assert new BigDecimal("300000").compareTo(item2.tinhThanhTien(3)) == 0;

        NhanVien nvChamSoc = new NhanVienChamSoc("NV1", "CS", "0912345678", "cs@mail.com",
                new BigDecimal("6000000"), 1.2, "Spa", new BigDecimal("1000000"));
        NhanVien thuNgan = new ThuNgan("NV2", "TN", "0912345679", "tn@mail.com",
                new BigDecimal("5000000"), 1.0, "Sang", new BigDecimal("500000"));
        NhanVien quanLy = new NhanVienQuanLy("NV3", "QL", "0912345670", "ql@mail.com",
                new BigDecimal("10000000"), 1.5, new BigDecimal("2000000"));

        assert new BigDecimal("8200000.00").compareTo(nvChamSoc.tinhLuong()) == 0;
        assert new BigDecimal("5500000.00").compareTo(thuNgan.tinhLuong()) == 0;
        assert new BigDecimal("17000000.00").compareTo(quanLy.tinhLuong()) == 0;
    }

    private static void testFullSaveLoadSubtypesAndSnapshots() throws Exception {
        Path tempDir = Files.createTempDirectory("petcare_test_saveload");
        Path testFile = tempDir.resolve("test_data.txt");
        Files.copy(Path.of("data.txt"), testFile);

        CuaHangService service = new CuaHangService(testFile);
        service.napDuLieu();

        assert service.getDsThuCung().size() >= 5;
        ThuCung tc1 = CuaHangService.timTheoMa(service.getDsThuCung(), "TC001");
        assert tc1 instanceof Cho;
        assert "Poodle".equals(((Cho) tc1).getGiongCho());

        ThuCung tc2 = CuaHangService.timTheoMa(service.getDsThuCung(), "TC002");
        assert tc2 instanceof Meo;
        assert "Trang vang".equals(((Meo) tc2).getMauLong());

        NhanVien nv1 = CuaHangService.timTheoMa(service.getDsNhanVien(), "NV001");
        assert nv1 instanceof NhanVienChamSoc;
        NhanVien nv3 = CuaHangService.timTheoMa(service.getDsNhanVien(), "NV003");
        assert nv3 instanceof ThuNgan;
        NhanVien nv4 = CuaHangService.timTheoMa(service.getDsNhanVien(), "NV004");
        assert nv4 instanceof NhanVienQuanLy;
    }

    private static void testDuplicateIdAfterReload() throws Exception {
        Path tempDir = Files.createTempDirectory("petcare_test_dup");
        Path testFile = tempDir.resolve("test_dup.txt");
        Files.copy(Path.of("data.txt"), testFile);

        CuaHangService service = new CuaHangService(testFile);
        service.napDuLieu();

        String maSinh = CuaHangService.sinhMaMoi(service.getDsKhachHang(), "KH");
        assert "KH005".equals(maSinh);
        assert service.timKiemHoaDon("hd001").size() == 1;
        assert service.timKiemHoaDon("KH001").get(0).getMa().equals("HD001");
        assert service.timKiemHoaDon("khong-ton-tai").isEmpty();

        KhachHang khMoi = new KhachHang(maSinh, "Khach 5", "0900000005", "k5@mail.com", "Dia chi", LocalDate.now());
        service.themKhachHang(khMoi);

        boolean batDuocLoiTrung = false;
        try {
            service.themKhachHang(new KhachHang("KH005", "Khach Trung", "0900000005", "kt@mail.com", "Dia chi", LocalDate.now()));
        } catch (ValidationException e) {
            batDuocLoiTrung = true;
        }
        assert batDuocLoiTrung;

        String maSinhTiep = CuaHangService.sinhMaMoi(service.getDsKhachHang(), "KH");
        assert "KH006".equals(maSinhTiep);
    }

    private static void testServiceInvoiceEditAndCatalogPriceEditAndReloadRegression() throws Exception {
        Path tempDir = Files.createTempDirectory("petcare_test_reg_service");
        Path testFile = tempDir.resolve("test_reg.txt");
        Files.copy(Path.of("data.txt"), testFile);

        CuaHangService service = new CuaHangService(testFile);
        service.napDuLieu();

        // Dich vu tam DV001 co say kho: don gia co ban 150000, tinhGia(1) = 170000 (kem 20k say kho)
        DichVuTam dvTam = (DichVuTam) CuaHangService.timTheoMa(service.getDsDichVu(), "DV001");
        assert dvTam.isCoSayKho();

        String maHD = "HD_REG_1";
        HoaDon hd = new HoaDon(maHD, "KH001", "NV003", LocalDateTime.now(), TrangThaiHoaDon.CHUA_THANH_TOAN, null, BigDecimal.ZERO);
        service.taoHoaDonNhap(hd);

        // Them 1 lan DV001 vao hoa don
        service.themMucVaoHoaDon(maHD, "DV001", 1);
        HoaDon hdLay = CuaHangService.timTheoMa(service.getDsHoaDon(), maHD);
        ChiTietHoaDon ct = hdLay.getDanhSachChiTiet().get(0);

        // Snapshot don gia hieu dung phai la 170000 (khong phai 150000 gia goc)
        assert new BigDecimal("170000").compareTo(ct.getDonGia()) == 0;
        assert new BigDecimal("170000").compareTo(ct.getThanhTien()) == 0;

        // Sua so luong len 3
        service.suaSoLuongMucHoaDon(maHD, 0, 3);
        hdLay = CuaHangService.timTheoMa(service.getDsHoaDon(), maHD);
        ct = hdLay.getDanhSachChiTiet().get(0);

        // Thanh tien phai la 170000 * 3 = 510000 (bao toan phu phi)
        assert new BigDecimal("170000").compareTo(ct.getDonGia()) == 0;
        assert new BigDecimal("510000").compareTo(ct.getThanhTien()) == 0;
        assert new BigDecimal("510000").compareTo(hdLay.getTongTien()) == 0;

        // Thay doi gia trong danh muc goc len 999.000d va tat say kho
        service.suaDichVuTam("DV001", "Tam Doi Gia", new BigDecimal("999000"), "Mo ta moi", false);

        // Hoa don nhap da luu van phai giu nguyen don gia snapshot 170000 va thanh tien 510000
        hdLay = CuaHangService.timTheoMa(service.getDsHoaDon(), maHD);
        ct = hdLay.getDanhSachChiTiet().get(0);
        assert new BigDecimal("170000").compareTo(ct.getDonGia()) == 0;
        assert new BigDecimal("510000").compareTo(ct.getThanhTien()) == 0;

        // Reload tu dia de xac thuc
        CuaHangService reloaded = new CuaHangService(testFile);
        reloaded.napDuLieu();
        HoaDon hdReload = CuaHangService.timTheoMa(reloaded.getDsHoaDon(), maHD);
        ChiTietHoaDon ctReload = hdReload.getDanhSachChiTiet().get(0);
        assert new BigDecimal("170000").compareTo(ctReload.getDonGia()) == 0;
        assert new BigDecimal("510000").compareTo(ctReload.getThanhTien()) == 0;
        assert new BigDecimal("510000").compareTo(hdReload.getTongTien()) == 0;
    }

    private static void testInvoiceTotalsAndPaidMetadataValidation() throws Exception {
        Path tempDir = Files.createTempDirectory("petcare_test_inv_val");

        // 1. Hoa don co thanh tien dong sai lech (line total != unit * qty)
        Path badLineFile = tempDir.resolve("bad_line.txt");
        String badLineContent = "# PETCARE_STORE_DATA_V1\n\n"
                + "[KHACH_HANG]\nKH001|An|0912345678|an@mail.com|DC|2026-01-01\n\n"
                + "[NHAN_VIEN]\nTHU_NGAN|NV003|Ngan|0933445566|ngan@mail.com|5000000|1.0|Sang|0\n\n"
                + "[HOA_DON]\nHD001|KH001|NV003|2026-10-25 10:00|CHUA_THANH_TOAN||999000\n\n"
                + "[CHI_TIET_HOA_DON]\nHD001|SP001|Hat|SAN_PHAM|100000|2|999000\n";
        Files.writeString(badLineFile, badLineContent);
        CuaHangService s1 = new CuaHangService(badLineFile);
        boolean batLoiLine = false;
        try {
            s1.napDuLieu();
        } catch (ValidationException e) {
            batLoiLine = true;
        }
        assert batLoiLine : "Phai bat duoc loi thanh tien chi tiet khong khop don gia * so luong";

        // 2. Hoa don da thanh toan nhung thieu ngay thanh toan
        Path badPaidFile = tempDir.resolve("bad_paid.txt");
        String badPaidContent = "# PETCARE_STORE_DATA_V1\n\n"
                + "[KHACH_HANG]\nKH001|An|0912345678|an@mail.com|DC|2026-01-01\n\n"
                + "[NHAN_VIEN]\nTHU_NGAN|NV003|Ngan|0933445566|ngan@mail.com|5000000|1.0|Sang|0\n\n"
                + "[HOA_DON]\nHD001|KH001|NV003|2026-10-25 10:00|DA_THANH_TOAN||200000\n\n"
                + "[CHI_TIET_HOA_DON]\nHD001|SP001|Hat|SAN_PHAM|100000|2|200000\n";
        Files.writeString(badPaidFile, badPaidContent);
        CuaHangService s2 = new CuaHangService(badPaidFile);
        boolean batLoiPaid = false;
        try {
            s2.napDuLieu();
        } catch (ValidationException e) {
            batLoiPaid = true;
        }
        assert batLoiPaid : "Phai bat duoc hoa don da thanh toan thieu ngay thanh toan";

        // 3. Hoa don chua thanh toan nhung lai co ngay thanh toan
        Path badDraftFile = tempDir.resolve("bad_draft.txt");
        String badDraftContent = "# PETCARE_STORE_DATA_V1\n\n"
                + "[KHACH_HANG]\nKH001|An|0912345678|an@mail.com|DC|2026-01-01\n\n"
                + "[NHAN_VIEN]\nTHU_NGAN|NV003|Ngan|0933445566|ngan@mail.com|5000000|1.0|Sang|0\n\n"
                + "[HOA_DON]\nHD001|KH001|NV003|2026-10-25 10:00|CHUA_THANH_TOAN|2026-10-25 10:30|200000\n\n"
                + "[CHI_TIET_HOA_DON]\nHD001|SP001|Hat|SAN_PHAM|100000|2|200000\n";
        Files.writeString(badDraftFile, badDraftContent);
        CuaHangService s3 = new CuaHangService(badDraftFile);
        boolean batLoiDraft = false;
        try {
            s3.napDuLieu();
        } catch (ValidationException e) {
            batLoiDraft = true;
        }
        assert batLoiDraft : "Phai bat duoc hoa don chua thanh toan nhung lai co ngay thanh toan";
    }

    private static void testAppointmentStaffRoleAndDuplicateIdAndReactivationConflict() throws Exception {
        Path tempDir = Files.createTempDirectory("petcare_test_app_role");
        Path testFile = tempDir.resolve("test_app.txt");
        Files.copy(Path.of("data.txt"), testFile);

        CuaHangService service = new CuaHangService(testFile);
        service.napDuLieu();

        // 1. Thu tao lich hen phan cong cho Thu Ngan (NV003) -> Phai bi chan
        LichHen lhThuNgan = new LichHen("LH_TN", "KH001", "TC001", "DV001", "NV003",
                LocalDateTime.of(2026, 11, 1, 9, 0),
                LocalDateTime.of(2026, 11, 1, 10, 0),
                TrangThaiLichHen.DAT_LICH, "Phan cong sai vai tro");
        boolean batLoiRole = false;
        try {
            service.datLichHen(lhThuNgan);
        } catch (ValidationException e) {
            batLoiRole = true;
        }
        assert batLoiRole : "Phai chan khong cho phep phan cong lich hen cho nhan vien khong phai NhanVienChamSoc";

        // 2. Thu tao lich hen voi ma trung lap (LH001 da ton tai) -> Phai bi chan
        LichHen lhTrungMa = new LichHen("LH001", "KH001", "TC001", "DV001", "NV001",
                LocalDateTime.of(2026, 11, 2, 9, 0),
                LocalDateTime.of(2026, 11, 2, 10, 0),
                TrangThaiLichHen.DAT_LICH, "Trung ma");
        boolean batLoiTrungMa = false;
        try {
            service.datLichHen(lhTrungMa);
        } catch (ValidationException e) {
            batLoiTrungMa = true;
        }
        assert batLoiTrungMa : "Phai chan tao lich hen trung ma ID";

        // 3. Cho phep khoang gio lien ke (Adjacent intervals khong xung dot)
        // LH001 chay tu 09:00 den 10:30 voi NV001 va TC001.
        // Dat ca tiep theo ngay tu 10:30 den 11:30 voi NV001 va TC002 (chu KH001) phai thanh cong!
        LichHen lhLienKe = new LichHen("LH_LIEN_KE", "KH001", "TC002", "DV001", "NV001",
                LocalDateTime.of(2026, 10, 26, 10, 30),
                LocalDateTime.of(2026, 10, 26, 11, 30),
                TrangThaiLichHen.DAT_LICH, "Lien ke ca truoc");
        service.datLichHen(lhLienKe);
        assert CuaHangService.timTheoMa(service.getDsLichHen(), "LH_LIEN_KE") != null;

        // 4. Chuyen trang thai tu DA_HUY sang DAT_LICH khi bi xung dot gio -> Phai bi chan
        // Dat lich moi LH_ACTIVE chiem gio 2026-10-24 tu 09:00 den 11:00 voi NV002
        LichHen lhActive = new LichHen("LH_ACT", "KH001", "TC001", "DV001", "NV002",
                LocalDateTime.of(2026, 10, 24, 9, 0),
                LocalDateTime.of(2026, 10, 24, 11, 0),
                TrangThaiLichHen.DAT_LICH, "Dang hoat dong");
        service.datLichHen(lhActive);

        // LH004 dang o trang thai DA_HUY tu 08:00 den 18:00 cung voi NV002.
        // Thu cap nhat LH004 tro lai DAT_LICH -> Phai bi bao loi xung dot voi LH_ACT!
        boolean batLoiKichHoat = false;
        try {
            service.capNhatTrangThaiLichHen("LH004", TrangThaiLichHen.DAT_LICH);
        } catch (ValidationException e) {
            batLoiKichHoat = true;
        }
        assert batLoiKichHoat : "Phai chan kich hoat lai lich da huy neu xung dot voi mot lich khac dang active";
    }

    private static void testStrictFileParsingAndSecurityRejection() throws Exception {
        Path tempDir = Files.createTempDirectory("petcare_test_strict_file");

        // 1. Tep tin rong
        Path emptyFile = tempDir.resolve("empty.txt");
        Files.createFile(emptyFile);
        CuaHangService sEmpty = new CuaHangService(emptyFile);
        boolean batEmpty = false;
        try {
            sEmpty.napDuLieu();
        } catch (DuLieuException e) {
            batEmpty = true;
        }
        assert batEmpty : "Phai tu choi tep tin rong khong co header";

        // 2. Sai phien ban header
        Path wrongHeaderFile = tempDir.resolve("wrong_hdr.txt");
        Files.writeString(wrongHeaderFile, "# PETCARE_STORE_DATA_V99\n[KHACH_HANG]\n");
        CuaHangService sHdr = new CuaHangService(wrongHeaderFile);
        boolean batHdr = false;
        try {
            sHdr.napDuLieu();
        } catch (DuLieuException e) {
            batHdr = true;
        }
        assert batHdr : "Phai tu choi tep sai phien ban header";

        // 3. Section khong hop le
        Path unknownSecFile = tempDir.resolve("unknown_sec.txt");
        Files.writeString(unknownSecFile, "# PETCARE_STORE_DATA_V1\n[UNKNOWN_SECTION]\ndata1|data2\n");
        CuaHangService sSec = new CuaHangService(unknownSecFile);
        boolean batSec = false;
        try {
            sSec.napDuLieu();
        } catch (DuLieuException e) {
            batSec = true;
        }
        assert batSec : "Phai tu choi section khong xac dinh";

        // 4. Du cot (extra columns)
        Path extraColFile = tempDir.resolve("extra_col.txt");
        Files.writeString(extraColFile, "# PETCARE_STORE_DATA_V1\n[KHACH_HANG]\nKH001|An|0912345678|an@mail.com|DC|2026-01-01|COT_THUA\n");
        CuaHangService sCol = new CuaHangService(extraColFile);
        boolean batCol = false;
        try {
            sCol.napDuLieu();
        } catch (DuLieuException e) {
            batCol = true;
        }
        assert batCol : "Phai tu choi dong co cot thua";

        // 5. Gia tri boolean coSayKho khong hop le (vi du: 'maybe')
        Path badBoolFile = tempDir.resolve("bad_bool.txt");
        Files.writeString(badBoolFile, "# PETCARE_STORE_DATA_V1\n[DICH_VU]\nTAM|DV001|Tam|100000|Mo ta|MAYBE\n");
        CuaHangService sBool = new CuaHangService(badBoolFile);
        boolean batBool = false;
        try {
            sBool.napDuLieu();
        } catch (DuLieuException e) {
            batBool = true;
        }
        assert batBool : "Phai tu choi gia tri boolean khong dung true/false";

        // 6. Ky tu escape khong hop le (\z)
        Path badEscFile = tempDir.resolve("bad_esc.txt");
        Files.writeString(badEscFile, "# PETCARE_STORE_DATA_V1\n[KHACH_HANG]\nKH001|An\\z|0912345678|an@mail.com|DC|2026-01-01\n");
        CuaHangService sEsc = new CuaHangService(badEscFile);
        boolean batEsc = false;
        try {
            sEsc.napDuLieu();
        } catch (DuLieuException e) {
            batEsc = true;
        }
        assert batEsc : "Phai tu choi ky tu escape khong hop le";
    }

    private static void testStrictDateAndNonFiniteRejection() throws Exception {
        // 1. Strict Date: 2026-02-30 khong ton tai
        boolean batLoiNgay = false;
        try {
            LocalDate.parse("2026-02-30", DinhDangUtil.DINH_DANG_NGAY);
        } catch (DateTimeParseException e) {
            batLoiNgay = true;
        }
        assert batLoiNgay : "Strict date parser phai tu choi ngay 2026-02-30";

        // 2. Can nang khong huu han (Infinity hoac NaN)
        boolean batLoiCanNangInf = false;
        try {
            new Cho("TC_INF", "Gau", "KH001", 2, Double.POSITIVE_INFINITY, "Poodle");
        } catch (IllegalArgumentException e) {
            batLoiCanNangInf = true;
        }
        assert batLoiCanNangInf : "Phai tu choi can nang la so vo han (Infinity)";

        boolean batLoiCanNangAm = false;
        try {
            new Cho("TC_NEG", "Gau", "KH001", 2, -5.0, "Poodle");
        } catch (IllegalArgumentException e) {
            batLoiCanNangAm = true;
        }
        assert batLoiCanNangAm : "Phai tu choi can nang <= 0";
    }

    private static void testForcedSaveFailureRollback() throws Exception {
        Path tempDir = Files.createTempDirectory("petcare_test_tx_rollback");
        Path testFile = tempDir.resolve("data.txt");
        Files.copy(Path.of("data.txt"), testFile);

        CuaHangService service = new CuaHangService(testFile);
        service.napDuLieu();

        // Tao thu muc khong rong tai data.txt.tmp de buoc Files.newBufferedWriter(tempPath, ...) nem ra IOException
        Path tempPath = tempDir.resolve("data.txt.tmp");
        Files.createDirectories(tempPath);
        Files.writeString(tempPath.resolve("blocker.dat"), "CAN_TRO_GHI_FILE_TAM");

        // 1. Thu them khach hang khi ghi dia bi loi
        KhachHang khMoi = new KhachHang("KH_FAIL", "Khach Fail", "0912345678", "fail@mail.com", "Dia chi", LocalDate.now());
        boolean batLoiGhi = false;
        try {
            service.themKhachHang(khMoi);
        } catch (DuLieuException e) {
            batLoiGhi = true;
        }
        assert batLoiGhi : "Phai nem ra ngoai le DuLieuException khi khong the ghi tep tam";

        // Bo nho phai duoc rollback ve trang thai truoc khi them KH_FAIL
        assert CuaHangService.timTheoMa(service.getDsKhachHang(), "KH_FAIL") == null : "Khach hang khong duoc ton tai trong bo nho neu ghi dia that bai!";

        // 2. Thu thanh toan hoa don khi ghi dia bi loi
        HoaDon hdTruocLoi = CuaHangService.timTheoMa(service.getDsHoaDon(), "HD003");
        assert hdTruocLoi.isChuaThanhToan();
        SanPham sp1TruocLoi = CuaHangService.timTheoMa(service.getDsSanPham(), "SP001");
        int tonCu = sp1TruocLoi.getSoLuongTon();
        byte[] bytesTruocLoi = Files.readAllBytes(testFile);

        boolean batLoiPay = false;
        try {
            service.thanhToanHoaDon("HD003");
        } catch (DuLieuException e) {
            batLoiPay = true;
        }
        assert batLoiPay;

        // Kiem tra tep tin va trang thai service duoc lam moi (refreshed service state) khong bi thay doi
        byte[] bytesSauLoi = Files.readAllBytes(testFile);
        assert java.util.Arrays.equals(bytesTruocLoi, bytesSauLoi) : "Noi dung tep tin data.txt khong duoc phep thay doi khi ghi that bai!";

        HoaDon hdRefreshed = CuaHangService.timTheoMa(service.getDsHoaDon(), "HD003");
        assert hdRefreshed.isChuaThanhToan() : "Trang thai hoa don trong service phai rollback ve CHUA_THANH_TOAN!";

        SanPham sp1Refreshed = CuaHangService.timTheoMa(service.getDsSanPham(), "SP001");
        assert sp1Refreshed.getSoLuongTon() == tonCu : "Ton kho trong service phai rollback ve nguyen ven!";

        // 3. Thu sua thuc an khi ghi dia bi loi
        boolean batLoiSua = false;
        try {
            service.suaThucAn("SP001", "Hat Sua Fail", new BigDecimal("500000"), 99, LocalDate.now().plusYears(1), 2000);
        } catch (DuLieuException e) {
            batLoiSua = true;
        }
        assert batLoiSua;
        SanPham sp1SuaRefreshed = CuaHangService.timTheoMa(service.getDsSanPham(), "SP001");
        assert sp1SuaRefreshed.getDonGia().compareTo(new BigDecimal("120000")) == 0 : "Gia san pham phai giu nguyen 120.000!";

        // 4. Thu xoa khach hang khi ghi dia bi loi
        KhachHang khTestXoa = new KhachHang("KH_TMP", "Khach Tam", "0912345678", "tam@mail.com", "DC", LocalDate.now());
        // Tam bo chan de them vao
        Files.delete(tempPath.resolve("blocker.dat"));
        Files.delete(tempPath);
        service.themKhachHang(khTestXoa);
        assert CuaHangService.timTheoMa(service.getDsKhachHang(), "KH_TMP") != null;

        // Dat lai blocker de thu xoa that bai
        Files.createDirectories(tempPath);
        Files.writeString(tempPath.resolve("blocker.dat"), "CAN_TRO_GHI_FILE_TAM");
        boolean batLoiXoa = false;
        try {
            service.xoaKhachHang("KH_TMP");
        } catch (DuLieuException e) {
            batLoiXoa = true;
        }
        assert batLoiXoa;
        assert CuaHangService.timTheoMa(service.getDsKhachHang(), "KH_TMP") != null : "Khach hang khong bi mat khoi bo nho khi xoa that bai tren dia!";

        // Don dep thu muc chan
        Files.delete(tempPath.resolve("blocker.dat"));
        Files.delete(tempPath);

        // Thanh toan lai luc nay phai thanh cong!
        service.thanhToanHoaDon("HD003");
        HoaDon hdDaTra = CuaHangService.timTheoMa(service.getDsHoaDon(), "HD003");
        assert hdDaTra.getTrangThai() == TrangThaiHoaDon.DA_THANH_TOAN;
    }

    private static void testPaymentInsufficientStockAndDuplicateLinesAndRepeatPayment() throws Exception {
        Path tempDir = Files.createTempDirectory("petcare_test_payment");
        Path testFile = tempDir.resolve("test_pay.txt");
        Files.copy(Path.of("data.txt"), testFile);

        CuaHangService service = new CuaHangService(testFile);
        service.napDuLieu();

        SanPham sp4 = CuaHangService.timTheoMa(service.getDsSanPham(), "SP004");
        assert sp4 != null && sp4.getSoLuongTon() == 4;

        String maHD = "HD_TEST_PAY";
        HoaDon hd = new HoaDon(maHD, "KH001", "NV003", LocalDateTime.now(), null, null, BigDecimal.ZERO);
        service.taoHoaDonNhap(hd);

        service.themMucVaoHoaDon(maHD, "SP004", 3);
        service.themMucVaoHoaDon(maHD, "SP004", 2);

        boolean batDuocLoiTonKho = false;
        try {
            service.thanhToanHoaDon(maHD);
        } catch (ValidationException e) {
            batDuocLoiTonKho = true;
        }
        assert batDuocLoiTonKho;
        assert sp4.getSoLuongTon() == 4;

        service.suaSoLuongMucHoaDon(maHD, 1, 1);
        service.thanhToanHoaDon(maHD);

        SanPham sp4SauPay = CuaHangService.timTheoMa(service.getDsSanPham(), "SP004");
        assert sp4SauPay.getSoLuongTon() == 0;
        HoaDon hdSauPay = CuaHangService.timTheoMa(service.getDsHoaDon(), maHD);
        assert hdSauPay.getTrangThai() == TrangThaiHoaDon.DA_THANH_TOAN;
        assert hdSauPay.getNgayThanhToan() != null;

        boolean batDuocLoiTraLap = false;
        try {
            service.thanhToanHoaDon(maHD);
        } catch (ValidationException e) {
            batDuocLoiTraLap = true;
        }
        assert batDuocLoiTraLap;

        boolean batDuocLoiSua = false;
        try {
            service.themMucVaoHoaDon(maHD, "SP001", 1);
        } catch (ValidationException e) {
            batDuocLoiSua = true;
        }
        assert batDuocLoiSua;
    }

    private static void testAppointmentConflictsAndCrudConstraints() throws Exception {
        Path tempDir = Files.createTempDirectory("petcare_test_conflict");
        Path testFile = tempDir.resolve("test_app.txt");
        Files.copy(Path.of("data.txt"), testFile);

        CuaHangService service = new CuaHangService(testFile);
        service.napDuLieu();

        // 1. Trùng nhân viên NV001
        LichHen trungNhanVien = new LichHen("LH_NEW1", "KH002", "TC003", "DV001", "NV001",
                LocalDateTime.of(2026, 10, 26, 9, 30),
                LocalDateTime.of(2026, 10, 26, 11, 0),
                TrangThaiLichHen.DAT_LICH, "Test trung NV");
        boolean xungDotNV = false;
        try {
            service.datLichHen(trungNhanVien);
        } catch (ValidationException e) {
            xungDotNV = true;
        }
        assert xungDotNV;

        // 2. Trùng thú cưng TC001
        LichHen trungThuCung = new LichHen("LH_NEW2", "KH001", "TC001", "DV001", "NV002",
                LocalDateTime.of(2026, 10, 26, 10, 0),
                LocalDateTime.of(2026, 10, 26, 11, 30),
                TrangThaiLichHen.DAT_LICH, "Test trung TC");
        boolean xungDotTC = false;
        try {
            service.datLichHen(trungThuCung);
        } catch (ValidationException e) {
            xungDotTC = true;
        }
        assert xungDotTC;

        // 3. Sửa lịch hẹn chính nó
        service.suaLichHen("LH001", "DV001", "NV001",
                LocalDateTime.of(2026, 10, 26, 9, 0),
                LocalDateTime.of(2026, 10, 26, 10, 30),
                "Ghi chu cap nhat");

        // 4. Chặn xóa sản phẩm hoặc dịch vụ đã có trong snapshot hóa đơn
        boolean batLoiXoaSP = false;
        try {
            service.xoaSanPham("SP001");
        } catch (ValidationException e) {
            batLoiXoaSP = true;
        }
        assert batLoiXoaSP : "Phai chan xoa san pham SP001 da duoc ghi nhan trong hoa don HD001/HD003";

        boolean batLoiXoaDV = false;
        try {
            service.xoaDichVu("DV001");
        } catch (ValidationException e) {
            batLoiXoaDV = true;
        }
        assert batLoiXoaDV : "Phai chan xoa dich vu DV001 da duoc ghi nhan trong hoa don";
    }

    private static void testOwnerReviewProbeRegressionAndPaidInvoiceImmutability() throws Exception {
        // 1. Missing invoice item reference: corrupts HD001|SP001| to HD001|SP999|
        String sample = Files.readString(Path.of("data.txt"));
        Path tempDir = Files.createTempDirectory("petcare_test_probe_reg");
        Path p1 = tempDir.resolve("probe_missing_item.txt");
        Files.writeString(p1, sample.replace("HD001|SP001|", "HD001|SP999|"));
        byte[] bytesBefore1 = Files.readAllBytes(p1);
        boolean batMissingItem = false;
        try {
            new CuaHangService(p1).napDuLieu();
        } catch (ValidationException e) {
            batMissingItem = true;
            assert e.getMessage().contains("HD001") && e.getMessage().contains("SP999") : "Thong bao loi phai chua ma hoa don va ma san pham";
        }
        assert batMissingItem : "Phai tu choi nap hoa don co muc san pham khong ton tai";
        assert java.util.Arrays.equals(bytesBefore1, Files.readAllBytes(p1)) : "Tep goc khong duoc bi thay doi khi nap loi";

        // 2. Wrong invoice item kind: changes HD001|DV001|Tam ve sinh spa|DICH_VU| to SAN_PHAM|
        Path p2 = tempDir.resolve("probe_wrong_kind.txt");
        Files.writeString(p2, sample.replace("HD001|DV001|Tam ve sinh spa|DICH_VU|", "HD001|DV001|Tam ve sinh spa|SAN_PHAM|"));
        byte[] bytesBefore2 = Files.readAllBytes(p2);
        boolean batWrongKind = false;
        try {
            new CuaHangService(p2).napDuLieu();
        } catch (ValidationException e) {
            batWrongKind = true;
            assert e.getMessage().contains("HD001") && e.getMessage().contains("DV001") : "Thong bao loi phai chua ma hoa don va ma muc";
        }
        assert batWrongKind : "Phai tu choi nap hoa don co sai lech loai muc (kind mismatch)";
        assert java.util.Arrays.equals(bytesBefore2, Files.readAllBytes(p2)) : "Tep goc khong duoc bi thay doi khi nap loi";

        // 3. Paid invoice blocks themChiTietKhiNap
        Path p3 = tempDir.resolve("probe_paid_append.txt");
        Files.writeString(p3, sample);
        CuaHangService s = new CuaHangService(p3);
        s.napDuLieu();
        HoaDon hd = CuaHangService.timTheoMa(s.getDsHoaDon(), "HD001");
        assert hd != null && !hd.isChuaThanhToan();

        boolean batPaidAppend = false;
        try {
            hd.themChiTietKhiNap(new ChiTietHoaDon("SP001", "Test", LoaiMucHoaDon.SAN_PHAM, BigDecimal.ONE, 1, BigDecimal.ONE));
        } catch (IllegalStateException e) {
            batPaidAppend = true;
        }
        assert batPaidAppend : "themChiTietKhiNap tren hoa don da thanh toan phai nem ra IllegalStateException";

        // 4. Defensive copy check: mutations on returned collection do not affect HoaDon
        int countBefore = hd.getDanhSachChiTiet().size();
        DanhSach<ChiTietHoaDon> copy = hd.getDanhSachChiTiet();
        copy.clear();
        assert hd.getDanhSachChiTiet().size() == countBefore : "clear() tren ban sao defensive copy khong duoc anh huong den hoa don!";
        copy.add(new ChiTietHoaDon("SP001", "Fake", LoaiMucHoaDon.SAN_PHAM, BigDecimal.TEN, 1, BigDecimal.TEN));
        assert hd.getDanhSachChiTiet().size() == countBefore : "add() tren ban sao defensive copy khong duoc anh huong den hoa don!";

        // 5. capNhatTongTien on paid invoice must not change total
        BigDecimal oldTotal = hd.getTongTien();
        hd.capNhatTongTien();
        assert oldTotal.compareTo(hd.getTongTien()) == 0 : "capNhatTongTien tren hoa don da thanh toan khong duoc phep thay doi tong tien";

        // 6. Draft operations on paid invoice must throw IllegalStateException
        boolean batDraftAdd = false;
        try {
            hd.themChiTiet(new ChiTietHoaDon("SP001", "Test", LoaiMucHoaDon.SAN_PHAM, BigDecimal.ONE, 1, BigDecimal.ONE));
        } catch (IllegalStateException e) {
            batDraftAdd = true;
        }
        assert batDraftAdd;

        boolean batDraftRemove = false;
        try {
            hd.xoaChiTiet(0);
        } catch (IllegalStateException e) {
            batDraftRemove = true;
        }
        assert batDraftRemove;

        boolean batDraftEditQty = false;
        try {
            hd.capNhatSoLuongChiTiet(0, 99);
        } catch (IllegalStateException e) {
            batDraftEditQty = true;
        }
        assert batDraftEditQty;

        boolean batDraftSetKH = false;
        try {
            hd.setMaKhachHang("KH999");
        } catch (IllegalStateException e) {
            batDraftSetKH = true;
        }
        assert batDraftSetKH;

        boolean batDraftSetNV = false;
        try {
            hd.setMaNhanVien("NV999");
        } catch (IllegalStateException e) {
            batDraftSetNV = true;
        }
        assert batDraftSetNV;

        boolean batRepeatPay = false;
        try {
            hd.danhDauDaThanhToan(LocalDateTime.now());
        } catch (IllegalStateException e) {
            batRepeatPay = true;
        }
        assert batRepeatPay;
    }

    private static void testPreSaveStagingValidationAndNegativeFileIntegrity() throws Exception {
        Path tempDir = Files.createTempDirectory("petcare_test_presave_staging");

        // 1. Negative file test: duplicate entity ID
        Path dupIdFile = tempDir.resolve("dup_id.txt");
        String dupIdContent = "# PETCARE_STORE_DATA_V1\n\n"
                + "[KHACH_HANG]\n"
                + "KH001|An|0912345678|an@mail.com|DC|2026-01-01\n"
                + "KH001|Trung|0987654321|trung@mail.com|DC|2026-01-02\n";
        Files.writeString(dupIdFile, dupIdContent);
        boolean batDupId = false;
        try {
            new CuaHangService(dupIdFile).napDuLieu();
        } catch (ValidationException e) {
            batDupId = true;
            assert e.getMessage().contains("KH001") : "Loi phai thong bao ma trung lap KH001";
        }
        assert batDupId : "Phai tu choi tep tin chua ma trung lap";

        // 2. Negative file test: invalid customer ref in pet
        Path badPetRefFile = tempDir.resolve("bad_pet_ref.txt");
        String badPetRefContent = "# PETCARE_STORE_DATA_V1\n\n"
                + "[KHACH_HANG]\n"
                + "KH001|An|0912345678|an@mail.com|DC|2026-01-01\n\n"
                + "[THU_CUNG]\n"
                + "CHO|TC001|Gau|KH999|2|5.0|Poodle\n";
        Files.writeString(badPetRefFile, badPetRefContent);
        boolean batBadPetRef = false;
        try {
            new CuaHangService(badPetRefFile).napDuLieu();
        } catch (ValidationException e) {
            batBadPetRef = true;
            assert e.getMessage().contains("TC001") && e.getMessage().contains("KH999");
        }
        assert batBadPetRef : "Phai tu choi thu cung tham chieu chu so huu khong ton tai";

        // 3. taoHoaDonNhap: reject non-draft or present paid timestamp
        Path liveFile = tempDir.resolve("live_data.txt");
        Files.copy(Path.of("data.txt"), liveFile);
        CuaHangService service = new CuaHangService(liveFile);
        service.napDuLieu();

        // 3a. Thu tao hoa don nhap nhung dat status DA_THANH_TOAN
        HoaDon hdPaidNew = new HoaDon("HD_INJECT", "KH001", "NV003", LocalDateTime.now(), TrangThaiHoaDon.DA_THANH_TOAN, LocalDateTime.now(), BigDecimal.ZERO);
        boolean batInjectPaid = false;
        try {
            service.taoHoaDonNhap(hdPaidNew);
        } catch (ValidationException e) {
            batInjectPaid = true;
        }
        assert batInjectPaid : "taoHoaDonNhap phai tu choi hoa don o trang thai DA_THANH_TOAN";
        assert CuaHangService.timTheoMa(service.getDsHoaDon(), "HD_INJECT") == null;

        // 3b. Thu tao hoa don nhap co ngay thanh toan
        HoaDon hdDraftWithDate = new HoaDon("HD_INJECT2", "KH001", "NV003", LocalDateTime.now(), TrangThaiHoaDon.CHUA_THANH_TOAN, LocalDateTime.now(), BigDecimal.ZERO);
        boolean batInjectDate = false;
        try {
            service.taoHoaDonNhap(hdDraftWithDate);
        } catch (ValidationException e) {
            batInjectDate = true;
        }
        assert batInjectDate : "taoHoaDonNhap phai tu choi hoa don nhap co chua ngay thanh toan";
        assert CuaHangService.timTheoMa(service.getDsHoaDon(), "HD_INJECT2") == null;

        // 4. Validate full staging before save: mutating entity to invalid subtype
        byte[] bytesBeforeSave = Files.readAllBytes(liveFile);

        // 4a. Sua thuc an thanh khoi luong gram <= 0
        boolean batLoiGram = false;
        try {
            service.suaThucAn("SP001", "Hat Loi", new BigDecimal("100000"), 10, LocalDate.now().plusMonths(6), 0);
        } catch (ValidationException e) {
            batLoiGram = true;
        }
        assert batLoiGram : "Full staging validation truoc khi luu phai bat loi gram <= 0 cua thuc an";
        assert java.util.Arrays.equals(bytesBeforeSave, Files.readAllBytes(liveFile)) : "Tep tin khong duoc bi ghi khi validation truoc luu that bai";
        ThucAn sp1Rollback = (ThucAn) CuaHangService.timTheoMa(service.getDsSanPham(), "SP001");
        assert sp1Rollback.getKhoiLuongGram() == 1000 : "Bo nho phai duoc rollback ve 1000g ban dau";

        // 4b. Sua nhan vien cham soc thanh phu cap am
        boolean batLoiPhuCap = false;
        try {
            service.suaNhanVienChamSoc("NV001", "Groomer", "0911223344", "g@mail.com",
                    new BigDecimal("8000000"), 1.2, "Cat tia", new BigDecimal("-100000"));
        } catch (ValidationException e) {
            batLoiPhuCap = true;
        }
        assert batLoiPhuCap : "Full staging validation truoc khi luu phai bat phu cap am";
        assert java.util.Arrays.equals(bytesBeforeSave, Files.readAllBytes(liveFile));
        NhanVienChamSoc nv1Rollback = (NhanVienChamSoc) CuaHangService.timTheoMa(service.getDsNhanVien(), "NV001");
        assert nv1Rollback.getPhuCap().compareTo(new BigDecimal("1500000")) == 0 : "Bo nho phai duoc rollback ve phu cap hop le";

        // 4c. Sua ho ten thanh chuoi rong
        boolean batLoiTenTrong = false;
        try {
            service.suaKhachHang("KH001", "", "0912345678", "an@mail.com", "DC");
        } catch (ValidationException e) {
            batLoiTenTrong = true;
        }
        assert batLoiTenTrong : "Phai bat loi ten khach hang rong";
        assert java.util.Arrays.equals(bytesBeforeSave, Files.readAllBytes(liveFile));
        KhachHang kh1Rollback = CuaHangService.timTheoMa(service.getDsKhachHang(), "KH001");
        assert "Nguyen Van An".equals(kh1Rollback.getHoTen());

        // 5. Normal CRUD roundtrip still works cleanly
        String maKH = CuaHangService.sinhMaMoi(service.getDsKhachHang(), "KH");
        KhachHang khMoi = new KhachHang(maKH, "Khach Hop Le", "0918889999", "ok@mail.com", "DC", LocalDate.now());
        service.themKhachHang(khMoi);
        assert CuaHangService.timTheoMa(service.getDsKhachHang(), maKH) != null;

        // Reload to verify persisted cleanly
        CuaHangService reloadOk = new CuaHangService(liveFile);
        reloadOk.napDuLieu();
        assert CuaHangService.timTheoMa(reloadOk.getDsKhachHang(), maKH) != null;
    }
}
