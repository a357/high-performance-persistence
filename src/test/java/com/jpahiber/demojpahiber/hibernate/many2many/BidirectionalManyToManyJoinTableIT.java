package com.jpahiber.demojpahiber.hibernate.many2many;

import com.jpahiber.demojpahiber.config.utils.DbTest;
import jakarta.persistence.*;
import lombok.*;
import org.junit.Test;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class BidirectionalManyToManyJoinTableIT extends DbTest {


    @Override
    protected void afterInit() {
        doInJPA(em -> {
            Student student1 = new Student(1L, "John Doe");
            Student student2 = new Student(2L, "Jane Doe");

            Subject subject1 = new Subject(1L, "Maths");
            Subject subject2 = new Subject(2L, "English");
            Subject subject3 = new Subject(3L, "Science");


            student1.addSubject(subject1);
            student1.addSubject(subject2);
            student1.addSubject(subject3);

            student2.addSubject(subject3);

            em.persist(student1);
            em.persist(student2);
        });
    }

    @Test
    public void removeSubjectFromHead() {
        doInJPA(em -> {
            Student student = em.find(Student.class, 1L);
            student.removeSubject(student.getSubjects().get(0).getSubject());
            em.persist(student);
        });
    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Embeddable
    public static class StudentSubjectId {
        @NonNull
        private Long st;
        @NonNull
        private Long sb;
    }

    @Data
    @NoArgsConstructor
    @Entity(name = "student_subject")
    public static class StudentSubject {

        public StudentSubject(Student student, Subject subject) {
            this.student = student;
            this.subject = subject;
            this.id = new StudentSubjectId(student.getId(), subject.getId());
        }

        @EmbeddedId
        private StudentSubjectId id;

        @ManyToOne
        @MapsId("st")
        private Student student;

        @ManyToOne
        @MapsId("sb")
        private Subject subject;
    }

    @Data
    @NoArgsConstructor
    @RequiredArgsConstructor
    @Entity
    @Table(name = "student")
    public static class Student {
        @NonNull
        @Id
        private Long id;

        @NonNull
        private String name;

        @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<StudentSubject> subjects = new LinkedList<>();

        public void removeSubject(Subject subject) {
            for(Iterator<StudentSubject> iterator = subjects.iterator(); iterator.hasNext(); ) {
                StudentSubject ss = iterator.next();
                if (Objects.equals(this, ss.getStudent()) && Objects.equals(subject, ss.getSubject())) {
                    iterator.remove();
                    ss.getSubject().getStudents().remove(ss);
                    ss.setStudent(null);
                    ss.setSubject(null);
                    break;
                }
            }
        }

        public void addSubject(Subject subject) {
            StudentSubject ss = new StudentSubject(this, subject);
            subjects.add(ss);
            subject.getStudents().add(ss);
        }

    }

    @Data
    @NoArgsConstructor
    @RequiredArgsConstructor
    @Entity
    @Table(name = "subject")
    public static class Subject {
        @NonNull
        @Id
        private Long id;

        @NonNull
        private String name;

        @OneToMany(mappedBy = "subject", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<StudentSubject> students = new LinkedList<>();


        public void removeStudent(Student student) {
            for (Iterator<StudentSubject> iterator = students.iterator(); iterator.hasNext();) {
                StudentSubject ss = iterator.next();
                if (Objects.equals(this, ss.getSubject()) && Objects.equals(student, ss.getStudent())) {
                    iterator.remove();
                    ss.getSubject().getStudents().remove(ss);
                    ss.setSubject(null);
                    ss.setStudent(null);
                    break;
                }
            }
        }

        public void addStudent(Student student) {
            StudentSubject ss = new StudentSubject(student, this);
            students.add(ss);
            student.getSubjects().add(ss);
        }
    }

    @Override
    protected Class<?>[] entities() {
        return new Class[]{StudentSubject.class, Student.class, Subject.class};
    }

}
