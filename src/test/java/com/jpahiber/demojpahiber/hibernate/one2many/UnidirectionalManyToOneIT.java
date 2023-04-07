package com.jpahiber.demojpahiber.hibernate.one2many;

import com.jpahiber.demojpahiber.config.utils.DbTest;
import jakarta.persistence.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.Optional;
import java.util.Properties;

@Slf4j
public class UnidirectionalManyToOneIT extends DbTest {

    @Test
    public void saveReview() {
        doInJPA(em -> {
            Customer customer = em.getReference(Customer.class, 1L);

            /**
                Persist new review

                Query:["insert into review (comment, customer_id, id) values (?, ?, ?)"], Params:[(Good, 1, 1)]
             */
            Review review = new Review();
            review.setComment("Ugly");
            review.setCustomer(customer);
            em.persist(review);
        });
    }

    @Test
    public void removeCustomerFromReview() {
        doInJPA(em -> {
            Review review = em.getReference(Review.class, 1L);

            /**
                Set to review customer null

                Query:["update review set comment=?, customer_id=? where id=?"], Params:[(Good, null, 1)]
             */
            review.setCustomer(null);
        });
    }

    @Test
    public void removeReview(){
        doInJPA(em -> {
            Review review = em.find(Review.class, 1L);

            /**
                Remove review from customer

                Query:["delete from review where id=?"], Params:[(1)]
             */
            em.remove(review);
        });
    }


    @Override
    public void afterInit() {
        Customer customer1 = new Customer();
        customer1.setId(1L);
        customer1.setName("John Doe");

        Review review = new Review();
        review.setComment("Good");
        review.setCustomer(customer1);

        Review review2 = new Review();
        review2.setComment("Bad");
        review2.setCustomer(customer1);

        doInJPA(entityManager -> {
            entityManager.persist(customer1);
            entityManager.persist(review);
            entityManager.persist(review2);
        });
    }

    @Override
    protected Class<?>[] entities() {
        return new Class<?>[]{Customer.class, Review.class};
    }

    @Override
    protected Optional<Properties> getCustomProperties() {
        return Optional.of(new Properties() {{
            setProperty("hibernate.jdbc.batch_size", "3");
        }});
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
        @ManyToOne
        private Customer customer;
    }
}
