package petcare.model;

import java.math.BigDecimal;

/**
 * Dich vu cat tia long, tao kieu cho thu cung.
 */
public class DichVuCatTia extends DichVu {
    private static final BigDecimal PHU_PHI_TAO_KIEU = new BigDecimal("50000");
    private String kieuCat;

    public DichVuCatTia(String ma, String ten, BigDecimal donGiaCoBan, String moTa, String kieuCat) {
        super(ma, ten, donGiaCoBan, moTa);
        if (kieuCat == null || kieuCat.trim().isEmpty()) {
            throw new IllegalArgumentException("Kieu cat khong duoc de trong.");
        }
        this.kieuCat = kieuCat.trim();
    }

    public String getKieuCat() {
        return kieuCat;
    }

    public void setKieuCat(String kieuCat) {
        if (kieuCat == null || kieuCat.trim().isEmpty()) {
            throw new IllegalArgumentException("Kieu cat khong duoc de trong.");
        }
        this.kieuCat = kieuCat.trim();
    }

    @Override
    public String getDonViTinh() {
        return "Lan";
    }

    @Override
    public BigDecimal tinhGia(int soLuongHoacSoNgay) {
        if (soLuongHoacSoNgay < 0) {
            throw new IllegalArgumentException("So luong khong duoc am.");
        }
        BigDecimal giaMoiLan = getDonGia();
        if (kieuCat != null && kieuCat.toLowerCase().contains("tao kieu")) {
            giaMoiLan = giaMoiLan.add(PHU_PHI_TAO_KIEU);
        }
        return giaMoiLan.multiply(BigDecimal.valueOf(soLuongHoacSoNgay));
    }

    @Override
    public String toString() {
        return super.toString() + String.format(" | Kieu cat: %s", kieuCat);
    }
}
