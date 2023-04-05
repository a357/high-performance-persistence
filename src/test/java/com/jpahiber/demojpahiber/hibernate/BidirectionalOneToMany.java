package com.jpahiber.demojpahiber.hibernate;

import com.jpahiber.demojpahiber.hibernate.utils.DbTest;
import jakarta.persistence.*;
import lombok.Data;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class BidirectionalOneToMany extends DbTest {


    @Override
    protected Class<?>[] entities() {
        return new Class[]{Customer.class, Review.class};
    }

    @Override
    protected void afterInit() {
        doInJPA(em -> {
            Customer customer = new Customer();
            customer.setId(1L);
            customer.setName("John Doe");

            Review review1 = new Review();
            review1.setComment("Good");
            customer.addReview(review1);

            Review review2 = new Review();
            review2.setComment("Ugly");
            customer.addReview(review2);

            /**
             Create customer

             Query:["insert into customer (id, name) values (?, ?)"], Params:[(1, John Doe)]
             Query:["insert into review (comment, customer_id, id) values (?, ?, ?)"], Params:[(Good, 1, 1)]
             Query:["insert into review (comment, customer_id, id) values (?, ?, ?)"], Params:[(Ugly, 1, 2)]
             */
            em.persist(customer);
        });
    }

    @Test
    public void removeReview(){
        doInJPA(em -> {
            Customer customer = em.getReference(Customer.class, 1L);

            /**
                Remove review

             Query:["select c1_0.id,c1_0.name from customer c1_0 where c1_0.id=?"], Params:[(1)]
             Query:["select r1_0.customer_id,r1_0.id,r1_0.comment from review r1_0 where r1_0.customer_id=?"], Params:[(1)]
             Query:["delete from review where id=?"], Params:[(1)]
             */
            customer.removeReview(customer.getReviews().get(0));
        });
    }


    @Data
    @Entity(name = "Customer")
    @Table(name = "customer")
    public static class Customer {

        @Id
        private Long id;
        private String name;

        @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<Review> reviews = new ArrayList<>();

        public void addReview(Review review) {
            reviews.add(review);
            review.setCustomer(this);
        }

        public void removeReview(Review review) {
            reviews.remove(review);
            review.setCustomer(null);
        }
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
