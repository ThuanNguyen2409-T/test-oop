package petcare.persistence;

import petcare.collection.DanhSach;
import petcare.model.*;
import petcare.util.DinhDangUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Xu ly doc/ghi du lieu toan bo cua hang tu tep van ban UTF-8 voi kiem tra nghiem ngat
 * ve phien ban, so cot, gia tri boolean va co che ghi thay the an toan.
 */
public class FileStorage {
    public static final String FILE_HEADER = "# PETCARE_STORE_DATA_V1";
    private static final Set<String> CAC_SECTION_HOP_LE = new HashSet<>(Arrays.asList(
            "KHACH_HANG", "NHAN_VIEN", "THU_CUNG", "DICH_VU", "SAN_PHAM", "LICH_HEN", "HOA_DON", "CHI_TIET_HOA_DON"
    ));

    public static class StagingData {
        public DanhSach<KhachHang> khachHangs = new DanhSach<>();
        public DanhSach<NhanVien> nhanViens = new DanhSach<>();
        public DanhSach<ThuCung> thuCungs = new DanhSach<>();
        public DanhSach<DichVu> dichVus = new DanhSach<>();
        public DanhSach<SanPham> sanPhams = new DanhSach<>();
        public DanhSach<LichHen> lichHens = new DanhSach<>();
        public DanhSach<HoaDon> hoaDons = new DanhSach<>();
    }

    private static class RawHoaDon {
        String ma;
        String maKH;
        String maNV;
        LocalDateTime ngayTao;
        TrangThaiHoaDon trangThai;
        LocalDateTime ngayTT;
        BigDecimal tongTien;

        RawHoaDon(String ma, String maKH, String maNV, LocalDateTime ngayTao,
                  TrangThaiHoaDon trangThai, LocalDateTime ngayTT, BigDecimal tongTien) {
            this.ma = ma;
            this.maKH = maKH;
            this.maNV = maNV;
            this.ngayTao = ngayTao;
            this.trangThai = trangThai;
            this.ngayTT = ngayTT;
            this.tongTien = tongTien;
        }
    }

    public static String escapeField(String text) {
        if (text == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\\') {
                sb.append("\\\\");
            } else if (c == '|') {
                sb.append("\\|");
            } else if (c == '\n') {
                sb.append("\\n");
            } else if (c == '\r') {
                sb.append("\\r");
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static List<String> splitEscapedPipes(String line, int lineNumber) throws DuLieuException {
        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int i = 0;
        int len = line.length();
        while (i < len) {
            char c = line.charAt(i);
            if (c == '\\') {
                if (i + 1 >= len) {
                    throw new DuLieuException("Dong " + lineNumber + ": Ky tu escape '\\' o cuoi dong khong hop le.");
                }
                char next = line.charAt(i + 1);
                if (next == '|') {
                    current.append('|');
                    i += 2;
                } else if (next == '\\') {
                    current.append('\\');
                    i += 2;
                } else if (next == 'n') {
                    current.append('\n');
                    i += 2;
                } else if (next == 'r') {
                    current.append('\r');
                    i += 2;
                } else {
                    throw new DuLieuException("Dong " + lineNumber + ": Ky tu escape '\\" + next + "' khong hop le.");
                }
            } else if (c == '|') {
                tokens.add(current.toString());
                current.setLength(0);
                i++;
            } else {
                current.append(c);
                i++;
            }
        }
        tokens.add(current.toString());
        return tokens;
    }

    public StagingData docTuFile(Path path) throws DuLieuException {
        if (!Files.exists(path)) {
            throw new DuLieuException("Tep tin du lieu khong ton tai: " + path);
        }

        StagingData staging = new StagingData();
        List<RawHoaDon> rawInvoices = new ArrayList<>();
        Map<String, DanhSach<ChiTietHoaDon>> rawInvoiceLines = new LinkedHashMap<>();

        String currentSection = null;
        int lineNumber = 0;
        boolean daXacThucHeader = false;

        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String trimmed = line.trim();
                if (trimmed.isEmpty()) {
                    continue;
                }

                if (!daXacThucHeader) {
                    if (FILE_HEADER.equals(trimmed)) {
                        daXacThucHeader = true;
                        continue;
                    } else {
                        throw new DuLieuException("Dong " + lineNumber + ": Header tep khong hop le hoac khong dung phien ban. Yeu cau: '" + FILE_HEADER + "', nhan: '" + trimmed + "'");
                    }
                }

                if (trimmed.startsWith("#")) {
                    continue;
                }

                if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                    String sec = trimmed.substring(1, trimmed.length() - 1).toUpperCase();
                    if (!CAC_SECTION_HOP_LE.contains(sec)) {
                        throw new DuLieuException("Dong " + lineNumber + ": Section khong hop le: [" + sec + "]");
                    }
                    currentSection = sec;
                    continue;
                }

                if (currentSection == null) {
                    throw new DuLieuException("Dong " + lineNumber + ": Phat hien dong du lieu truoc khi co section.");
                }

                List<String> parts = splitEscapedPipes(line, lineNumber);
                xuLyDong(currentSection, parts, staging, rawInvoices, rawInvoiceLines, lineNumber);
            }
        } catch (IOException e) {
            throw new DuLieuException("Loi I/O doc tep tin tai dong " + lineNumber + ": " + e.getMessage(), e);
        }

        if (!daXacThucHeader) {
            throw new DuLieuException("Tep tin du lieu rong, thieu tieu de bat buoc: " + FILE_HEADER);
        }

        // Xay dung toan bo HoaDon day du chi tiet ngay trong ham khoi tao de bao dam tinh bat bien
        for (RawHoaDon raw : rawInvoices) {
            DanhSach<ChiTietHoaDon> lines = rawInvoiceLines.getOrDefault(raw.ma.toUpperCase(), new DanhSach<>());
            staging.hoaDons.add(new HoaDon(raw.ma, raw.maKH, raw.maNV, raw.ngayTao, raw.trangThai, raw.ngayTT, raw.tongTien, lines));
        }

        return staging;
    }

    private void xuLyDong(String section, List<String> parts, StagingData staging,
                          List<RawHoaDon> rawInvoices,
                          Map<String, DanhSach<ChiTietHoaDon>> rawInvoiceLines,
                          int lineNumber) throws DuLieuException {
        try {
            switch (section) {
                case "KHACH_HANG" -> {
                    if (parts.size() != 6) {
                        throw new DuLieuException("So luong cot khong dung (can dung 6 cot, nhan " + parts.size() + ")");
                    }
                    String ma = parts.get(0).trim();
                    String hoTen = parts.get(1).trim();
                    String sdt = parts.get(2).trim();
                    String email = parts.get(3).trim();
                    String diaChi = parts.get(4).trim();
                    LocalDate ngayDangKy = LocalDate.parse(parts.get(5).trim(), DinhDangUtil.DINH_DANG_NGAY);
                    staging.khachHangs.add(new KhachHang(ma, hoTen, sdt, email, diaChi, ngayDangKy));
                }
                case "NHAN_VIEN" -> {
                    if (parts.size() != 9) {
                        throw new DuLieuException("So luong cot nhan vien khong dung (can dung 9 cot, nhan " + parts.size() + ")");
                    }
                    String loai = parts.get(0).trim().toUpperCase();
                    String ma = parts.get(1).trim();
                    String hoTen = parts.get(2).trim();
                    String sdt = parts.get(3).trim();
                    String email = parts.get(4).trim();
                    BigDecimal luongCoBan = new BigDecimal(parts.get(5).trim());
                    double heSoLuong = Double.parseDouble(parts.get(6).trim());

                    if ("CHAM_SOC".equals(loai)) {
                        String chuyenMon = parts.get(7).trim();
                        BigDecimal phuCap = new BigDecimal(parts.get(8).trim());
                        staging.nhanViens.add(new NhanVienChamSoc(ma, hoTen, sdt, email, luongCoBan, heSoLuong, chuyenMon, phuCap));
                    } else if ("THU_NGAN".equals(loai)) {
                        String caLamViec = parts.get(7).trim();
                        BigDecimal hoaHong = new BigDecimal(parts.get(8).trim());
                        staging.nhanViens.add(new ThuNgan(ma, hoTen, sdt, email, luongCoBan, heSoLuong, caLamViec, hoaHong));
                    } else if ("QUAN_LY".equals(loai)) {
                        BigDecimal phuCapChucVu = new BigDecimal(parts.get(7).trim());
                        staging.nhanViens.add(new NhanVienQuanLy(ma, hoTen, sdt, email, luongCoBan, heSoLuong, phuCapChucVu));
                    } else {
                        throw new DuLieuException("Loai nhan vien khong hop le: " + loai);
                    }
                }
                case "THU_CUNG" -> {
                    if (parts.size() != 7) {
                        throw new DuLieuException("So luong cot thu cung khong dung (can dung 7 cot, nhan " + parts.size() + ")");
                    }
                    String loai = parts.get(0).trim().toUpperCase();
                    String ma = parts.get(1).trim();
                    String ten = parts.get(2).trim();
                    String maKhachHang = parts.get(3).trim();
                    int tuoi = Integer.parseInt(parts.get(4).trim());
                    double canNang = Double.parseDouble(parts.get(5).trim());
                    String extra = parts.get(6).trim();

                    if ("CHO".equals(loai)) {
                        staging.thuCungs.add(new Cho(ma, ten, maKhachHang, tuoi, canNang, extra));
                    } else if ("MEO".equals(loai)) {
                        staging.thuCungs.add(new Meo(ma, ten, maKhachHang, tuoi, canNang, extra));
                    } else {
                        throw new DuLieuException("Loai thu cung khong hop le: " + loai);
                    }
                }
                case "DICH_VU" -> {
                    if (parts.size() != 6) {
                        throw new DuLieuException("So luong cot dich vu khong dung (can dung 6 cot, nhan " + parts.size() + ")");
                    }
                    String loai = parts.get(0).trim().toUpperCase();
                    String ma = parts.get(1).trim();
                    String ten = parts.get(2).trim();
                    BigDecimal donGia = new BigDecimal(parts.get(3).trim());
                    String moTa = parts.get(4).trim();
                    String extra = parts.get(5).trim();

                    if ("TAM".equals(loai)) {
                        if (!"true".equalsIgnoreCase(extra) && !"false".equalsIgnoreCase(extra)) {
                            throw new DuLieuException("Gia tri boolean coSayKho khong hop le: '" + extra + "' (chi chap nhan true/false)");
                        }
                        boolean coSayKho = Boolean.parseBoolean(extra);
                        staging.dichVus.add(new DichVuTam(ma, ten, donGia, moTa, coSayKho));
                    } else if ("CAT_TIA".equals(loai)) {
                        staging.dichVus.add(new DichVuCatTia(ma, ten, donGia, moTa, extra));
                    } else if ("LUU_TRU".equals(loai)) {
                        staging.dichVus.add(new DichVuLuuTru(ma, ten, donGia, moTa, extra));
                    } else {
                        throw new DuLieuException("Loai dich vu khong hop le: " + loai);
                    }
                }
                case "SAN_PHAM" -> {
                    if (parts.size() != 7) {
                        throw new DuLieuException("So luong cot san pham khong dung (can dung 7 cot, nhan " + parts.size() + ")");
                    }
                    String loai = parts.get(0).trim().toUpperCase();
                    String ma = parts.get(1).trim();
                    String ten = parts.get(2).trim();
                    BigDecimal donGia = new BigDecimal(parts.get(3).trim());
                    int tonKho = Integer.parseInt(parts.get(4).trim());
                    String extra1 = parts.get(5).trim();
                    String extra2 = parts.get(6).trim();

                    if ("THUC_AN".equals(loai)) {
                        LocalDate hsd = LocalDate.parse(extra1, DinhDangUtil.DINH_DANG_NGAY);
                        int gram = Integer.parseInt(extra2);
                        staging.sanPhams.add(new ThucAn(ma, ten, donGia, tonKho, hsd, gram));
                    } else if ("PHU_KIEN".equals(loai)) {
                        staging.sanPhams.add(new PhuKien(ma, ten, donGia, tonKho, extra1, extra2));
                    } else {
                        throw new DuLieuException("Loai san pham khong hop le: " + loai);
                    }
                }
                case "LICH_HEN" -> {
                    if (parts.size() != 9) {
                        throw new DuLieuException("So luong cot lich hen khong dung (can dung 9 cot, nhan " + parts.size() + ")");
                    }
                    String ma = parts.get(0).trim();
                    String maKH = parts.get(1).trim();
                    String maTC = parts.get(2).trim();
                    String maDV = parts.get(3).trim();
                    String maNV = parts.get(4).trim();
                    LocalDateTime batDau = LocalDateTime.parse(parts.get(5).trim(), DinhDangUtil.DINH_DANG_NGAY_GIO);
                    LocalDateTime ketThuc = LocalDateTime.parse(parts.get(6).trim(), DinhDangUtil.DINH_DANG_NGAY_GIO);
                    TrangThaiLichHen trangThai = TrangThaiLichHen.valueOf(parts.get(7).trim().toUpperCase());
                    String ghiChu = parts.get(8).trim();

                    staging.lichHens.add(new LichHen(ma, maKH, maTC, maDV, maNV, batDau, ketThuc, trangThai, ghiChu));
                }
                case "HOA_DON" -> {
                    if (parts.size() != 7) {
                        throw new DuLieuException("So luong cot hoa don khong dung (can dung 7 cot, nhan " + parts.size() + ")");
                    }
                    String ma = parts.get(0).trim();
                    String maKH = parts.get(1).trim();
                    String maNV = parts.get(2).trim();
                    LocalDateTime ngayTao = LocalDateTime.parse(parts.get(3).trim(), DinhDangUtil.DINH_DANG_NGAY_GIO);
                    TrangThaiHoaDon trangThai = TrangThaiHoaDon.valueOf(parts.get(4).trim().toUpperCase());
                    String ngayTTStr = parts.get(5).trim();
                    LocalDateTime ngayTT = (ngayTTStr.isEmpty() || "null".equalsIgnoreCase(ngayTTStr))
                            ? null
                            : LocalDateTime.parse(ngayTTStr, DinhDangUtil.DINH_DANG_NGAY_GIO);
                    BigDecimal tongTien = new BigDecimal(parts.get(6).trim());

                    rawInvoices.add(new RawHoaDon(ma, maKH, maNV, ngayTao, trangThai, ngayTT, tongTien));
                }
                case "CHI_TIET_HOA_DON" -> {
                    if (parts.size() != 7) {
                        throw new DuLieuException("So luong cot chi tiet hoa don khong dung (can dung 7 cot, nhan " + parts.size() + ")");
                    }
                    String maHD = parts.get(0).trim();
                    String maMuc = parts.get(1).trim();
                    String tenMuc = parts.get(2).trim();
                    LoaiMucHoaDon loaiMuc = LoaiMucHoaDon.valueOf(parts.get(3).trim().toUpperCase());
                    BigDecimal donGia = new BigDecimal(parts.get(4).trim());
                    int soLuong = Integer.parseInt(parts.get(5).trim());
                    BigDecimal thanhTien = new BigDecimal(parts.get(6).trim());

                    boolean tonTaiHD = false;
                    for (RawHoaDon raw : rawInvoices) {
                        if (raw.ma.equalsIgnoreCase(maHD)) {
                            tonTaiHD = true;
                            break;
                        }
                    }
                    if (!tonTaiHD) {
                        throw new DuLieuException("Chi tiet tham chieu toi ma hoa don khong ton tai: " + maHD);
                    }

                    rawInvoiceLines.computeIfAbsent(maHD.toUpperCase(), k -> new DanhSach<>())
                            .add(new ChiTietHoaDon(maMuc, tenMuc, loaiMuc, donGia, soLuong, thanhTien));
                }
                default -> throw new DuLieuException("Section khong duoc ho tro: " + section);
            }
        } catch (DuLieuException e) {
            throw new DuLieuException("Dong " + lineNumber + " [" + section + "]: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new DuLieuException("Dong " + lineNumber + " [" + section + "]: Cu phap khong hop le - " + e.getMessage(), e);
        }
    }

    public void ghiRaFile(Path targetPath, StagingData data) throws DuLieuException {
        Path parent = targetPath.getParent();
        if (parent != null && !Files.exists(parent)) {
            try {
                Files.createDirectories(parent);
            } catch (IOException e) {
                throw new DuLieuException("Khong the tao thu muc cha: " + parent, e);
            }
        }

        Path tempPath = targetPath.resolveSibling(targetPath.getFileName().toString() + ".tmp");

        try (BufferedWriter writer = Files.newBufferedWriter(tempPath, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {

            writer.write(FILE_HEADER);
            writer.newLine();

            // Khach hang
            writer.newLine();
            writer.write("[KHACH_HANG]");
            writer.newLine();
            for (KhachHang kh : data.khachHangs) {
                writer.write(String.join("|",
                        escapeField(kh.getMa()),
                        escapeField(kh.getHoTen()),
                        escapeField(kh.getSoDienThoai()),
                        escapeField(kh.getEmail()),
                        escapeField(kh.getDiaChi()),
                        kh.getNgayDangKy().format(DinhDangUtil.DINH_DANG_NGAY)
                ));
                writer.newLine();
            }

            // Nhan vien
            writer.newLine();
            writer.write("[NHAN_VIEN]");
            writer.newLine();
            for (NhanVien nv : data.nhanViens) {
                if (nv instanceof NhanVienChamSoc cs) {
                    writer.write(String.join("|",
                            "CHAM_SOC",
                            escapeField(cs.getMa()),
                            escapeField(cs.getHoTen()),
                            escapeField(cs.getSoDienThoai()),
                            escapeField(cs.getEmail()),
                            cs.getLuongCoBan().toPlainString(),
                            String.valueOf(cs.getHeSoLuong()),
                            escapeField(cs.getChuyenMon()),
                            cs.getPhuCap().toPlainString()
                    ));
                } else if (nv instanceof ThuNgan tn) {
                    writer.write(String.join("|",
                            "THU_NGAN",
                            escapeField(tn.getMa()),
                            escapeField(tn.getHoTen()),
                            escapeField(tn.getSoDienThoai()),
                            escapeField(tn.getEmail()),
                            tn.getLuongCoBan().toPlainString(),
                            String.valueOf(tn.getHeSoLuong()),
                            escapeField(tn.getCaLamViec()),
                            tn.getHoaHong().toPlainString()
                    ));
                } else if (nv instanceof NhanVienQuanLy ql) {
                    writer.write(String.join("|",
                            "QUAN_LY",
                            escapeField(ql.getMa()),
                            escapeField(ql.getHoTen()),
                            escapeField(ql.getSoDienThoai()),
                            escapeField(ql.getEmail()),
                            ql.getLuongCoBan().toPlainString(),
                            String.valueOf(ql.getHeSoLuong()),
                            ql.getPhuCapChucVu().toPlainString(),
                            ""
                    ));
                }
                writer.newLine();
            }

            // Thu cung
            writer.newLine();
            writer.write("[THU_CUNG]");
            writer.newLine();
            for (ThuCung tc : data.thuCungs) {
                if (tc instanceof Cho cho) {
                    writer.write(String.join("|",
                            "CHO",
                            escapeField(cho.getMa()),
                            escapeField(cho.getTen()),
                            escapeField(cho.getMaKhachHang()),
                            String.valueOf(cho.getTuoi()),
                            String.valueOf(cho.getCanNang()),
                            escapeField(cho.getGiongCho())
                    ));
                } else if (tc instanceof Meo meo) {
                    writer.write(String.join("|",
                            "MEO",
                            escapeField(meo.getMa()),
                            escapeField(meo.getTen()),
                            escapeField(meo.getMaKhachHang()),
                            String.valueOf(meo.getTuoi()),
                            String.valueOf(meo.getCanNang()),
                            escapeField(meo.getMauLong())
                    ));
                }
                writer.newLine();
            }

            // Dich vu
            writer.newLine();
            writer.write("[DICH_VU]");
            writer.newLine();
            for (DichVu dv : data.dichVus) {
                if (dv instanceof DichVuTam tam) {
                    writer.write(String.join("|",
                            "TAM",
                            escapeField(tam.getMa()),
                            escapeField(tam.getTen()),
                            tam.getDonGia().toPlainString(),
                            escapeField(tam.getMoTa()),
                            String.valueOf(tam.isCoSayKho())
                    ));
                } else if (dv instanceof DichVuCatTia catTia) {
                    writer.write(String.join("|",
                            "CAT_TIA",
                            escapeField(catTia.getMa()),
                            escapeField(catTia.getTen()),
                            catTia.getDonGia().toPlainString(),
                            escapeField(catTia.getMoTa()),
                            escapeField(catTia.getKieuCat())
                    ));
                } else if (dv instanceof DichVuLuuTru luuTru) {
                    writer.write(String.join("|",
                            "LUU_TRU",
                            escapeField(luuTru.getMa()),
                            escapeField(luuTru.getTen()),
                            luuTru.getDonGia().toPlainString(),
                            escapeField(luuTru.getMoTa()),
                            escapeField(luuTru.getLoaiPhong())
                    ));
                }
                writer.newLine();
            }

            // San pham
            writer.newLine();
            writer.write("[SAN_PHAM]");
            writer.newLine();
            for (SanPham sp : data.sanPhams) {
                if (sp instanceof ThucAn ta) {
                    writer.write(String.join("|",
                            "THUC_AN",
                            escapeField(ta.getMa()),
                            escapeField(ta.getTen()),
                            ta.getDonGia().toPlainString(),
                            String.valueOf(ta.getSoLuongTon()),
                            ta.getHanSuDung().format(DinhDangUtil.DINH_DANG_NGAY),
                            String.valueOf(ta.getKhoiLuongGram())
                    ));
                } else if (sp instanceof PhuKien pk) {
                    writer.write(String.join("|",
                            "PHU_KIEN",
                            escapeField(pk.getMa()),
                            escapeField(pk.getTen()),
                            pk.getDonGia().toPlainString(),
                            String.valueOf(pk.getSoLuongTon()),
                            escapeField(pk.getChatLieu()),
                            escapeField(pk.getKichCo())
                    ));
                }
                writer.newLine();
            }

            // Lich hen
            writer.newLine();
            writer.write("[LICH_HEN]");
            writer.newLine();
            for (LichHen lh : data.lichHens) {
                writer.write(String.join("|",
                        escapeField(lh.getMa()),
                        escapeField(lh.getMaKhachHang()),
                        escapeField(lh.getMaThuCung()),
                        escapeField(lh.getMaDichVu()),
                        escapeField(lh.getMaNhanVien()),
                        lh.getThoiGianBatDau().format(DinhDangUtil.DINH_DANG_NGAY_GIO),
                        lh.getThoiGianKetThuc().format(DinhDangUtil.DINH_DANG_NGAY_GIO),
                        lh.getTrangThai().name(),
                        escapeField(lh.getGhiChu())
                ));
                writer.newLine();
            }

            // Hoa don
            writer.newLine();
            writer.write("[HOA_DON]");
            writer.newLine();
            for (HoaDon hd : data.hoaDons) {
                writer.write(String.join("|",
                        escapeField(hd.getMa()),
                        escapeField(hd.getMaKhachHang()),
                        escapeField(hd.getMaNhanVien()),
                        hd.getNgayTao().format(DinhDangUtil.DINH_DANG_NGAY_GIO),
                        hd.getTrangThai().name(),
                        hd.getNgayThanhToan() != null ? hd.getNgayThanhToan().format(DinhDangUtil.DINH_DANG_NGAY_GIO) : "",
                        hd.getTongTien().toPlainString()
                ));
                writer.newLine();
            }

            // Chi tiet hoa don
            writer.newLine();
            writer.write("[CHI_TIET_HOA_DON]");
            writer.newLine();
            for (HoaDon hd : data.hoaDons) {
                for (ChiTietHoaDon ct : hd.getDanhSachChiTiet()) {
                    writer.write(String.join("|",
                            escapeField(hd.getMa()),
                            escapeField(ct.getMaMuc()),
                            escapeField(ct.getTenMuc()),
                            ct.getLoaiMuc().name(),
                            ct.getDonGia().toPlainString(),
                            String.valueOf(ct.getSoLuong()),
                            ct.getThanhTien().toPlainString()
                    ));
                    writer.newLine();
                }
            }

            writer.flush();
        } catch (IOException e) {
            try {
                Files.deleteIfExists(tempPath);
            } catch (IOException ignored) {
            }
            throw new DuLieuException("Khong the ghi du lieu ra tep tam: " + e.getMessage(), e);
        }

        try {
            try {
                Files.move(tempPath, targetPath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(tempPath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            try {
                Files.deleteIfExists(tempPath);
            } catch (IOException ignored) {
            }
            throw new DuLieuException("Loi khi thay the tep tin goc tu tep tam: " + e.getMessage(), e);
        }
    }
}
