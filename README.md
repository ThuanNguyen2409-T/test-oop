# HỆ THỐNG QUẢN LÝ CỬA HÀNG CHĂM SÓC THÚ CƯNG (PET CARE STORE)

Ứng dụng dòng lệnh (Console Application) viết bằng **Java 17 Standard Library**, áp dụng chuẩn mực lập trình hướng đối tượng (OOP), phục vụ đồ án môn học Lập trình hướng đối tượng.

---

## 1. Hướng Dẫn Biên Dịch & Chạy Ứng Dụng

### Yêu cầu môi trường
- **JDK 17** trở lên (Oracle JDK hoặc OpenJDK) đã được cấu hình biến môi trường `JAVA_HOME` và `PATH`.
- Hệ điều hành: Windows (hỗ trợ hiển thị tiếng Việt UTF-8 qua `chcp 65001`). Các script `.bat` đã được kiểm tra trong đường dẫn chứa khoảng trắng.

### Cách 1: Chạy bằng kịch bản dòng lệnh (.bat)
1. **Chạy ứng dụng chính**:
   ```cmd
   run.bat
   ```
   Kịch bản sẽ tự động tạo thư mục `bin/`, tạo tệp phản hồi (argument file) với đường dẫn trích dẫn an toàn dạng `/`, biên dịch toàn bộ mã nguồn `src/` với cờ `--release 17 -encoding UTF-8` và chạy lớp `Main`. Không dùng lệnh `pause` gây nghẽn tự động hóa.

2. **Chạy bộ kiểm thử tự động (Assert-based regression suite)**:
   ```cmd
   test.bat
   ```
   Kịch bản sẽ biên dịch và chạy `PetCareTestSuite` với cờ kích hoạt kiểm tra khẳng định `java -ea`. Tất cả các test đều thao tác trên thư mục tạm (`java.nio.file.Files.createTempDirectory`), bảo toàn tuyệt đối tệp mẫu `data.txt`.

### Cách 2: Chạy trong Visual Studio Code
Dự án đã tích hợp sẵn thư mục cấu hình `.vscode/`:
1. Mở thư mục `D:\5.OOP` trong VSCode.
2. Cài đặt extension: **Extension Pack for Java** (Microsoft).
3. Vào tab **Run and Debug** (`Ctrl + Shift + D`):
   - Chọn cấu hình: **Chạy Ung Dung Pet Care (Main)** để chạy console trên `integratedTerminal`.
   - Chọn cấu hình: **Chay Bo Kiem Thu (PetCareTestSuite)** để chạy kiểm thử khẳng định (-ea).

Trong terminal PowerShell tại thư mục dự án, dùng `.\run.bat` để chạy và `.\test.bat` để kiểm thử. Chương trình có menu quản lý khách hàng, thú cưng, nhân viên, dịch vụ, sản phẩm, lịch hẹn, hóa đơn và thống kê. Menu hóa đơn có mục **9. Tìm kiếm** theo mã hóa đơn, mã khách hàng, mã nhân viên, ngày hoặc trạng thái; mục **3** xem đầy đủ chi tiết theo mã.

---

## 2. Bảng Ánh Xạ Tiêu Chí Chấm Điểm OOP (Rubric Mapping)

| Tiêu chí | Hiện thực trong dự án | Vị trí mã nguồn |
| :--- | :--- | :--- |
| **Tối thiểu 15 lớp miền** | **19 lớp miền nghiệp vụ hoàn chỉnh**: `Nguoi`, `KhachHang`, `NhanVien`, `NhanVienChamSoc`, `ThuNgan`, `NhanVienQuanLy`, `ThuCung`, `Cho`, `Meo`, `DichVu`, `DichVuTam`, `DichVuCatTia`, `DichVuLuuTru`, `SanPham`, `ThucAn`, `PhuKien`, `LichHen`, `ChiTietHoaDon`, `HoaDon`. | Thư mục `src/petcare/model/` |
| **Constructors** | Mọi lớp đều có hàm khởi tạo tường minh gán giá trị hợp lệ. Lớp con gọi `super(...)` của lớp cha. Xác thực các giá trị cơ bản ngay tại biên constructor/setter; các ràng buộc toàn vẹn phức tạp (định dạng SĐT, không âm phụ cấp/lương, khóa ngoại, không xung đột) được kiểm tra tập trung tại tầng nghiệp vụ `CuaHangService` và phương thức `kiemTraHopLeToanBo`. | Toàn bộ các lớp trong `petcare.model` & `CuaHangService` |
| **Thuộc tính riêng tư & Đóng gói chặt chẽ** | - 100% thuộc tính được đóng gói `private final` (đối với mã ID bất biến và snapshot chi tiết hóa đơn).<br>- Không có setter tùy tiện cho mã định danh hoặc số tiền snapshot.<br>- `HoaDon` bảo vệ danh sách chi tiết bằng bản sao phòng thủ (defensive copy), đóng hoàn toàn mọi phương thức sửa đổi khi hóa đơn đã ở trạng thái `DA_THANH_TOAN`. | Toàn bộ các lớp miền |
| **Kế thừa hợp lý** | - `Nguoi` &rarr; `KhachHang`, `NhanVien`<br>- `NhanVien` &rarr; `NhanVienChamSoc`, `ThuNgan`, `NhanVienQuanLy`<br>- `ThuCung` &rarr; `Cho`, `Meo`<br>- `DichVu` &rarr; `DichVuTam`, `DichVuCatTia`, `DichVuLuuTru`<br>- `SanPham` &rarr; `ThucAn`, `PhuKien` | Phân cấp phân loại tự nhiên |
| **Đa hình thực tế (Polymorphism)** | 1. `tinhGia(int)`: Dịch vụ tắm phụ thu sấy khô, cắt tỉa phụ thu tạo kiểu, lưu trú phụ thu phòng VIP.<br>2. `tinhLuong()`: Nhân viên chăm sóc nhận phụ cấp chuyên môn, thu ngân nhận hoa hồng, quản lý nhận phụ cấp chức vụ.<br>3. `TinhTien`: Dùng chung để tính tiền và tạo snapshot cho cả `SanPham` và `DichVu` trên hóa đơn.<br>4. `CoMa`: Dùng chung cho thuật toán tìm kiếm và sinh mã tự động. | `DichVu*.java`, `NhanVien*.java`, `CuaHangService.java` |
| **Lớp & Phương thức trừu tượng** | - `Nguoi` có phương thức trừu tượng `getVaiTro()`<br>- `NhanVien` có `tinhLuong()`<br>- `ThuCung` có `getLoaiThuCung()`, `getTiengKeu()`<br>- `DichVu` có `tinhGia(int)`, `getDonViTinh()`<br>- `SanPham` có `getLoaiSanPham()` | `Nguoi.java`, `NhanVien.java`, `ThuCung.java`, `DichVu.java`, `SanPham.java` |
| **Giao diện (Interface)** | - `CoMa`: Định nghĩa thực thể có mã nhận diện `getMa()`.<br>- `TinhTien`: Định nghĩa thực thể có thể định giá `tinhThanhTien(int)`. | `src/petcare/interfaces/` |
| **Thuộc tính & Phương thức tĩnh** | - `LichHen.DINH_DANG_NGAY_GIO`, `LichHen.coGiaoNhauKhoangThoiGian(...)`<br>- `HoaDon.TI_LE_THUE_VAT`<br>- `DinhDangUtil`: Phương thức tiện ích định dạng tiền và thời gian STRICT.<br>- `CuaHangService.sinhMaMoi(...)`: Thuật toán sinh mã tiếp theo dựa trên dữ liệu hiện có. | `LichHen.java`, `HoaDon.java`, `DinhDangUtil.java`, `CuaHangService.java` |
| **Lớp danh sách mảng đối tượng tự xây dựng** | Lớp `DanhSach<T>` tự quản lý bộ nhớ mảng `Object[]`, tự động nhân đôi kích thước khi đầy, hỗ trợ `add`, `get`, `set`, `remove(index)`, `remove(item)`, `size`, `isEmpty`, `clear` và triển khai `Iterable<T>` để duyệt bằng `for-each`. | `src/petcare/collection/DanhSach.java` |
| **Đọc / ghi tệp tin văn bản** | Đọc/ghi tệp tin `data.txt` chuẩn UTF-8, định dạng phân tách bằng dấu gạch đứng `\|` có cơ chế escape an toàn. Ghi ra tệp tạm `.tmp` sau đó thay thế nguyên tử (atomic move). Xác thực toàn bộ staging trước khi nạp. Rollback bộ nhớ nếu ghi tệp thất bại. | `src/petcare/persistence/FileStorage.java`, `CuaHangService.java` |

---

## 3. Đặc Tả Định Dạng Tệp Dữ Liệu `data.txt`

Tệp dữ liệu sử dụng bảng mã **UTF-8**, dòng đầu tiên bắt buộc phải là tiêu đề phiên bản `# PETCARE_STORE_DATA_V1`. Các khối dữ liệu được bắt đầu bằng thẻ `[TEN_SECTION]`. Các trường trên mỗi dòng được phân tách bằng dấu `|`.

Các ký tự đặc biệt (`\`, `|`, `\n`, `\r`) được escape an toàn khi lưu (`\\`, `\|`, `\n`, `\r`) và unescape khi đọc. Parser từ chối các ký tự escape không hợp lệ hoặc treo ở cuối dòng.

### Số lượng và thứ tự các trường bắt buộc (Strict column counts):
1. `[KHACH_HANG]` (Đúng 6 trường):
   `ma|hoTen|soDienThoai|email|diaChi|ngayDangKy(uuuu-MM-dd)`
2. `[NHAN_VIEN]` (Đúng 9 trường cho cả 3 vai trò):
   - `CHAM_SOC|ma|hoTen|soDienThoai|email|luongCoBan|heSoLuong|chuyenMon|phuCap`
   - `THU_NGAN|ma|hoTen|soDienThoai|email|luongCoBan|heSoLuong|caLamViec|hoaHong`
   - `QUAN_LY|ma|hoTen|soDienThoai|email|luongCoBan|heSoLuong|phuCapChucVu|`
3. `[THU_CUNG]` (Đúng 7 trường):
   - `CHO|ma|ten|maKhachHang|tuoi|canNang|giongCho`
   - `MEO|ma|ten|maKhachHang|tuoi|canNang|mauLong`
4. `[DICH_VU]` (Đúng 6 trường):
   - `TAM|ma|ten|donGiaCoBan|moTa|coSayKho(true/false)`
   - `CAT_TIA|ma|ten|donGiaCoBan|moTa|kieuCat`
   - `LUU_TRU|ma|ten|donGiaCoBan|moTa|loaiPhong`
5. `[SAN_PHAM]` (Đúng 7 trường):
   - `THUC_AN|ma|ten|donGia|soLuongTon|hanSuDung(uuuu-MM-dd)|khoiLuongGram`
   - `PHU_KIEN|ma|ten|donGia|soLuongTon|chatLieu|kichCo`
6. `[LICH_HEN]` (Đúng 9 trường):
   `ma|maKhachHang|maThuCung|maDichVu|maNhanVien|thoiGianBatDau(uuuu-MM-dd HH:mm)|thoiGianKetThuc|trangThai|ghiChu`
7. `[HOA_DON]` (Đúng 7 trường):
   `ma|maKhachHang|maNhanVien|ngayTao|trangThai|ngayThanhToan|tongTien`
8. `[CHI_TIET_HOA_DON]` (Đúng 7 trường):
   `maHoaDon|maMuc|tenMuc|loaiMuc(SAN_PHAM/DICH_VU)|donGia|soLuong|thanhTien`

---

## 4. Các Quy Tắc Nghiệp Vụ Cốt Lõi

1. **Chính sách Snapshot Đơn Giá Hiệu Dụng & Tính Toán Hóa Đơn**:
   - Khi đưa một mục vào hóa đơn, đơn giá snapshot được tính theo đơn giá hiệu dụng của 1 đơn vị: `item.tinhThanhTien(1)`. Nhờ vậy, các dịch vụ có phụ phí đa hình (như sấy khô +20.000d, cắt tạo kiểu +50.000d, phòng VIP +100.000d) được lưu trữ đơn giá đầy đủ.
   - Khi chỉnh sửa số lượng trên hóa đơn nháp, hệ thống sử dụng đơn giá hiệu dụng snapshot đã lưu, không tham chiếu lại danh mục gốc.
   - Khi nạp tệp: mọi dòng chi tiết đều phải thỏa mãn `thanhTien == donGia * soLuong`, và `tongTien` của hóa đơn phải bằng chính xác tổng các dòng.
   - Hóa đơn đã thanh toán bắt buộc không rỗng, phải có thời gian thanh toán $\ge$ thời gian tạo. Hóa đơn nháp không được có thời gian thanh toán.
2. **Kiểm tra xung đột lịch hẹn & Phân công nhân viên**:
   - Nhân viên phụ trách lịch hẹn bắt buộc phải thuộc vai trò Nhân viên chăm sóc (`NhanVienChamSoc`).
   - Hai lịch hẹn xung đột khi khoảng thời gian giao nhau ($S_1 < E_2$ và $S_2 < E_1$) và trùng nhân viên hoặc trùng thú cưng.
   - Cho phép các khoảng thời gian liền kề (ví dụ: ca 1 từ 09:00 - 10:30, ca 2 từ 10:30 - 11:30 không xung đột).
   - Khi kích hoạt lại một lịch hẹn đã hủy (`DA_HUY` &rarr; `DAT_LICH`), hệ thống tái kiểm tra xung đột thời gian với tất cả các lịch đang hoạt động khác.
3. **Tính Nguyên Tử Giao Dịch Trong Bộ Nhớ (Transaction Rollback)**:
   - Tất cả các thao tác thay đổi dữ liệu (thêm/sửa/xóa/thanh toán) đều thực hiện cơ chế snapshot trước khi biến đổi bộ nhớ.
   - Nếu quá trình ghi đĩa thất bại (ví dụ: bị khóa tệp tạm), trạng thái bộ nhớ của dịch vụ được phục hồi 100% về trạng thái hợp lệ gần nhất, ngăn chặn việc báo lỗi nhưng bộ nhớ vẫn giữ thay đổi sai lệch.
4. **Ràng Buộc Toàn Vẹn Khóa Ngoại Khi Xóa**:
   - Ngăn chặn xóa bất kỳ khách hàng, thú cưng, nhân viên, dịch vụ hoặc sản phẩm nào đã từng được tham chiếu trong lịch hẹn hoặc chi tiết hóa đơn (kể cả hóa đơn đã thanh toán trong quá khứ).
5. **Thẩm Định Thời Gian STRICT & Số Học Hữu Hạn**:
   - Sử dụng định dạng `uuuu-MM-dd` và `uuuu-MM-dd HH:mm` với `ResolverStyle.STRICT`, ngăn chặn triệt để việc phân tích các ngày không tồn tại (như 2026-02-30).
   - Từ chối các giá trị số thực không hữu hạn (`Infinity`, `NaN`) hoặc âm không hợp lệ.
