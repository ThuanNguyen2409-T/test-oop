package petcare.model;

/**
 * Trang thai thanh toan cua hoa don.
 */
public enum TrangThaiHoaDon {
    CHUA_THANH_TOAN("Chua thanh toan (Nhap)"),
    DA_THANH_TOAN("Da thanh toan");

    private final String moTa;

    TrangThaiHoaDon(String moTa) {
        this.moTa = moTa;
    }

    public String getMoTa() {
        return moTa;
    }
}
