package petcare.interfaces;

import java.math.BigDecimal;

/**
 * Interface cho cac doi tuong co the tinh tien trong hoa don (san pham, dich vu).
 */
public interface TinhTien {
    String getMa();
    String getTen();
    BigDecimal getDonGia();
    BigDecimal tinhThanhTien(int soLuong);
}
