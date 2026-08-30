package com.app.hourbook.specification;

import org.springframework.data.jpa.domain.Specification;

import com.app.hourbook.enums.GroundStatus;
import com.app.hourbook.model.Ground;

public class GroundSpecification {

    private GroundSpecification() {
    }

    public static Specification<Ground> filter(String keyword,
                                               GroundStatus status) {

        return (root, query, cb) -> {

            Specification<Ground> specification = Specification.where(null);

            // Search
            if (keyword != null && !keyword.trim().isEmpty()) {

                String search = "%" + keyword.trim().toLowerCase() + "%";

                specification = specification.and((r, q, c) ->
                        c.or(
                                c.like(c.lower(r.get("name")), search),
                                c.like(c.lower(r.get("location")), search),
                                c.like(c.lower(r.get("sports")), search)
                        ));
            }

            // Status Filter
            if (status != null) {

                specification = specification.and((r, q, c) ->
                        c.equal(r.get("status"), status));
            }

            return specification.toPredicate(root, query, cb);
        };
    }
}