package com.jpahiber.demojpahiber.hibernate.one2one;

import com.jpahiber.demojpahiber.config.utils.DbTest;
import jakarta.persistence.*;
import lombok.Data;
import org.junit.Test;

public class UnidirectionalOneToOneMapsId extends DbTest {

    @Test
    public void saveReview() {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setName("John Doe");

        Review review = new Review();
        review.setComment("Good");
        review.setCustomer(customer);

        doInJPA(entityManager -> entityManager.persist(review));

        doInJPA(em -> em.find(Review.class, 1L));
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
        private Long id;
        private String comment;
        @OneToOne
        @MapsId
        private Customer customer;
    }
}
