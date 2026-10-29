package petcare.model;

/**
 * Lop dai dien cho thu cung loai cho.
 */
public class Cho extends ThuCung {
    private String giongCho;

    public Cho(String ma, String ten, String maKhachHang, int tuoi, double canNang, String giongCho) {
        super(ma, ten, maKhachHang, tuoi, canNang);
        if (giongCho == null || giongCho.trim().isEmpty()) {
            throw new IllegalArgumentException("Giong cho khong duoc de trong.");
        }
        this.giongCho = giongCho.trim();
    }

    public String getGiongCho() {
        return giongCho;
    }

    public void setGiongCho(String giongCho) {
        if (giongCho == null || giongCho.trim().isEmpty()) {
            throw new IllegalArgumentException("Giong cho khong duoc de trong.");
        }
        this.giongCho = giongCho.trim();
    }

    @Override
    public String getLoaiThuCung() {
        return "Cho";
    }

    @Override
    public String getTiengKeu() {
        return "Gau gau";
    }

    @Override
    public String toString() {
        return super.toString() + String.format(" | Giong: %s", giongCho);
    }
}
