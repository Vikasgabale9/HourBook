package com.app.hourbook.specification;

import org.springframework.data.jpa.domain.Specification;

import com.app.hourbook.model.VendorDetails;

public class VendorSpecification {

    private VendorSpecification() {
    }

    public static Specification<VendorDetails> search(String keyword) {

        return (root, query, cb) -> {

            if (keyword == null || keyword.trim().isEmpty()) {
                return cb.conjunction();
            }

            String search = "%" + keyword.trim().toLowerCase() + "%";

            return cb.or(

                    cb.like(cb.lower(root.get("fullName")), search),

                    cb.like(cb.lower(root.get("phoneNumber")), search),

                    cb.like(cb.lower(root.get("email")), search)

            );

        };
    }

}