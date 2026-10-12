package com.example.baitapsession03.controller;

import com.example.baitapsession03.dto.ApiResponse;
import com.example.baitapsession03.model.Course;
import com.example.baitapsession03.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.net.URI;
import java.util.List;

@Controller
@RequestMapping("/courses")
public class CourseController {

    private final CourseService courseService;

    @Autowired
    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Course>>> getAllCourses() {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách khóa học thành công.", courseService.getAllCourses()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Course>> getCourseById(@PathVariable("id") Long id) {
        Course course = courseService.getCourseById(id);
        if (course == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.failure("Không tìm thấy khóa học có ID " + id + "."));
        }
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin khóa học thành công.", course));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Course>> createCourse(@RequestBody Course course) {
        Course created = courseService.createCourse(course);
        return ResponseEntity.created(URI.create("/courses/" + created.getId()))
                .body(ApiResponse.success("Tạo khóa học thành công.", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Course>> updateCourse(
            @PathVariable("id") Long id, @RequestBody Course course) {
        Course updated = courseService.updateCourse(id, course);
        if (updated == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.failure("Không tìm thấy khóa học có ID " + id + "."));
        }
        return ResponseEntity.ok(ApiResponse.success("Cập nhật khóa học thành công.", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Course>> deleteCourseById(@PathVariable("id") Long id) {
        Course deleted = courseService.deleteCourseById(id);
        if (deleted == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.failure("Không tìm thấy khóa học có ID " + id + "."));
        }
        // Dung 200 de co body wrapper; 204 khong duoc co body.
        return ResponseEntity.ok(ApiResponse.success("Xóa khóa học thành công.", deleted));
    }
}
