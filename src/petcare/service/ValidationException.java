package petcare.service;

/**
 * Ngoai le vi pham quy tac nghiep vu (rang buoc toan ven, trung lap, ton kho, xung dot lich hen).
 */
public class ValidationException extends Exception {
    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
