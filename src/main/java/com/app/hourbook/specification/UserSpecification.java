package com.app.hourbook.specification;

import org.springframework.data.jpa.domain.Specification;

import com.app.hourbook.model.AppUserDetails;

public class UserSpecification {

    private UserSpecification() {
    }

    public static Specification<AppUserDetails> search(String keyword) {

        return (root, query, criteriaBuilder) -> {

            if (keyword == null || keyword.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            String searchText = "%" + keyword.trim().toLowerCase() + "%";

            return criteriaBuilder.or(

                    criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("fullName")),
                            searchText),

                    criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("phoneNumber")),
                            searchText),

                    criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("email")),
                            searchText)

            );

        };

    }

}