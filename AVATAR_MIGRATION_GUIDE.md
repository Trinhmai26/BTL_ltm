# Hướng dẫn thêm cột Avatar vào Database

## Các thay đổi đã thực hiện:

### 1. Database Schema
- Thêm cột `avatar_url VARCHAR(500)` vào bảng `users`
- Cột này lưu URL của ảnh avatar người dùng

### 2. Backend (Server)
- **User.java**: Thêm field `avatarUrl` với getter/setter
- **DatabaseManager.java**: 
  - Cập nhật `createTables()` để tạo cột avatar_url
  - Cập nhật `authenticate()` để load avatarUrl
  - Cập nhật `addUser()` để lưu avatarUrl khi đăng ký
  - Cập nhật `updateUser()` để cập nhật avatarUrl
  - Cập nhật `getAllUsers()` để load avatarUrl cho admin

### 3. Frontend (Client)
- Thêm cache `avatarCache` để lưu ảnh đã tải
- **Form đăng ký**: Thêm avatar selector với preview
- **Thông tin cá nhân**: 
  - Hiển thị avatar với mode view/edit
  - Có thể thay đổi avatar bằng URL
  - Cache ảnh tự động khi nhập URL
- Tự động load avatar khi đăng nhập

## Cách chạy Migration:

### Nếu database đã tồn tại:

1. **Mở MySQL Command Line hoặc MySQL Workbench**

2. **Chạy script migration:**
```bash
mysql -u root -p < migration_add_avatar.sql
```

HOẶC trong MySQL Workbench/Command Line:
```sql
USE login_system;

ALTER TABLE users 
ADD COLUMN IF NOT EXISTS avatar_url VARCHAR(500) AFTER email;
```

3. **Compile lại project:**
```bash
cd common
javac -encoding UTF-8 -d target/classes src/main/java/com/loginSystem/common/*.java

cd ../server
javac -encoding UTF-8 -d target/classes -cp "target/classes;../common/target/classes;../libs/*" src/main/java/com/loginSystem/server/*.java

cd ../client
javac -encoding UTF-8 -d target/classes -cp "target/classes;../common/target/classes;../libs/*" src/main/java/com/loginSystem/client/*.java
```

### Nếu database mới:
- Không cần chạy migration
- Server sẽ tự động tạo bảng với cột avatar_url khi khởi động lần đầu

## Tính năng mới:

1. **Đăng ký tài khoản**: Có thể chọn avatar bằng URL
2. **Thông tin cá nhân**: 
   - Chế độ xem: Hiển thị thông tin và avatar (read-only)
   - Chế độ chỉnh sửa: Cho phép sửa thông tin và thay đổi avatar
3. **Avatar cache**: Tự động cache ảnh đã tải để tránh load lại
4. **Default avatar**: Nếu không có avatar, hiển thị avatar mặc định từ ui-avatars.com

## Định dạng URL Avatar hỗ trợ:
- URL trực tiếp đến file ảnh: `https://example.com/avatar.jpg`
- URL từ dịch vụ avatar: `https://ui-avatars.com/api/?name=John`
- Các định dạng ảnh: JPG, PNG, GIF, WebP

## Lưu ý:
- Avatar URL tối đa 500 ký tự
- Nên sử dụng URL HTTPS cho bảo mật
- Ảnh sẽ được resize về 120x120px khi hiển thị
- Cache được lưu trong RAM, sẽ mất khi đóng ứng dụng
