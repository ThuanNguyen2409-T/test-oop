package petcare.model;

import java.math.BigDecimal;

/**
 * Dich vu tam ve sinh, spa thu cung.
 */
public class DichVuTam extends DichVu {
    private static final BigDecimal PHU_PHI_SAY_KHO = new BigDecimal("20000");
    private boolean coSayKho;

    public DichVuTam(String ma, String ten, BigDecimal donGiaCoBan, String moTa, boolean coSayKho) {
        super(ma, ten, donGiaCoBan, moTa);
        this.coSayKho = coSayKho;
    }

    public boolean isCoSayKho() {
        return coSayKho;
    }

    public void setCoSayKho(boolean coSayKho) {
        this.coSayKho = coSayKho;
    }

    @Override
    public String getDonViTinh() {
        return "Lan";
    }

    @Override
    public BigDecimal tinhGia(int soLuongHoacSoNgay) {
        BigDecimal giaMoiLan = getDonGia();
        if (coSayKho) {
            giaMoiLan = giaMoiLan.add(PHU_PHI_SAY_KHO);
        }
        return giaMoiLan.multiply(BigDecimal.valueOf(soLuongHoacSoNgay));
    }

    @Override
    public String toString() {
        return super.toString() + String.format(" | Say kho: %s", coSayKho ? "Co (+20.000d)" : "Khong");
    }
}
