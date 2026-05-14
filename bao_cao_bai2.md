# Báo cáo Bài 2: Hủy Đơn Hàng Gấp

## Phần 1: Phân tích logic

Câu lệnh `UPDATE` trong `OrderRepository` không tác động được vào CSDL dù không báo lỗi biên dịch vì:

1. **Thiếu annotation `@Modifying`**: Mặc định, annotation `@Query` trong Spring Data JPA được sử dụng cho các truy vấn đọc dữ liệu (`SELECT`). Khi chạy các câu lệnh thay đổi dữ liệu (DML như `UPDATE`, `DELETE`), Spring Data JPA sẽ mặc định cố gắng trả về một tập kết quả (ResultSet), điều này khiến cho câu lệnh cập nhật không thể thực thi đúng cách. Việc bổ sung `@Modifying` là bắt buộc để thông báo cho Spring Data JPA biết rằng đây là câu lệnh làm thay đổi trạng thái của database thay vì truy vấn lấy dữ liệu.
2. **Thiếu quản lý Transaction (`@Transactional`)**: Mọi thao tác làm thay đổi dữ liệu trong cơ sở dữ liệu đều cần được thực hiện trong một Transaction. Nếu thiếu annotation `@Transactional` (tại tầng Service hoặc Repository), thao tác `UPDATE` sẽ không bao giờ được commit xuống Database.

## Đề xuất xử lý bẫy dữ liệu (Edge cases)

Đối với trường hợp Admin truyền `orderId` âm hoặc mã của đơn hàng đã ở trạng thái `DELIVERED`, hệ thống nên được xử lý theo hướng chặn và ném ra ngoại lệ:

1. **Validation đầu vào**: Kiểm tra nếu `orderId <= 0` ở ngay đầu vào của Service hoặc Controller, hệ thống cần chặn lại ngay và ném ra ngoại lệ như `IllegalArgumentException("Mã đơn hàng không hợp lệ")`.
2. **Kiểm tra trạng thái nghiệp vụ trước khi cập nhật**: 
   - Không nên gọi lệnh cập nhật trực tiếp mà nên gọi `findById()` để kiểm tra sự tồn tại của đơn hàng.
   - Nếu đơn hàng không tồn tại, ném ra ngoại lệ `EntityNotFoundException`.
   - Nếu trạng thái hiện tại là `DELIVERED`, hệ thống cần từ chối thao tác và ném ra ngoại lệ nghiệp vụ như `IllegalStateException("Không thể hủy đơn hàng đã giao thành công")` để có thể hiển thị thông báo lỗi phù hợp lên giao diện cho Admin.
