# KỊCH BẢN DEMO BẢO VỆ ĐỒ ÁN (STEP-BY-STEP CONSOLE DEMO SCRIPT)
## Đề tài: Hệ Thống Quản Lý Cửa Hàng Chăm Sóc Thú Cưng (Pet Care Store)

---

## MỤC TIÊU BUỔI BẢO VỆ
Kịch bản này cung cấp trình tự thao tác mẫu trên bảng điều khiển dòng lệnh (Console) để minh chứng đầy đủ toàn bộ các tiêu chí chấm điểm: **19 lớp miền OOP, Đóng gói, Kế thừa, Đa hình thực tế, Interface, Mảng động `DanhSach<T>`, Ràng buộc toàn vẹn dữ liệu, Cơ chế thanh toán an toàn, Bảo toàn snapshot phụ phí và Lưu trữ tệp tin bền vững.**

---

## BƯỚC 1: Khởi Động & Kiểm Tra Nạp Dữ Liệu Ban Đầu
1. Mở cửa sổ dòng lệnh tại thư mục gốc `D:\5.OOP`.
2. Chạy kịch bản:
   ```cmd
   run.bat
   ```
3. Quan sát màn hình khởi động:
   - Hệ thống hiển thị:
     `Tep tin du lieu: ...\data.txt`
     `[OK] Nap du lieu ban dau thanh cong tu 'data.txt'.`
   - Menu chính gồm 8 chức năng quản lý và mục `0. Thoat chuong trinh`.

---

## BƯỚC 2: Minh Chứng Đa Hình (Polymorphism) & Sửa Dữ Liệu Từng Vai Trò
1. Tại menu chính, chọn `3` (**Quan ly Nhan vien**).
2. Chọn `1` (**Danh sach nhan vien**):
   - Giảng viên quan sát thấy 4 nhân viên thuộc 3 vai trò khác nhau được tính lương hoàn toàn đa hình:
     + `NV001`: Nhân viên chăm sóc &rarr; Lương = $8.000.000 \times 1.2 + 1.500.000 \text{ (phụ cấp)} = 11.100.000$ VNĐ.
     + `NV002`: Nhân viên chăm sóc &rarr; Lương = $7.500.000 \times 1.1 + 1.000.000 = 9.250.000$ VNĐ.
     + `NV003`: Thu ngân &rarr; Lương = $7.000.000 \times 1.0 + 800.000 \text{ (hoa hồng)} = 7.800.000$ VNĐ.
     + `NV004`: Quản lý &rarr; Lương = $12.000.000 \times 1.5 + 3.000.000 \text{ (phụ cấp chức vụ)} = 21.000.000$ VNĐ.
3. Chọn `3` (**Sua nhan vien**):
   - Nhập `NV001`: Hệ thống tự nhận diện đây là Nhân viên chăm sóc và cho phép sửa chuyên môn và phụ cấp nghề.
   - Nhập `NV003`: Hệ thống cho phép sửa ca làm việc và hoa hồng doanh số.
   - Nhập `NV004`: Hệ thống cho phép sửa phụ cấp chức vụ.
4. Chọn `0` để quay lại menu chính.

---

## BƯỚC 3: Minh Chứng Đa Hình Tính Giá Dịch Vụ
1. Tại menu chính, chọn `4` (**Quan ly Dich vu**).
2. Chọn `1` (**Danh sach dich vu**):
   - `DV001` (Tắm sấy khô): Có sấy khô &rarr; Tự động tính thêm 20.000 VNĐ vào giá mỗi lần (Đơn giá hiệu dụng: 170.000 VNĐ).
   - `DV002` (Cắt tỉa tạo kiểu): Kiểu cắt "Tao kieu" &rarr; Tự động cộng phụ phí 50.000 VNĐ (Đơn giá hiệu dụng: 300.000 VNĐ).
   - `DV004` (Khách sạn lưu trú): Phòng "VIP" &rarr; Tự động cộng 100.000 VNĐ/ngày.
3. Chọn `3` (**Sua dich vu**): Hệ thống cho phép sửa đổi các thuộc tính đặc thù của từng loại hình dịch vụ.
4. Chọn `0` để quay lại.

---

## BƯỚC 4: Minh Chứng Đặt Lịch Hẹn & Thuật Toán Bắt Xung Đột
1. Tại menu chính, chọn `6` (**Quan ly Lich hen**).
2. Chọn `1` (**Danh sach tat ca lich hen**):
   - Lưu ý lịch hẹn `LH001`: Khách `KH001`, thú cưng `TC001`, nhân viên `NV001` từ `2026-10-26 09:00` đến `10:30`.
3. **Thử nghiệm 1: Xung đột nhân viên chăm sóc**:
   - Chọn `2` (Đặt lịch hẹn mới):
     + Mã KH: `KH002`
     + Mã TC: `TC003` (Chó Ki Ki của KH002)
     + Mã DV: `DV001`
     + Mã NV: `NV001` (Nhân viên này đang bận ca LH001)
     + Thời gian bắt đầu: `2026-10-26 09:30` (trùng giờ)
     + Thời gian kết thúc: `2026-10-26 11:00`
   - **Kết quả**: Hệ thống từ chối ngay lập tức và báo:
     `[Loi nghiep vu]: Xung dot lich hen voi ma LH001 ... Nhan vien hoac thu cung da co lich hen vao gio nay!`
4. **Thử nghiệm 2: Phân công sai vai trò nhân viên**:
   - Chọn `2` (Đặt lịch hẹn mới):
     + Nhập mã NV: `NV003` (Thu ngân) hoặc `NV004` (Quản lý).
   - **Kết quả**: Hệ thống báo lỗi:
     `[Loi nghiep vu]: Nhan vien phu trach lich hen phai la Nhan vien cham soc (NhanVienChamSoc): NV003`
5. **Thử nghiệm 3: Khoảng giờ liền kề (Adjacent intervals)**:
   - Ca 1 kết thúc lúc 10:30. Đặt ca 2 với `NV001` và `TC002` từ `10:30` đến `11:30`.
   - **Kết quả**: Đặt lịch thành công, chứng minh giải thuật không bắt nhầm ca nối tiếp!
6. **Thử nghiệm 4: Tìm kiếm lịch hẹn**:
   - Chọn `7` (**Tim kiem lich hen theo tu khoa**): Nhập `Poodle` hoặc mã nhân viên `NV001` &rarr; hiển thị đúng các ca liên quan.

---

## BƯỚC 5: Minh Chứng Quy Trình Hóa Đơn & Bảo Toàn Snapshot Phụ Phí
1. Tại menu chính, chọn `7` (**Quan ly Hoa don & Thanh toan**).
2. Chọn `1` (**Danh sach hoa don**):
   - Quan sát hóa đơn nháp `HD003` của khách `KH002`.
3. Chọn `3` (**Xem chi tiet hoa don**):
   - Nhập `HD003`:
     + Dòng 1: `SP001` x 1 (Giá 120.000)
     + Dòng 2: `SP001` x 2 (Giá 120.000) &rarr; **2 dòng sản phẩm trùng lặp**
     + Dòng 3: `DV002` x 1 (Đơn giá hiệu dụng: 300.000 đã gồm 50k phụ phí tạo kiểu)
     + Tổng tiền: 660.000 VNĐ.
4. **Sửa số lượng dòng dịch vụ để chứng minh bảo toàn phụ phí**:
   - Chọn `5` (**Sua so luong muc trong hoa don nhap**):
     + Nhập mã hóa đơn: `HD003`
     + Nhập STT mục: `3` (dịch vụ cắt tỉa tạo kiểu DV002)
     + Nhập số lượng mới: `2`
   - Quan sát chi tiết hóa đơn: Thành tiền trở thành $300.000 \times 2 = 600.000$ VNĐ, bảo toàn đơn giá hiệu dụng 300.000 VNĐ mà không bị tụt về đơn giá cơ bản 250.000 VNĐ!
5. Chọn `8` (**Thanh toan hoa don**):
   - Nhập `HD003`, xác nhận `y`.
   - **Hệ thống xử lý**:
     + Gom nhóm: Sản phẩm `SP001` cần tổng cộng $1 + 2 = 3$ gói (chống tràn số với `Math.addExact`).
     + Kiểm tra tồn kho của `SP001` (ban đầu có 20 &ge; 3) &rarr; Thỏa mãn.
     + Trừ tồn kho `SP001` từ 20 xuống còn 17 (chỉ trừ 1 lần cho tổng 3 sản phẩm).
     + Chuyển trạng thái `HD003` sang `Da thanh toan`.
     + Màn hình báo: `THANH TOAN THANH CONG! Kho hang da duoc tru tu dong va hoa don duoc chuyen sang trang thai bat bien.`
6. **Thử nghiệm tính bất biến của hóa đơn đã thanh toán**:
   - Chọn `4` (Thêm mục vào hóa đơn): nhập `HD003` &rarr; Bị từ chối: `Khong the them muc vao hoa don da thanh toan!`
   - Chọn `7` (Xóa hóa đơn): nhập `HD003` &rarr; Bị từ chối: `Khong the xoa hoa don da thanh toan!`
   - Chọn `8` (Thanh toán lại): nhập `HD003` &rarr; Bị từ chối: `Hoa don da duoc thanh toan truoc do!`

---

## BƯỚC 6: Minh Chứng Bảo Vệ Snapshot Giá Khỏi Sửa Đổi Danh Mục

Trong menu `7`, có thể chọn `9` để tìm hóa đơn: nhập `hd001` (không phân biệt hoa thường), `KH001` hoặc `2026-10-25`; chọn `3` rồi nhập mã để xem các dòng chi tiết.

1. Quay lại menu `5` (**Quan ly San pham**).
2. Chọn `3` (**Sua san pham**):
   - Nhập mã `SP001`.
   - Đổi giá từ 120.000 thành 999.000 VNĐ.
3. Quay lại menu `7` (**Quan ly Hoa don**), chọn `3` (**Xem chi tiet hoa don**):
   - Nhập `HD001` hoặc `HD003`:
   - Dòng chi tiết của `SP001` trên hóa đơn vẫn giữ nguyên giá snapshot cũ là 120.000 VNĐ! Tổng tiền hóa đơn không hề bị suy suyển.

---

## BƯỚC 7: Minh Chứng Ràng Buộc Khóa Ngoại (Chặn Xóa Cả Khi Có Snapshot)
1. Chọn menu `5` (**Quan ly San pham**).
2. Chọn `4` (**Xoa san pham**):
   - Nhập mã `SP001` (sản phẩm đã ghi nhận trong hóa đơn HD001 và HD003).
   - Xác nhận `y`.
   - **Kết quả**: Hệ thống ngăn chặn ngay:
     `[Loi nghiep vu]: Khong the xoa san pham SP001 vi da duoc ghi nhan trong hoa don: HD001 (Da thanh toan).`

---

## BƯỚC 8: Minh Chứng Báo Cáo Thống Kê
1. Tại menu chính, chọn `8` (**Bao cao & Thong ke**).
2. Chọn `1` (**Tong doanh thu thuc te**):
   - Hệ thống tự động tính tổng tiền của tất cả các hóa đơn có trạng thái `DA_THANH_TOAN`.
3. Chọn `2` (**Danh sach san pham ton kho thap**):
   - Nhập ngưỡng cảnh báo: `5`.
   - Hệ thống lọc ra các sản phẩm sắp hết hàng cần nhập thêm (ví dụ: Áo len mùa đông `SP004` chỉ còn 4 chiếc).
4. Chọn `3` (**Danh sach lich hen theo ngay**):
   - Nhập ngày: `2026-10-26`.
   - Hệ thống hiển thị tất cả các ca làm đẹp, spa được xếp lịch trong ngày đó.

---

## BƯỚC 9: Minh Chứng Bền Vững Dữ Liệu & Thoát An Toàn (EOF / Exit)
1. Chọn `0` để thoát chương trình hoặc gửi ký hiệu kết thúc luồng EOF.
2. Hệ thống thoát êm dịu không có lỗi văng hay vòng lặp vô hạn.
3. Chạy lại:
   ```cmd
   run.bat
   ```
4. Chọn menu `1` &rarr; `2` (Thêm khách hàng mới):
   - Quan sát mã khách hàng tự động gợi ý tiếp nối: `KH005` (tự động phân tích từ dữ liệu đã có để tránh trùng lặp tuyệt đối).
