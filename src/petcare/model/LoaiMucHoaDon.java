package petcare.model;

/**
 * Loai muc ghi tren chi tiet hoa don.
 */
public enum LoaiMucHoaDon {
    SAN_PHAM("San pham"),
    DICH_VU("Dich vu");

    private final String moTa;

    LoaiMucHoaDon(String moTa) {
        this.moTa = moTa;
    }

    public String getMoTa() {
        return moTa;
    }
}
