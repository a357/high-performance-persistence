package com.jpahiber.demojpahiber.hibernate.many2many;

import com.jpahiber.demojpahiber.config.utils.DbTest;
import jakarta.persistence.*;
import lombok.Data;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class UnidirectionalManyToManyIT extends DbTest {


    @Test
    public void saveStudent() {
        Student student = new Student();
        student.setId(1L);
        student.setName("John Doe");

        Subject subject = new Subject();
        subject.setName("Maths");

        student.addSubject(subject);

        doInJPA(em -> em.persist(student));

        doInJPA(em -> em.find(Student.class, 1L));

        doInJPA(em -> {
            Student st = em.find(Student.class, 1L);
            st.removeSubject(st.getSubjects().get(0));
            em.persist(st);
        });
    }



    @Data
    @Entity
    @Table(name = "student")
    public static class Student {
        @Id
        private Long id;
        private String name;

        /**
         * cascading makes sense only for parent side to child side, that's why orphanRemoval = true and
         * cascade = {CascadeType.REMOVE} not desirable because the both sides are parent
         */
        @ManyToMany(cascade = {CascadeType.ALL})
        @JoinTable(name = "student_subject",
                joinColumns = @JoinColumn(name = "student_id"),
                inverseJoinColumns = @JoinColumn(name = "subject_id"))
        List<Subject> subjects = new ArrayList<>();


        public void addSubject(Subject subject) {
            subjects.add(subject);
        }

        public void removeSubject(Subject subject) {
            subjects.remove(subject);
        }
    }

    @Data
    @Entity
    @Table(name = "subject")
    public static class Subject {
        @Id
        @GeneratedValue
        private Long id;
        private String name;
    }

    @Override
    protected Class<?>[] entities() {
        return new Class[]{Student.class, Subject.class};
    }
}
