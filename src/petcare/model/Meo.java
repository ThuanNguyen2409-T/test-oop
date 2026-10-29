package petcare.model;

/**
 * Lop dai dien cho thu cung loai meo.
 */
public class Meo extends ThuCung {
    private String mauLong;

    public Meo(String ma, String ten, String maKhachHang, int tuoi, double canNang, String mauLong) {
        super(ma, ten, maKhachHang, tuoi, canNang);
        if (mauLong == null || mauLong.trim().isEmpty()) {
            throw new IllegalArgumentException("Mau long meo khong duoc de trong.");
        }
        this.mauLong = mauLong.trim();
    }

    public String getMauLong() {
        return mauLong;
    }

    public void setMauLong(String mauLong) {
        if (mauLong == null || mauLong.trim().isEmpty()) {
            throw new IllegalArgumentException("Mau long meo khong duoc de trong.");
        }
        this.mauLong = mauLong.trim();
    }

    @Override
    public String getLoaiThuCung() {
        return "Meo";
    }

    @Override
    public String getTiengKeu() {
        return "Meo meo";
    }

    @Override
    public String toString() {
        return super.toString() + String.format(" | Mau long: %s", mauLong);
    }
}
