package com.jpahiber.demojpahiber.hibernate.one2one;

import com.jpahiber.demojpahiber.config.utils.DbTest;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.LazyToOne;
import org.hibernate.annotations.LazyToOneOption;
import org.hibernate.testing.bytecode.enhancement.BytecodeEnhancerRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

/**
 * !!! N + 1 problem for @OneToOne bidirectional relationship on the parent side !!!
 * even we set fetch type to LAZY, it will still be eager in @OneToOne bidirectional relationship on the parent side,
 * this is because unlike the parent side-side @OneToMany relationship where Hibernate will assign a collection proxy
 * even if there is no child entity, the OneToOne relationship must decide if to assign the child reference to null or to an Object,
 * be it the actual entity or lazy load proxy. This is an issue that affects only the parent side of the bidirectional @OneToOne association.
 * On the other hand, the child side, which maps the associated foreign key column, knows whether the parent reference
 * is either null or not by simply inspecting the foreign key column value.Therefore, the child side assigns a Proxy only
 * if the value of the foreign key column is not null. Otherwise, the @OneToOne association to the parent side entity
 * will be set to null.
 *
 * Solution
 * Bytecode enhancement is the only viable workaround for the N+1 query problem on the parent side @OneToOne JPA association.
 * Requires LazyToOneOption.NO_PROXY and without @MapsId on the child side.
 */
@RunWith(BytecodeEnhancerRunner.class)
public class BidirectionalOneToOneN1Solution extends DbTest {

    @Test //on parent side, it will be eager
    public void findParentSideCustomer() {


        doInJPA(entityManager -> {
            for (int i = 0; i < 3; i++) {
                Review review = new Review();
                review.setComment("Good" + i);

                Customer customer = new Customer();
                customer.setId((long) i);
                customer.setName("John Doe: " + i);
                customer.setReview(review);

                entityManager.persist(customer);
            }
        });

        doInJPA(em -> {
            List<Customer> customers = em
                    .createQuery("select c from Customer c where c.name like 'John%'", Customer.class)
                    .getResultList();
        });
    }

    @Test //on child side, it will be lazy
    public void findChildSideReview() {
        doInJPA(entityManager -> {
            for (int i = 0; i < 3; i++) {
                Review review = new Review();
                review.setComment("Good" + i);

                Customer customer = new Customer();
                customer.setId((long) i);
                customer.setName("John Doe: " + i);
                customer.setReview(review);

                entityManager.persist(customer);
            }
        });

        doInJPA(em -> {
            List<Review> reviews = em
                    .createQuery("select r from Review r where r.comment like 'Goo%'", Review.class)
                    .getResultList();
        });

    }

    @Override
    protected Class<?>[] entities() {
        return new Class[]{Customer.class, Review.class};
    }

    @Data
    @Entity(name = "Customer")
    @Table(name = "customer")
    public static class Customer {
        @Id
        private Long id;
        private String name;

        @OneToOne(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
        @LazyToOne(LazyToOneOption.NO_PROXY)
        private Review review;

        public void setReview(Review review) {
            this.review = review;
            review.setCustomer(this);
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
        @OneToOne(fetch = FetchType.LAZY)
        @MapsId
        private Customer customer;
    }
}
