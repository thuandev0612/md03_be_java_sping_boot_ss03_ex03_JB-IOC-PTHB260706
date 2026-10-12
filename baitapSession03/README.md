# Session 03 — Bài 1, 2 và 3: Phân tầng, CRUD và ApiResponse

Dự án dùng Java 21, Spring Boot 4.1.1, Gradle và Lombok. Dữ liệu nằm trong bộ nhớ, chưa sử dụng cơ sở dữ liệu.

## Cấu trúc dự án

```text
src/main/java/com/example/baitapsession03/
├── BaitapSession03Application.java
├── dto/
│   └── ApiResponse.java
├── model/
│   ├── Instructor.java
│   ├── Course.java
│   └── Enrollment.java
├── repository/
│   ├── InstructorRepository.java
│   ├── CourseRepository.java
│   └── EnrollmentRepository.java
├── service/
│   ├── InstructorService.java
│   ├── CourseService.java
│   └── EnrollmentService.java
└── controller/
    ├── ApiExceptionHandler.java
    ├── InstructorController.java
    ├── CourseController.java
    └── EnrollmentController.java
```

Mỗi nhánh xử lý theo luồng **Controller → Service → Repository**. Model mô tả dữ liệu được truyền qua các tầng.

| Tầng | Vai trò | Annotation |
| --- | --- | --- |
| Model | Chứa các trường dữ liệu | Lombok tạo constructor/getter/setter |
| Repository | Lưu dữ liệu trong bộ nhớ, thực hiện CRUD | `@Repository` |
| Service | Kiểm tra dữ liệu, quan hệ giữa các model và gọi repository | `@Service` |
| Controller | Nhận request, gọi service và trả `ResponseEntity<ApiResponse<...>>` | `@Controller` |

Các model đúng theo đề:

- `Instructor`: `Long id`, `String name`, `String email`.
- `Course`: `Long id`, `String title`, `String status`, `Long instructorId`.
- `Enrollment`: `Long id`, `String studentName`, `Long courseId`.

`status` đang là chuỗi, với dữ liệu mẫu `ACTIVE` và `DRAFT`. Model dùng `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`; không cần viết thêm constructor trùng chức năng.

## Constructor Injection và bean

Ví dụ trong `CourseService`:

```java
private final CourseRepository courseRepository;

@Autowired
public CourseService(CourseRepository courseRepository) {
    this.courseRepository = courseRepository;
}
```

Spring tạo bean `CourseRepository` và truyền nó vào constructor khi tạo `CourseService`. `CourseController` cũng nhận `CourseService` bằng constructor. Hai nhánh còn lại sử dụng cùng cách này. Không đặt `@Autowired` trên field và không tự `new` repository/service trong controller.

Trong bài CRUD, `CourseService` còn nhận `InstructorRepository` để kiểm tra giảng viên và `EnrollmentRepository` để kiểm tra lượt đăng ký trước khi xóa. `InstructorService` nhận thêm `CourseRepository`; `EnrollmentService` nhận thêm `CourseRepository`. Constructor trong ví dụ trên chỉ minh họa một dependency; constructor thực tế liệt kê tất cả dependency cần dùng.

Lớp `BaitapSession03Application` có `@SpringBootApplication`, nằm ở package cha `com.example.baitapsession03`. Nhờ vậy Spring quét được cả ba package chứa `@Controller`, `@Service`, `@Repository` bên dưới. Model là đối tượng dữ liệu, không cần đăng ký thành Spring bean.

## Quan hệ 1–n trong dữ liệu mẫu

```text
Instructor (1) ─── (n) Course (1) ─── (n) Enrollment
                   instructorId           courseId
```

`Course.instructorId` tham chiếu `Instructor.id`. `Enrollment.courseId` tham chiếu `Course.id`. Đây là liên kết bằng ID ở mức mô hình; bài này chưa dùng JPA hay khóa ngoại trong database.

| Model | Số bản ghi | Ví dụ thể hiện quan hệ |
| --- | --- | --- |
| Instructor | 2 | Giảng viên ID 1: Nguyen Van An; ID 2: Tran Thi Binh |
| Course | 3 | Java Fundamentals và Spring Boot cùng có `instructorId = 1` |
| Enrollment | 4 | Le Minh Chau và Pham Quang Duy cùng có `courseId = 1` |

Một giảng viên có thể dạy nhiều khóa học, nhưng mỗi khóa học ở mô hình này thuộc một giảng viên. Một khóa học có nhiều lượt đăng ký, nhưng mỗi lượt đăng ký thuộc một khóa học. Khi thêm dữ liệu mẫu, dùng ID giảng viên/khóa học đã có.

## Chạy trong IntelliJ

1. Chọn **File → Open**, mở thư mục `D:\Code\Springboot\baitapSession03` như một project Gradle.
2. Chọn JDK 21 cho Project SDK và Gradle JVM, chờ Gradle tải xong dependencies.
3. Chạy phương thức `main()` của `BaitapSession03Application`.
4. Mở một trong các địa chỉ sau:

| GET URL | Kết quả JSON |
| --- | --- |
| http://localhost:8080/instructors | 2 giảng viên |
| http://localhost:8080/courses | 3 khóa học |
| http://localhost:8080/enrollments | 4 lượt đăng ký |

Các controller giữ `@Controller` theo bài đầu. Tất cả phương thức API hiện trả `ResponseEntity` chứa `ApiResponse`, nên Spring xử lý HTTP body trực tiếp và không cần thêm `@ResponseBody`. `@RequestMapping` đặt đường dẫn chung cho từng loại tài nguyên. Các danh sách và đối tượng trong response hiện nằm bên trong trường `data`.

Nếu project Session 02 đang dùng cổng 8080, dừng ứng dụng đó trước; hoặc đặt `server.port=8081` trong `src/main/resources/application.properties` của project này và đổi URL sang cổng 8081. Đường dẫn `/` chưa có API, hãy dùng ba đường dẫn trong bảng.

## Các phương thức CRUD qua từng tầng

| Repository | Service (ví dụ Instructor) | Ý nghĩa |
| --- | --- | --- |
| `findAll()` | `getAllInstructors()` | Trả danh sách |
| `findById(id)` | `getInstructorById(id)` | Trả bản ghi hoặc `null` |
| `create(instructor)` | `createInstructor(instructor)` | Thêm và trả bản ghi đã lưu |
| `update(id, instructor)` | `updateInstructor(id, instructor)` | Thay thế thông tin, trả bản ghi sau sửa hoặc `null` |
| `deleteById(id)` | `deleteInstructorById(id)` | Xóa và trả bản ghi đã xóa hoặc `null` |

Course và Enrollment có các phương thức tương ứng: `getAllCourses()`, `getCourseById()`, `createCourse()`, `updateCourse()`, `deleteCourseById()`; và `getAllEnrollments()`, `getEnrollmentById()`, `createEnrollment()`, `updateEnrollment()`, `deleteEnrollmentById()`.

`findAll()` trả một bản sao danh sách. `update()` thay phần tử tại vị trí tìm được, giữ ID theo URL; không thêm bản ghi mới nếu ID không tồn tại. `deleteById()` dùng kết quả của `List.remove(index)` để trả về bản ghi vừa xóa. Các thao tác repository dùng `synchronized` để không sửa danh sách đồng thời trong cùng repository.

## Endpoint và HTTP status

Áp dụng bảng dưới cho ba đường dẫn tài nguyên: `/instructors`, `/courses`, `/enrollments`.

| Method | Đường dẫn ví dụ | Thành công | Lỗi chính |
| --- | --- | --- | --- |
| GET | `/courses` | **200**, mảng JSON (có thể là `[]`) | |
| GET | `/courses/{id}` | **200**, JSON khóa học | **404** nếu không có ID |
| POST | `/courses` | **201**, JSON bản ghi mới và header `Location` trỏ đến API chi tiết | **400** nếu dữ liệu sai |
| PUT | `/courses/{id}` | **200**, JSON sau cập nhật | **400** nếu dữ liệu sai; **404** nếu không có ID |
| DELETE | `/courses/{id}` | **200**, wrapper với bản ghi vừa xóa trong `data` | **400** nếu còn dữ liệu con; **404** nếu không có ID |

`@PathVariable("id")` lấy ID từ URL; `@RequestBody` nhận JSON cho POST/PUT. Các ID trên URL phải là số nguyên dương; giá trị sai trả 400.

Từ bài wrapper, xóa thành công chuyển từ 204 sang **200 OK** để có thể trả message và data. HTTP **204 No Content không có body**, nên không dùng 204 khi muốn trả wrapper. Service vẫn trả đối tượng bị xóa, controller chỉ đóng gói kết quả:

```java
Instructor deleted = instructorService.deleteInstructorById(id);
if (deleted == null) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.failure("Không tìm thấy giảng viên có ID " + id + "."));
}
return ResponseEntity.ok(ApiResponse.success("Xóa giảng viên thành công.", deleted));
```

## Quy tắc dữ liệu và ID

- Khi POST, có thể bỏ `id` hoặc gửi `id: null` để hệ thống tự cấp ID lớn hơn ID hiện có. Nếu gửi ID, ID phải dương và chưa tồn tại; ID trùng trả 400.
- PUT cập nhật đầy đủ các trường thông tin, không phải cập nhật từng phần như PATCH. Có thể bỏ `id` trong body; nếu có, nó phải trùng ID trên URL. ID không tồn tại trả 404 và không tạo mới.
- Instructor cần tên không trống và email đúng dạng cơ bản `ten@mien.com`.
- Course cần `title`, `status` không trống và `instructorId` tồn tại. `status` vẫn là chuỗi, chưa giới hạn danh sách giá trị.
- Enrollment cần `studentName` không trống và `courseId` tồn tại.
- Không xóa Instructor khi còn Course tham chiếu; không xóa Course khi còn Enrollment tham chiếu. Các trường hợp này trả 400; hãy chuyển liên kết bằng PUT hoặc xóa bản ghi con trước. Không tự động xóa dây chuyền.
- Request không hợp lệ không làm thay đổi dữ liệu đã lưu. Body rỗng, JSON hỏng hoặc ID sai kiểu được Spring trả 400.

Service/repository ném `IllegalArgumentException` khi vi phạm các quy tắc trên. `ApiExceptionHandler` dùng `@RestControllerAdvice` và `@ExceptionHandler` để chuyển lỗi này thành `ResponseEntity` có status 400, ví dụ:

```json
{
  "success": false,
  "message": "Giảng viên được tham chiếu không tồn tại.",
  "data": null
}
```

## Bài 3: Generic và wrapper ApiResponse

`dto/ApiResponse.java` là lớp định dạng response dùng chung, gồm ba trường:

```java
public class ApiResponse<T> {
    private final boolean success;
    private final String message;
    private final T data;
}
```

`T` là kiểu dữ liệu thay đổi theo API, ví dụ:

| Kiểu Java | Nội dung `data` |
| --- | --- |
| `ApiResponse<Course>` | Một khóa học |
| `ApiResponse<List<Course>>` | Danh sách khóa học |
| `ApiResponse<Instructor>` | Một giảng viên |
| `ApiResponse<List<Enrollment>>` | Danh sách lượt đăng ký |
| `ApiResponse<Void>` | Không có dữ liệu, dùng `null` cho lỗi |

Lombok `@Getter` tạo getter để JSON serializer đọc các trường; `@AllArgsConstructor` tạo constructor nhận `success`, `message`, `data`. `@JsonInclude(ALWAYS)` giữ trường `data` trong JSON kể cả khi giá trị là `null`.

Hai phương thức tiện ích:

```java
ApiResponse.success("Lấy thông tin khóa học thành công.", course);
ApiResponse.failure("Không tìm thấy khóa học.");
```

Phương thức đầu tạo `success = true` cùng dữ liệu, phương thức sau tạo `success = false` và `data = null`. Đây chỉ là cách đóng gói response, không kiểm tra nghiệp vụ.

Ví dụ controller lấy danh sách:

```java
@GetMapping
public ResponseEntity<ApiResponse<List<Course>>> getAllCourses() {
    return ResponseEntity.ok(ApiResponse.success(
            "Lấy danh sách khóa học thành công.", courseService.getAllCourses()));
}
```

`ResponseEntity` quyết định HTTP status/header; `ApiResponse` quyết định cấu trúc JSON body. Controller gọi service và chuyển kết quả thành HTTP response. Toàn bộ logic CRUD, validation và kiểm tra liên kết vẫn nằm trong service/repository; các file ở hai tầng này giữ nguyên từ bài 2.

GET `/courses/1` trả **200** với cấu trúc:

```json
{
  "success": true,
  "message": "Lấy thông tin khóa học thành công.",
  "data": {
    "id": 1,
    "title": "Java Fundamentals",
    "status": "ACTIVE",
    "instructorId": 1
  }
}
```

GET danh sách đặt mảng trong `data`; nếu danh sách rỗng thì `data: []` và `success: true`. POST vẫn trả **201** và header Location; PUT/DELETE thành công trả **200**. ID không tồn tại trả **404** với `success: false`, message giải thích và `data: null`.

`ApiExceptionHandler` cũng chuẩn hóa lỗi validation 400, JSON hỏng, ID sai kiểu, đường dẫn không tồn tại (404), HTTP method không hỗ trợ (405), Content-Type không hỗ trợ (415) và lỗi hệ thống (500). Lỗi hệ thống ghi chi tiết vào log, còn response trả thông báo chung. HTTP status không bị đổi thành 200 chỉ vì đã có trường `success`.

JSON gửi lên qua POST/PUT giữ nguyên như bài 2; **chỉ JSON trả về được bọc lại**. Trong Postman hoặc frontend, đọc `response.data.id` thay vì `response.id`.

## Thử CRUD bằng Postman

Chạy lại ứng dụng sau khi cập nhật code. Với POST/PUT, chọn **Body → raw → JSON**, header `Content-Type: application/json`.

**Instructor — POST `http://localhost:8080/instructors`:**

```json
{
  "name": "Le Thi Mai",
  "email": "mai@example.com"
}
```

**Course — POST `http://localhost:8080/courses`:**

```json
{
  "title": "Spring MVC",
  "status": "DRAFT",
  "instructorId": 1
}
```

**Enrollment — POST `http://localhost:8080/enrollments`:**

```json
{
  "studentName": "Nguyen Minh Nam",
  "courseId": 1
}
```

Mỗi POST thành công trả 201. Lấy `data.id` trong response để thử tiếp:

1. GET `/{loai-tai-nguyen}/{id}` → 200 và bản ghi vừa tạo.
2. PUT cùng URL với JSON đầy đủ như trên nhưng đổi tên/tiêu đề/trạng thái → 200 và dữ liệu đã đổi.
3. GET lại URL chi tiết để xác nhận dữ liệu đã được lưu.
4. DELETE cùng URL → 200, wrapper có thông báo và bản ghi đã xóa (nếu chưa có dữ liệu con tham chiếu).
5. GET lại ID vừa xóa → 404.

Ví dụ cụ thể: nếu POST Enrollment trả `data.id = 5`, sử dụng GET/PUT/DELETE `http://localhost:8080/enrollments/5`. Không mặc định ID luôn là 5 sau nhiều lần thử; xem ID thực tế trong response.

Để thử lỗi 400, đổi `instructorId` hoặc `courseId` thành `999999`, hoặc để tên rỗng. Để thử lỗi 404, GET `/courses/999999` khi ID đó chưa được tạo.

## Chạy bằng PowerShell và kiểm tra

Mở terminal ở thư mục project:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-21'
.\gradlew.bat bootRun
```

Kiểm tra và đóng gói JAR:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-21'
.\gradlew.bat test bootJar
```

Test khởi động ứng dụng trên một cổng trống và gọi HTTP thật tới cả ba controller. Test ban đầu kiểm tra dữ liệu mẫu, bean và quan hệ 1–n. `CrudApiTests` kiểm tra đủ vòng CRUD cho ba đối tượng; các mã 200/201/400/404; header Location; ID tự cấp, ID trùng; validation khi tạo/sửa; không đổi dữ liệu khi lỗi; bảo vệ liên kết và danh sách rỗng sau khi xóa. Mọi response trong các test đều được kiểm tra có đúng ba trường `success`, `message`, `data`.

`ApiErrorResponseTests` kiểm tra wrapper cho lỗi URL, HTTP method, Content-Type và lỗi hệ thống giả lập trong test. Bean service giả lập này chỉ tồn tại trong lớp test tương ứng; không thay đổi service thực của ứng dụng.

Khi ứng dụng khởi động lại, repository tạo lại dữ liệu mẫu. Các thay đổi CRUD chỉ nằm trong bộ nhớ, chưa lưu bền vững vào cơ sở dữ liệu. Test sử dụng ứng dụng riêng trên cổng ngẫu nhiên, không thao tác vào phiên bạn đang chạy trong IntelliJ.
