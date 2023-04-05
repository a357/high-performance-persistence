package com.jpahiber.demojpahiber.hibernate.one2many;

import com.jpahiber.demojpahiber.hibernate.utils.DbTest;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

@Slf4j
public class UnidirectionalOneToManyListJoinColumn extends DbTest {

    @Override
    protected void afterInit() {
        doInJPA(em -> {

            Review review1 = new Review();
            review1.setComment("Bad");

            Review review2 = new Review();
            review2.setComment("Good");

            Review review3 = new Review();
            review3.setComment("Ugly");


            Customer customer = new Customer();
            customer.setId(1L);
            customer.setName("John Doe");
            customer.getReviews().addAll(List.of(review1, review2, review3));

            /**
             persist customer

             Query:["insert into customer (name, id) values (?, ?)"], Params:[(John Doe, 1)]
             Query:["insert into review (comment, id) values (?, ?)"], Params:[(Bad, 1)]
             Query:["insert into review (comment, id) values (?, ?)"], Params:[(Good, 2)]
             Query:["insert into review (comment, id) values (?, ?)"], Params:[(Ugly, 3)]
             Query:["update review set c_id=? where id=?"], Params:[(1, 1)]
             Query:["update review set c_id=? where id=?"], Params:[(1, 2)]
             Query:["update review set c_id=? where id=?"], Params:[(1, 3)]
             */

            em.persist(customer);
        });
    }

    @Test
    public void save(){};

    @Test
    public void removeFirst(){
        doInJPA(em -> {
            Customer customer = em.find(Customer.class, 1L);
            customer.getReviews().remove(0);
            em.persist(customer);
        });
    }

    @Test
    public void removeLast(){
        doInJPA(em -> {
            Customer customer = em.find(Customer.class, 1L);
            customer.getReviews().remove(customer.getReviews().size() - 1);
            em.persist(customer);
        });
    }

    @Override
    protected Class<?>[] entities() {
        return new Class<?>[]{Customer.class, Review.class};
    }

    @Getter
    @Setter
    @Entity(name = "Customer")
    @Table(name = "customer")
    public static class Customer {

        @Id
        private Long id;
        private String name;

        @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
        @JoinColumn(name = "c_id")
        private List<Review> reviews = new LinkedList<>();
    }

    @Getter
    @Setter
    @Entity(name = "Review")
    @Table(name = "review")
    public static class Review {
        @Id
        @GeneratedValue
        private Long id;
        private String comment;
    }
}
