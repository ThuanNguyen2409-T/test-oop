package petcare.collection;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Lop danh sach tong quat tu cai dat bang mang dong (Object[]),
 * ho tro tu dong mo rong kich thuoc khi vuot qua suc chua.
 *
 * @param <T> Kieu phan tu trong danh sach
 */
public class DanhSach<T> implements Iterable<T> {
    private static final int SUC_CHUA_MAC_DINH = 10;
    private Object[] phanTu;
    private int soLuong;

    public DanhSach() {
        this(SUC_CHUA_MAC_DINH);
    }

    public DanhSach(int sucChuaBanDau) {
        if (sucChuaBanDau < 0) {
            throw new IllegalArgumentException("Suc chua ban dau khong hop le: " + sucChuaBanDau);
        }
        this.phanTu = new Object[sucChuaBanDau];
        this.soLuong = 0;
    }

    private void damBaoSucChua(int sucChuaCanThiet) {
        if (sucChuaCanThiet > phanTu.length) {
            int sucChuaMoi = phanTu.length == 0 ? SUC_CHUA_MAC_DINH : phanTu.length * 2;
            if (sucChuaMoi < sucChuaCanThiet) {
                sucChuaMoi = sucChuaCanThiet;
            }
            phanTu = Arrays.copyOf(phanTu, sucChuaMoi);
        }
    }

    public void add(T item) {
        damBaoSucChua(soLuong + 1);
        phanTu[soLuong++] = item;
    }

    @SuppressWarnings("unchecked")
    public T get(int index) {
        kiemTraViTri(index);
        return (T) phanTu[index];
    }

    @SuppressWarnings("unchecked")
    public T set(int index, T item) {
        kiemTraViTri(index);
        T cu = (T) phanTu[index];
        phanTu[index] = item;
        return cu;
    }

    @SuppressWarnings("unchecked")
    public T remove(int index) {
        kiemTraViTri(index);
        T biXoa = (T) phanTu[index];
        int soPhanTuDiChuyen = soLuong - index - 1;
        if (soPhanTuDiChuyen > 0) {
            System.arraycopy(phanTu, index + 1, phanTu, index, soPhanTuDiChuyen);
        }
        phanTu[--soLuong] = null;
        return biXoa;
    }

    public boolean remove(T item) {
        int idx = indexOf(item);
        if (idx >= 0) {
            remove(idx);
            return true;
        }
        return false;
    }

    public int indexOf(T item) {
        if (item == null) {
            for (int i = 0; i < soLuong; i++) {
                if (phanTu[i] == null) {
                    return i;
                }
            }
        } else {
            for (int i = 0; i < soLuong; i++) {
                if (item.equals(phanTu[i])) {
                    return i;
                }
            }
        }
        return -1;
    }

    public boolean contains(T item) {
        return indexOf(item) >= 0;
    }

    public int size() {
        return soLuong;
    }

    public boolean isEmpty() {
        return soLuong == 0;
    }

    public void clear() {
        for (int i = 0; i < soLuong; i++) {
            phanTu[i] = null;
        }
        soLuong = 0;
    }

    public int getSucChua() {
        return phanTu.length;
    }

    private void kiemTraViTri(int index) {
        if (index < 0 || index >= soLuong) {
            throw new IndexOutOfBoundsException("Chi muc ngoai pham vi: " + index + " (kich thuoc: " + soLuong + ")");
        }
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private int conTro = 0;

            @Override
            public boolean hasNext() {
                return conTro < soLuong;
            }

            @Override
            @SuppressWarnings("unchecked")
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return (T) phanTu[conTro++];
            }
        };
    }
}
