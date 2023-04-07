package com.jpahiber.demojpahiber.hibernate.one2one;

import com.jpahiber.demojpahiber.config.utils.DbTest;
import jakarta.persistence.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.Optional;
import java.util.Properties;

/**
 * unidirectional one to one relationship
 * the problem with this approach is that we need and
 *  index for the   primary key of the parent table
 *  and the         foreign key of the child table
 *
 *  so better mapping would be if we could use a single column which is both
 *  the     primary key of the child table
 *  and a   foreign key to the parent table primary key
 *  @link UnidirectionalOneToOneMapsId
 *
 *
 * */
@Slf4j
public class UnidirectionalOneToOne extends DbTest {

    @Test
    public void saveReview() {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setName("John Doe");

        Review review = new Review();
        review.setComment("Good");
        review.setCustomer(customer);

        doInJPA(entityManager -> {
            entityManager.persist(review);
        });
    }

    @Override
    protected Class<?>[] entities() {
        return new Class<?>[]{Customer.class, Review.class};
    }


    @Data
    @Entity(name = "Customer")
    @Table(name = "customer")
    public static class Customer {

        @Id
        private Long id;
        private String name;

    }

    @Data
    @Entity(name = "Review")
    @Table(name = "review")
    public static class Review {
        @Id
        @GeneratedValue
        private Long id;
        private String comment;
        @OneToOne(cascade = CascadeType.ALL)
        private Customer customer;
    }
}
