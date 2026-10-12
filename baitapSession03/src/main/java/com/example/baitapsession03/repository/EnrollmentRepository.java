package com.example.baitapsession03.repository;

import com.example.baitapsession03.model.Enrollment;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class EnrollmentRepository {

    private final List<Enrollment> enrollments = new ArrayList<>(List.of(
            new Enrollment(1L, "Le Minh Chau", 1L),
            new Enrollment(2L, "Pham Quang Duy", 1L),
            new Enrollment(3L, "Hoang Ngoc Ha", 2L),
            new Enrollment(4L, "Do Tuan Kiet", 3L)
    ));

    // Tra ban sao danh sach de GET khong bi anh huong khi request khac them/xoa.
    public synchronized List<Enrollment> findAll() {
        return new ArrayList<>(enrollments);
    }

    public synchronized Enrollment findById(Long id) {
        for (Enrollment enrollment : enrollments) {
            if (enrollment.getId().equals(id)) {
                return enrollment;
            }
        }
        return null;
    }

    public synchronized Enrollment create(Enrollment enrollment) {
        if (enrollment.getId() == null) {
            long maxId = enrollments.stream().mapToLong(Enrollment::getId).max().orElse(0L);
            if (maxId == Long.MAX_VALUE) {
                throw new IllegalArgumentException("Không thể cấp thêm ID tự động.");
            }
            enrollment.setId(maxId + 1);
        } else if (enrollment.getId() <= 0 || findById(enrollment.getId()) != null) {
            throw new IllegalArgumentException("ID phải lớn hơn 0 và chưa tồn tại.");
        }

        enrollments.add(enrollment);
        return enrollment;
    }

    public synchronized Enrollment update(Long id, Enrollment enrollment) {
        for (int i = 0; i < enrollments.size(); i++) {
            if (enrollments.get(i).getId().equals(id)) {
                // PUT thay the thong tin; ID tren URL xac dinh ban ghi can sua.
                enrollment.setId(id);
                enrollments.set(i, enrollment);
                return enrollment;
            }
        }
        return null;
    }

    public synchronized Enrollment deleteById(Long id) {
        for (int i = 0; i < enrollments.size(); i++) {
            if (enrollments.get(i).getId().equals(id)) {
                return enrollments.remove(i);
            }
        }
        return null;
    }
}
