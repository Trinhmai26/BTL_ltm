
<h2 align="center">
    <a href="https://dainam.edu.vn/vi/khoa-cong-nghe-thong-tin">
    🎓 Faculty of Information Technology (DaiNam University)
    </a>
</h2>


<div align="center">
    <p align="center">
        <img width="170" alt="aiotlab_logo" src="https://github.com/user-attachments/assets/41ef702b-3d6e-4ac4-beac-d8c9a874bca9" />
        <img width="180" alt="fitdnu_logo" src="https://github.com/user-attachments/assets/ec4815af-e477-480b-b9fa-c490b74772b8" />
        <img width="200" alt="dnu_logo" src="https://github.com/user-attachments/assets/2bcb1a6c-774c-4e7d-b14d-8c53dbb4067f" />
    </p>

[![AIoTLab](https://img.shields.io/badge/AIoTLab-green?style=for-the-badge)](https://www.facebook.com/DNUAIoTLab)
[![Faculty of Information Technology](https://img.shields.io/badge/Faculty%20of%20Information%20Technology-blue?style=for-the-badge)](https://dainam.edu.vn/vi/khoa-cong-nghe-thong-tin)
[![DaiNam University](https://img.shields.io/badge/DaiNam%20University-orange?style=for-the-badge)](https://dainam.edu.vn)

</div>
<h1 align="center">HỆ THỐNG ĐĂNG NHẬP CLIENT SERVER </h1>
</div>

## 📖 1. Giới thiệu


### 📖 Giới thiệu đề tài

Trong thời đại công nghệ thông tin phát triển mạnh mẽ, việc xây dựng các ứng dụng mạng an toàn và hiệu quả ngày càng trở nên quan trọng. Một trong những chức năng cơ bản và phổ biến nhất trong các hệ thống phần mềm chính là **chức năng đăng nhập (Login System)**, nhằm xác thực và quản lý người dùng.

Đề tài **“Hệ thống đăng nhập Client–Server bằng giao thức TCP ngôn ngữ Java”** được xây dựng với mục tiêu mô phỏng quá trình giao tiếp giữa **client** và **server** thông qua giao thức **TCP/IP**. Trong đó:

* **Server** có nhiệm vụ quản lý kết nối, xử lý yêu cầu và xác thực tài khoản người dùng.
* **Client** đóng vai trò gửi thông tin đăng nhập (username, password) đến server để kiểm tra.
* **Cơ sở dữ liệu MySQL** được sử dụng để lưu trữ thông tin tài khoản, đảm bảo khả năng quản lý dữ liệu tập trung, an toàn và dễ mở rộng.

Hệ thống hỗ trợ **đa luồng**, cho phép nhiều client kết nối và làm việc đồng thời mà không ảnh hưởng đến hiệu năng. Đây là một ứng dụng thực tiễn giúp sinh viên nắm vững kiến thức về:

* lập trình mạng trong Java,
* giao thức TCP,
* cơ chế đa luồng (multithreading),
* và kỹ thuật kết nối cơ sở dữ liệu (JDBC – MySQL).


Trong kiến trúc này:  

- **Server**: Chịu trách nhiệm xử lý logic nghiệp vụ, quản lý cơ sở dữ liệu và duy trì tính bảo mật.  
- **Client**: Cung cấp giao diện người dùng trực quan, hỗ trợ thao tác dễ dàng và thuận tiện.  

### 📊 Mục tiêu của đề tài

- Xây dựng hệ thống đăng nhập client–server bằng Java sử dụng giao thức TCP/IP.
- Cho phép nhiều client kết nối đồng thời, gửi thông tin đăng nhập tới server.
- Server xử lý, kiểm tra dữ liệu trong cơ sở dữ liệu và phản hồi kết quả.
- Củng cố kiến thức về lập trình mạng, đa luồng và kết nối cơ sở dữ liệu trong Java.
- Tạo nền tảng để mở rộng vớBoadcast
## 🔧 2. Công nghệ sử dụng

### 🌐 Ngôn Ngữ Lập Trình
- **Java SE 17+**: Ngôn ngữ lập trình chính  
- Hỗ trợ lập trình hướng đối tượng, đa luồng, lập trình socket  

### 🎨 Giao Diện Người Dùng
- **Java Swing**: Xây dựng giao diện desktop  
- Các thành phần chính: `JFrame`, `JPanel`, `JButton`, `JTextField`, `JPasswordField`, `JTable`  
- Xử lý sự kiện: `ActionListener`, `MouseListener`  

### 🗄️ Lưu Trữ Dữ Liệu
- Sử dụng My SQL
- Chứa thông tin tài khoản (username, password, role, …)  
- Thao tác: đọc, ghi, cập nhật, xóa tài khoản bằng Java I/O  

### 🔄 Xử Lý Đa Luồng
- **Java Multithreading**: Cho phép nhiều client kết nối đồng thời  
- Thread riêng cho từng client để tránh xung đột  
- Đồng bộ hóa khi ghi/đọc dữ liệu từ MySQL

## 🖼️ 3. Hình ảnh chức năng
<p align="center">
 <img src="https://github.com/user-attachments/assets/393881c9-8b2c-4084-aaa4-b1c0d9e188b0" />

</p>

<p align="center">
  <em>Hình 1: Giao diện Đăng nhập</em>

##  3.1. giao diện Admin
<p align="center">
<img src="https://github.com/user-attachments/assets/532626c2-babf-47c5-9e3e-89c6ce62f2d3" />
</p>
<p align="center">
  <em>Hình 2: Giao diện Dashboard</em>
  <p align="center">
<img  src="https://github.com/user-attachments/assets/8786d425-775b-4b0a-8ec8-7a0d6bc6081c" />  
</p>
<p align="center">
  <em>Hình 3: Giao diện User</em>
   <p align="center">
       <img src="https://github.com/user-attachments/assets/ab47db01-6bea-4226-b79d-514d1e1a2d37" />
</p>
<p align="center">
  <em>Hình 4: Giao diện Login story</em>
       <p align="center">
       <img src="https://github.com/user-attachments/assets/77964eef-3a3e-426a-905c-b0fb24fd5604" />
</p>
<p align="center">
  <em>Hình 5: Giao diện Đăng kí</em>
      <p align="center">
       <img src="https://github.com/user-attachments/assets/77964eef-3a3e-426a-905c-b0fb24fd5604" />
</p>
<p align="center">
  <em>Hình 6: Giao diện Đăng kí</em>
          <p align="center">
       <img src="https://github.com/user-attachments/assets/a7065392-d518-4919-80f6-e726b5b00b86" />
</p>
<p align="center">
  <em>Hình 7: Giao diện Thông tin cá nhân </em>
            <p align="center">
       <img  src="https://github.com/user-attachments/assets/d4eebd78-d1c0-4ed6-a429-7d5d1446a249" />

</p>
<p align="center">
  <em>Hình 7: Giao diện Lịch sử đăng nhập </em>
              <p align="center">
       <img  rc="https://github.com/user-attachments/assets/3b3cc7f0-4361-46c7-b160-ab3b2fc21e0b" />

</p>
<p align="center">
  <em>Hình 8: Giao diện hỗ trợ </em>
## ⚙️ 4. Các bước cài đặt


### 🔹 Bước 1: Chuẩn bị môi trường  
- Cài đặt **Java Development Kit (JDK 8 trở lên)**  
  - Tải tại: [Oracle JDK](https://www.oracle.com/java/technologies/javase-downloads.html) hoặc [OpenJDK](https://jdk.java.net/)  
  - Kiểm tra cài đặt:  
    ```bash
    java -version
    javac -version
    ```  

- Cài đặt một IDE hỗ trợ Java (khuyến nghị):  
  - [IntelliJ IDEA](https://www.jetbrains.com/idea/)  
  - [Eclipse](https://www.eclipse.org/)  
  - [NetBeans](https://netbeans.apache.org/)  

- Chuẩn bị file dữ liệu `users.csv` để lưu thông tin tài khoản.  

---

### 🔹 Bước 2:Lưu Trữ Dữ Liệu Qua MySQL  
- Khởi tạo CSDL: Tạo cơ sở dữ liệu MySQL để quản lý thông tin đăng nhập.
- Thiết kế bảng: Xây dựng bảng users với các cột: username, password, email, fullname, role, status, createdAt…
- Lưu trữ dữ liệu: Mỗi tài khoản là một bản ghi trong bảng.
- Kết nối ứng dụng: Java kết nối MySQL qua JDBC để thao tác dữ liệu.
- Thao tác chính (CRUD): Thêm, đọc, cập nhật, xóa tài khoản.
- Bảo mật: Mật khẩu được mã hóa, hạn chế truy cập trực tiếp vào CSDL.
### 🔹 Bước 3:Biên dịch source
Mở terminal tại thư mục dự án, gõ lệnh:
```
bash
Sao chép mã
javac BTL/*.java
👉 Lệnh trên sẽ biên dịch toàn bộ source code trong package hi.
```
### 🔹 Bước 4:Chạy hệ thống
```
    Chạy ServerMain
```
```
    Chạy ClientApp
```
### 🔹 Bước 5:Kiểm Thử
Kết nối: kiểm tra client có kết nối được với server qua TCP.

Đăng nhập: thử với tài khoản đúng, sai, hoặc không tồn tại.

CSDL MySQL: kiểm tra thêm, sửa, xóa tài khoản và trạng thái online/offline.

Đa luồng: nhiều client đăng nhập cùng lúc không bị xung đột.

Phân quyền: admin và user có quyền khác nhau.

Bảo mật: mật khẩu mã hóa, hạn chế truy cập trái phép.

Hiệu năng: đo thời gian phản hồi và độ ổn định khi tải cao.

## 📞5. Liên hệ
Nếu bạn có bất kỳ thắc mắc hoặc cần hỗ trợ về dự án **Hệ Thống Đăng Nhập Client-Server**, vui lòng liên hệ:  

- 👨‍🎓 **Sinh viên thực hiện**: Trịnh Thị Yến Mai 
- 🎓 **Khoa**: Công nghệ thông tin – Đại học Đại Nam  
- 📧 **Email**: Trinhyenmai26@.com





  






  



