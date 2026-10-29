package petcare.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;

/**
 * Cac tien ich dinh dang hien thi tien te va thoi gian voi che do STRICT nghiem ngat.
 */
public final class DinhDangUtil {
    public static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("uuuu-MM-dd")
            .withResolverStyle(ResolverStyle.STRICT);
    public static final DateTimeFormatter DINH_DANG_NGAY_GIO = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm")
            .withResolverStyle(ResolverStyle.STRICT);
    private static final DecimalFormat DINH_DANG_TIEN = new DecimalFormat("#,##0 VND");

    private DinhDangUtil() {
    }

    public static String dinhDangTien(BigDecimal tien) {
        if (tien == null) {
            return "0 VND";
        }
        return DINH_DANG_TIEN.format(tien);
    }

    public static String dinhDangNgay(LocalDate ngay) {
        if (ngay == null) {
            return "";
        }
        return ngay.format(DINH_DANG_NGAY);
    }

    public static String dinhDangNgayGio(LocalDateTime thoiGian) {
        if (thoiGian == null) {
            return "";
        }
        return thoiGian.format(DINH_DANG_NGAY_GIO);
    }
}
