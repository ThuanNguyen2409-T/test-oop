import petcare.persistence.DuLieuException;
import petcare.service.CuaHangService;
import petcare.service.ValidationException;
import petcare.ui.MenuConsole;
import petcare.util.KetThucNhapException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Diem khoi dau (Entry point) cua ung dung quan ly cua hang cham soc thu cung.
 */
public class Main {
    public static void main(String[] args) {
        Path duongDanFile = Paths.get("data.txt").toAbsolutePath().normalize();
        if (args.length > 0) {
            duongDanFile = Paths.get(args[0]).toAbsolutePath().normalize();
        }

        System.out.println("===============================================================");
        System.out.println("  UNG DUNG QUAN LY CUA HANG CHAM SOC THU CUNG (PET CARE STORE) ");
        System.out.println("===============================================================");
        System.out.println("Tep tin du lieu: " + duongDanFile);

        if (!Files.exists(duongDanFile)) {
            System.err.println("[LOI NGHIEP TRONG] Khong tim thay tep tin du lieu: " + duongDanFile);
            System.err.println("Vui long dam bao tep 'data.txt' nam dung vi tri thu muc goc cua du an.");
            System.exit(1);
        }

        CuaHangService service = new CuaHangService(duongDanFile);
        try {
            service.napDuLieu();
            System.out.println("[OK] Nap du lieu ban dau thanh cong tu 'data.txt'.");
        } catch (DuLieuException | ValidationException e) {
            System.err.println("[LOI NAP DU LIEU] " + e.getMessage());
            System.err.println("He thong tu choi khoi dong vi tep du lieu khong hop le hoac bi vi pham rang buoc toan ven.");
            System.exit(1);
        }

        try {
            MenuConsole menu = new MenuConsole(service);
            menu.chay();
        } catch (KetThucNhapException e) {
            System.out.println("\n[THONG BAO] Da nhan tin hieu ket thuc luong nhap (EOF). Dang thoat chuong trinh an toan...");
        }
    }
}
