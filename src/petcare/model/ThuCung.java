package petcare.model;

import petcare.interfaces.CoMa;

/**
 * Lop truu tuong dai dien cho thu cung cua khach hang.
 */
public abstract class ThuCung implements CoMa {
    private final String ma;
    private String ten;
    private String maKhachHang;
    private int tuoi;
    private double canNang;

    public ThuCung(String ma, String ten, String maKhachHang, int tuoi, double canNang) {
        if (ma == null || ma.trim().isEmpty()) {
            throw new IllegalArgumentException("Ma thu cung khong duoc de trong.");
        }
        if (tuoi < 0) {
            throw new IllegalArgumentException("Tuoi thu cung khong duoc am.");
        }
        if (!Double.isFinite(canNang) || canNang <= 0) {
            throw new IllegalArgumentException("Can nang thu cung phai la so thuc huu han > 0.");
        }
        this.ma = ma.trim();
        this.ten = ten != null ? ten.trim() : "";
        this.maKhachHang = maKhachHang != null ? maKhachHang.trim() : "";
        this.tuoi = tuoi;
        this.canNang = canNang;
    }

    @Override
    public String getMa() {
        return ma;
    }

    public String getTen() {
        return ten;
    }

    public void setTen(String ten) {
        if (ten == null || ten.trim().isEmpty()) {
            throw new IllegalArgumentException("Ten thu cung khong duoc de trong.");
        }
        this.ten = ten.trim();
    }

    public String getMaKhachHang() {
        return maKhachHang;
    }

    public void setMaKhachHang(String maKhachHang) {
        if (maKhachHang == null || maKhachHang.trim().isEmpty()) {
            throw new IllegalArgumentException("Ma chu so huu khong duoc de trong.");
        }
        this.maKhachHang = maKhachHang.trim();
    }

    public int getTuoi() {
        return tuoi;
    }

    public void setTuoi(int tuoi) {
        if (tuoi < 0) {
            throw new IllegalArgumentException("Tuoi thu cung khong duoc am.");
        }
        this.tuoi = tuoi;
    }

    public double getCanNang() {
        return canNang;
    }

    public void setCanNang(double canNang) {
        if (!Double.isFinite(canNang) || canNang <= 0) {
            throw new IllegalArgumentException("Can nang thu cung phai la so thuc huu han > 0.");
        }
        this.canNang = canNang;
    }

    public abstract String getLoaiThuCung();

    public abstract String getTiengKeu();

    @Override
    public String toString() {
        return String.format("[%s] %s (%s) | Chu: %s | Tuoi: %d | Can nang: %.1fkg | Tieng keu: %s",
                ma, ten, getLoaiThuCung(), maKhachHang, tuoi, canNang, getTiengKeu());
    }
}
