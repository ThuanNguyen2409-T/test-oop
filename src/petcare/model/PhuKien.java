package petcare.model;

import java.math.BigDecimal;

/**
 * San pham phu kien, do choi, trang phuc cho thu cung.
 */
public class PhuKien extends SanPham {
    private String chatLieu;
    private String kichCo;

    public PhuKien(String ma, String ten, BigDecimal donGia, int soLuongTon, String chatLieu, String kichCo) {
        super(ma, ten, donGia, soLuongTon);
        if (chatLieu == null || chatLieu.trim().isEmpty()) {
            throw new IllegalArgumentException("Chat lieu khong duoc de trong.");
        }
        if (kichCo == null || kichCo.trim().isEmpty()) {
            throw new IllegalArgumentException("Kich co khong duoc de trong.");
        }
        this.chatLieu = chatLieu.trim();
        this.kichCo = kichCo.trim();
    }

    public String getChatLieu() {
        return chatLieu;
    }

    public void setChatLieu(String chatLieu) {
        if (chatLieu == null || chatLieu.trim().isEmpty()) {
            throw new IllegalArgumentException("Chat lieu khong duoc de trong.");
        }
        this.chatLieu = chatLieu.trim();
    }

    public String getKichCo() {
        return kichCo;
    }

    public void setKichCo(String kichCo) {
        if (kichCo == null || kichCo.trim().isEmpty()) {
            throw new IllegalArgumentException("Kich co khong duoc de trong.");
        }
        this.kichCo = kichCo.trim();
    }

    @Override
    public String getLoaiSanPham() {
        return "Phu kien";
    }

    @Override
    public String toString() {
        return super.toString() + String.format(" | Chat lieu: %s | Kich co: %s", chatLieu, kichCo);
    }
}
