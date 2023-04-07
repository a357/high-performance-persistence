package com.jpahiber.demojpahiber.hibernate.many2many;

import com.jpahiber.demojpahiber.config.utils.DbTest;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.junit.Test;

import java.util.*;

public class BidirectionalManyToManySetIT extends DbTest {


    @Test
    public void removeSubject() {
        Student student1 = new Student(1L, "John Doe");
        Student student2 = new Student(2L, "Jane Doe");

        Subject subject1 = new Subject("Maths");
        Subject subject2 = new Subject("English");
        Subject subject3 = new Subject("Science");

        student1.addSubject(subject1);
        student1.addSubject(subject2);
        student1.addSubject(subject3);

        student2.addSubject(subject3);

        doInJPA(em -> {
            em.persist(student1);
            em.persist(student2);
        });


        /**
         * remove is tricky because of the order of the list,
         * and depends on @OrderColumn(name = "subject_order") in the Student class: update instead of an insert
         * type of Collection List or Set: insert/update vs delete
         * */
        doInJPA(em -> {
            Student student = em.find(Student.class, 1L);
            student.removeSubject(student.getSubjects().iterator().next());
            em.persist(student);
        });
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

        /**
         * cascading makes sense only for parent side to child side, that's why orphanRemoval = true and
         * cascade = {CascadeType.REMOVE} not desirable because the both sides are parent
         */
        @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
        @JoinTable(name = "student_subject",
                joinColumns = @JoinColumn(name = "student_id"),
                inverseJoinColumns = @JoinColumn(name = "subject_id"))
        Set<Subject> subjects = new HashSet<>();


        public void addSubject(Subject subject) {
            subjects.add(subject);
            subject.students.add(this);
        }

        public void removeSubject(Subject subject) {
            subjects.remove(subject);
            subject.students.remove(this);
        }
    }

    @Data
    @NoArgsConstructor
    @RequiredArgsConstructor
    @Entity
    @Table(name = "subject")
    public static class Subject {
        @Id
        @GeneratedValue
        private Long id;

        @NonNull
        private String name;

        @ManyToMany(mappedBy = "subjects")
        private Set<Student> students = new HashSet<>();

        @Override
        public int hashCode() {
            return this.getClass().hashCode();
        }
    }




    @Override
    protected Class<?>[] entities() {
        return new Class[]{Student.class, Subject.class};
    }
}
