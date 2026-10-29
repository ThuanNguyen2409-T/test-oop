package petcare.util;

/**
 * Ngoai le nem ra khi luong nhap lieu console bi dong hoac gap ky tu EOF (Ctrl+Z / Ctrl+D / het luong).
 */
public class KetThucNhapException extends RuntimeException {
    public KetThucNhapException(String message) {
        super(message);
    }

    public KetThucNhapException(String message, Throwable cause) {
        super(message, cause);
    }
}
