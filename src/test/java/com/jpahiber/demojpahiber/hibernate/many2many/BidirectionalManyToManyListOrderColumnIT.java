package com.jpahiber.demojpahiber.hibernate.many2many;

import com.jpahiber.demojpahiber.config.utils.DbTest;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

public class BidirectionalManyToManyListOrderColumnIT extends DbTest {

    @Override
    protected void afterInit() {
        doInJPA(em->{
            Student student1 = new Student(1L, "John Doe");
            Student student2 = new Student(2L, "Jane Doe");

            Subject subject1 = new Subject("Maths");
            Subject subject2 = new Subject("English");
            Subject subject3 = new Subject("Science");

            student1.addSubject(subject1);
            student1.addSubject(subject2);
            student1.addSubject(subject3);

            student2.addSubject(subject3);

            /**
             * Create students
             *
             * Query:["insert into student (id, name) values (?, ?)"], Params:[(1, John Doe)]
             * Query:["insert into student (id, name) values (?, ?)"], Params:[(2, Jane Doe)]
             * Query:["insert into subject (name, id) values (?, ?)"], Params:[(Maths, 1)]
             * Query:["insert into subject (name, id) values (?, ?)"], Params:[(English, 2)]
             * Query:["insert into subject (name, id) values (?, ?)"], Params:[(Science, 3)]
             * Query:["insert into student_subject (student_id, subject_id) values (?, ?)"], Params:[(1, 1)]
             * Query:["insert into student_subject (student_id, subject_id) values (?, ?)"], Params:[(1, 2)]
             * Query:["insert into student_subject (student_id, subject_id) values (?, ?)"], Params:[(1, 3)]
             * Query:["insert into student_subject (student_id, subject_id) values (?, ?)"], Params:[(2, 3)]
             */
            em.persist(student1);
            em.persist(student2);
        });
    }


    /**
     * remove is tricky because of the order of the list,
     * and depends on @OrderColumn(name = "subject_order") in the Student class: update instead of an insert
     * type of Collection List or Set: insert/update vs delete
     * */
    @Test
    public void removeSubjectFromHead() {
        doInJPA(em -> {
            Student student = em.find(Student.class, 1L);
            student.removeSubject(student.getSubjects().get(0));
            em.persist(student);
        });
    }

    @Test
    public void removeSubjectFromTail() {
        doInJPA(em -> {
            Student student = em.find(Student.class, 1L);
            student.removeSubject(student.getSubjects().get(student.getSubjects().size()-1));
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
        @OrderColumn(name = "subject_order")
        List<Subject> subjects = new LinkedList<>();


        public void addSubject(Subject subject) {
            subjects.add(subject);
            subject.students.add(this);
        }

        public void removeSubject(Subject subject) {
            subjects.remove(subject);
            subject.students.remove(this);
        }

        @Override
        public int hashCode() {
            return this.getClass().hashCode();
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
        private List<Student> students = new LinkedList<>();

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
