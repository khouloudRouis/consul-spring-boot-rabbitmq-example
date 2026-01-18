package com.orderservice.service.criteria;
 

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.orderservice.dto.SearchRequest;
import com.orderservice.entity.Order;

import jakarta.persistence.criteria.Predicate;

public class OrderSpecification {
	  public static Specification<Order> withCriteria(SearchRequest request) {
	        return (root, query, cb) -> {

	            List<Predicate> predicates = new ArrayList<>();

	            if (request.status() != null) {
	                predicates.add(
	                    cb.equal(root.get("status"), request.status())
	                );
	            }
 

	            if (request.fromDate() != null) {
	                predicates.add(
	                    cb.greaterThanOrEqualTo(
	                        root.get("createdAt"),
	                        request.fromDate().atStartOfDay()
	                    )
	                );
	            }

	            if (request.toDate() != null) {
	                predicates.add(
	                    cb.lessThanOrEqualTo(
	                        root.get("createdAt"),
	                        request.toDate().atTime(23, 59, 59)
	                    )
	                );
	            }

	            return cb.and(predicates.toArray(new Predicate[0]));
	        };
	    }
}
