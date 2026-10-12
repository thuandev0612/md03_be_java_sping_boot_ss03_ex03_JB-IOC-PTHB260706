package com.example.baitapsession03.repository;

import com.example.baitapsession03.model.Course;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class CourseRepository {

    private final List<Course> courses = new ArrayList<>(List.of(
            new Course(1L, "Java Fundamentals", "ACTIVE", 1L),
            new Course(2L, "Spring Boot", "ACTIVE", 1L),
            new Course(3L, "Web Fundamentals", "DRAFT", 2L)
    ));

    // Tra ban sao danh sach de GET khong bi anh huong khi request khac them/xoa.
    public synchronized List<Course> findAll() {
        return new ArrayList<>(courses);
    }

    public synchronized Course findById(Long id) {
        for (Course course : courses) {
            if (course.getId().equals(id)) {
                return course;
            }
        }
        return null;
    }

    public synchronized Course create(Course course) {
        if (course.getId() == null) {
            long maxId = courses.stream().mapToLong(Course::getId).max().orElse(0L);
            if (maxId == Long.MAX_VALUE) {
                throw new IllegalArgumentException("Không thể cấp thêm ID tự động.");
            }
            course.setId(maxId + 1);
        } else if (course.getId() <= 0 || findById(course.getId()) != null) {
            throw new IllegalArgumentException("ID phải lớn hơn 0 và chưa tồn tại.");
        }

        courses.add(course);
        return course;
    }

    public synchronized Course update(Long id, Course course) {
        for (int i = 0; i < courses.size(); i++) {
            if (courses.get(i).getId().equals(id)) {
                // PUT thay the thong tin; ID tren URL xac dinh ban ghi can sua.
                course.setId(id);
                courses.set(i, course);
                return course;
            }
        }
        return null;
    }

    public synchronized Course deleteById(Long id) {
        for (int i = 0; i < courses.size(); i++) {
            if (courses.get(i).getId().equals(id)) {
                return courses.remove(i);
            }
        }
        return null;
    }
}
