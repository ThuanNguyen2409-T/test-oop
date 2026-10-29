package petcare.model;

import petcare.interfaces.CoMa;

/**
 * Lop truu tuong dai dien cho con nguoi (khach hang, nhan vien).
 */
public abstract class Nguoi implements CoMa {
    public static final String SO_DIEN_THOAI_REGEX = "^0\\d{9,10}$";

    private final String ma;
    private String hoTen;
    private String soDienThoai;
    private String email;

    public Nguoi(String ma, String hoTen, String soDienThoai, String email) {
        if (ma == null || ma.trim().isEmpty()) {
            throw new IllegalArgumentException("Ma dinh danh khong duoc de trong.");
        }
        this.ma = ma.trim();
        this.hoTen = hoTen != null ? hoTen.trim() : "";
        this.soDienThoai = soDienThoai != null ? soDienThoai.trim() : "";
        this.email = email != null ? email.trim() : "";
    }

    public static boolean hopLeSoDienThoai(String sdt) {
        return sdt != null && sdt.matches(SO_DIEN_THOAI_REGEX);
    }

    @Override
    public String getMa() {
        return ma;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        if (hoTen == null || hoTen.trim().isEmpty()) {
            throw new IllegalArgumentException("Ho ten khong duoc de trong.");
        }
        this.hoTen = hoTen.trim();
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        if (!hopLeSoDienThoai(soDienThoai)) {
            throw new IllegalArgumentException("So dien thoai khong dung dinh dang hop le.");
        }
        this.soDienThoai = soDienThoai.trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email != null ? email.trim() : "";
    }

    public abstract String getVaiTro();

    @Override
    public String toString() {
        return String.format("[%s] %s | SDT: %s | Email: %s | Vai tro: %s",
                ma, hoTen, soDienThoai, email, getVaiTro());
    }
}
