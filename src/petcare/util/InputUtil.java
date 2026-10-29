package petcare.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Tien ich nhap lieu an toan tu ban phim console, bat loi EOF va xac thuc gia tri so hoc.
 */
public final class InputUtil {
    private InputUtil() {
    }

    private static String docDong(Scanner scanner, String thongBao) {
        System.out.print(thongBao);
        if (!scanner.hasNextLine()) {
            throw new KetThucNhapException("Da nhan tin hieu ket thuc luong nhap (EOF).");
        }
        try {
            return scanner.nextLine();
        } catch (NoSuchElementException e) {
            throw new KetThucNhapException("Da nhan tin hieu ket thuc luong nhap (EOF).", e);
        }
    }

    public static String nhapChuoi(Scanner scanner, String thongBao) {
        while (true) {
            String line = docDong(scanner, thongBao);
            if (line != null && !line.trim().isEmpty()) {
                return line.trim();
            }
            System.out.println("  [Loi] Noi dung khong duoc de trong. Vui long nhap lai.");
        }
    }

    public static String nhapChuoiTuyChon(Scanner scanner, String thongBao, String giaTriMacDinh) {
        String line = docDong(scanner, thongBao);
        if (line == null || line.trim().isEmpty()) {
            return giaTriMacDinh;
        }
        return line.trim();
    }

    public static int nhapSoNguyen(Scanner scanner, String thongBao, int toiThieu, int toiDa) {
        while (true) {
            String line = docDong(scanner, thongBao);
            try {
                int val = Integer.parseInt(line.trim());
                if (val >= toiThieu && val <= toiDa) {
                    return val;
                }
                System.out.printf("  [Loi] Gia tri phai tu %d den %d.%n", toiThieu, toiDa);
            } catch (NumberFormatException e) {
                System.out.println("  [Loi] Vui long nhap so nguyen hop le.");
            }
        }
    }

    public static double nhapSoThuc(Scanner scanner, String thongBao, double toiThieu) {
        while (true) {
            String line = docDong(scanner, thongBao);
            try {
                double val = Double.parseDouble(line.trim());
                if (!Double.isFinite(val)) {
                    System.out.println("  [Loi] Gia tri phai la so thuc huu han (khong chap nhan Infinity hoac NaN).");
                    continue;
                }
                if (val >= toiThieu) {
                    return val;
                }
                System.out.printf("  [Loi] Gia tri phai >= %.2f.%n", toiThieu);
            } catch (NumberFormatException e) {
                System.out.println("  [Loi] Vui long nhap so thuc hop le.");
            }
        }
    }

    public static BigDecimal nhapSoTien(Scanner scanner, String thongBao, BigDecimal toiThieu) {
        while (true) {
            String line = docDong(scanner, thongBao);
            try {
                BigDecimal val = new BigDecimal(line.trim());
                if (val.compareTo(toiThieu) >= 0) {
                    return val;
                }
                System.out.printf("  [Loi] So tien phai >= %s.%n", toiThieu);
            } catch (Exception e) {
                System.out.println("  [Loi] Vui long nhap so tien hop le (VD: 50000).");
            }
        }
    }

    public static LocalDate nhapNgay(Scanner scanner, String thongBao) {
        while (true) {
            String line = docDong(scanner, thongBao);
            try {
                return LocalDate.parse(line.trim(), DinhDangUtil.DINH_DANG_NGAY);
            } catch (DateTimeParseException e) {
                System.out.println("  [Loi] Dinh dang ngay khong hop le theo chuan uuuu-MM-dd nghiem ngat (VD: 2026-10-25).");
            }
        }
    }

    public static LocalDateTime nhapNgayGio(Scanner scanner, String thongBao) {
        while (true) {
            String line = docDong(scanner, thongBao);
            try {
                return LocalDateTime.parse(line.trim(), DinhDangUtil.DINH_DANG_NGAY_GIO);
            } catch (DateTimeParseException e) {
                System.out.println("  [Loi] Dinh dang ngay gio khong hop le theo chuan uuuu-MM-dd HH:mm nghiem ngat (VD: 2026-10-25 09:30).");
            }
        }
    }

    public static boolean nhapCoKhong(Scanner scanner, String thongBao) {
        while (true) {
            String line = docDong(scanner, thongBao + " (y/n): ");
            if (line != null) {
                String trimmed = line.trim().toLowerCase();
                if (trimmed.equals("y") || trimmed.equals("co") || trimmed.equals("yes") || trimmed.equals("1")) {
                    return true;
                }
                if (trimmed.equals("n") || trimmed.equals("khong") || trimmed.equals("no") || trimmed.equals("0")) {
                    return false;
                }
            }
            System.out.println("  [Loi] Vui long chi chon y (Co) hoac n (Khong).");
        }
    }
}
