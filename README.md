# 🧩 Hibernate Demo

Dự án **Hibernate Demo** minh họa cách sử dụng **Hibernate ORM** trong Java để ánh xạ (mapping) các lớp đối tượng sang bảng trong cơ sở dữ liệu, thực hiện các thao tác CRUD (Create, Read, Update, Delete).

---

## 🧠 Giới thiệu

Hibernate là framework ORM (Object Relational Mapping) mạnh mẽ trong Java, giúp:

- Tự động ánh xạ class Java thành bảng trong database.  
- Giảm việc viết SQL thủ công.  
- Dễ dàng thực hiện CRUD và quản lý transaction.  

Dự án này là ví dụ đơn giản để sinh viên hoặc người mới học có thể hiểu cách cấu hình Hibernate và chạy thử một ứng dụng Java kết nối cơ sở dữ liệu.

---

## 🚀 Tính năng

- Cấu hình Hibernate bằng file `hibernate.cfg.xml`.  
- Tạo entity (`Product`) và ánh xạ đến bảng trong DB.  
- Tự động tạo bảng nếu chưa có (với `hbm2ddl.auto=update`).  
- Thực hiện các thao tác CRUD cơ bản qua `Session`.  

---

## 🧩 Yêu cầu hệ thống

| Thành phần | Phiên bản khuyến nghị |
|-------------|-----------------------|
| Java JDK    | 8 hoặc cao hơn        |
| IDE         | IntelliJ IDEA / Eclipse / VSCode |
| Build Tool  | Maven                 |
| Database    | MySQL (hoặc H2, PostgreSQL, tùy chỉnh được) |
| Hibernate   | Phiên bản 6.x trở lên |

---

## ⚙️ Cài đặt & chạy demo

### 🔹 Bước 1. Clone repository
```bash
git clone https://github.com/PiraSama/hibernate-demo.git
cd hibernate-demo

### 🔹 Bước 2. Cấu hình cơ sở dữ liệu
-Tạo cơ sở dữ liệu trống trong MySQL
CREATE DATABASE multilang_product CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-Mở file src/main/resources/hibernate.cfg.xml và cập nhật thông tin kết nối:

<property name="hibernate.connection.url">jdbc:mysql://localhost:3306/hibernate_demo</property>
<property name="hibernate.connection.username">root</property>
<property name="hibernate.connection.password">your_password</property>
<property name="hibernate.dialect">org.hibernate.dialect.MySQL8Dialect</property>
<property name="hibernate.hbm2ddl.auto">update</property>
<property name="hibernate.show_sql">true</property>

### 🔹 Bước 3. Cài đặt dependency
- Dự án sử dụng Maven. Để tải và cài đặt các thư viện cần thiết, chạy:

              mvn clean install

Hoặc build từ IDE (chuột phải vào project → Maven → Reload project).

### 🔹 Bước 4. Chạy chương trình

Chạy MainApp trước để tạo dữ liệu mẫu (Lưu ý chạy 1 lần)


Sau đó, Chạy ProductUI để hiện ra giao diện

---
## 📂 Cấu trúc dự án
org.example.multilangproduct
│
├── config/                  → Cấu hình Hibernate
│   └── HibernateUtil.java
│
├── entity/                  → Các entity ánh xạ CSDL
│   ├── Product.java
│   ├── ProductTranslation.java
│   ├── ProductCategory.java
│   ├── Language.java
│
├── dao/                     → Tầng truy cập dữ liệu
│   ├── ProductDAO.java
│   ├── LanguageDAO.java
│   ├── ProductCategoryDAO.java
│
├── service/                 → Tầng xử lý nghiệp vụ
│   └── ProductService.java
│
├── dto/                     → Dữ liệu truyền ra UI
│   └── ProductDisplayDTO.java
│
├── ui/                      → Giao diện console demo
│   └── ProductUI.java
│
└── MainApp.java             → Điểm chạy chương trình

---
##🤝 Góp phần & Issue
Nếu bạn phát hiện lỗi hoặc muốn đóng góp thêm ví dụ, hãy:

 Fork repository này

Tạo branch mới: git checkout -b feature/my-feature

Commit thay đổi: git commit -m "Add new feature"

Push lên branch: git push origin feature/my-feature

Tạo Pull Request

