package com.example.demo.repository;

import com.example.demo.entity.Enrollment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

	boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);

	@Query("""
		select e.course.id
		from Enrollment e
		where e.student.id = :studentId
		""")
	List<Long> findEnrolledCourseIdsByStudentId(@Param("studentId") Long studentId);

	@Query("""
		select e
		from Enrollment e
		join fetch e.course c
		where e.student.id = :studentId
		order by e.enrollDate desc, e.id desc
		""")
	List<Enrollment> findMyEnrollments(@Param("studentId") Long studentId);
}
