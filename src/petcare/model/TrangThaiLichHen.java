package petcare.model;

/**
 * Trang thai cua lich hen cham soc thu cung.
 */
public enum TrangThaiLichHen {
    DAT_LICH("Dat lich"),
    HOAN_THANH("Hoan thanh"),
    DA_HUY("Da huy");

    private final String moTa;

    TrangThaiLichHen(String moTa) {
        this.moTa = moTa;
    }

    public String getMoTa() {
        return moTa;
    }
}
