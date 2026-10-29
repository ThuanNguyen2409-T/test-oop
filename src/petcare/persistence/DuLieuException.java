package petcare.persistence;

/**
 * Ngoai le khi doc/ghi du lieu tep tin that bai hoac du lieu khong dung dinh dang.
 */
public class DuLieuException extends Exception {
    public DuLieuException(String message) {
        super(message);
    }

    public DuLieuException(String message, Throwable cause) {
        super(message, cause);
    }
}
