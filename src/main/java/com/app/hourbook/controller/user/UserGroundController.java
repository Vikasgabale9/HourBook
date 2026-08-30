package com.app.hourbook.controller.user;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.app.hourbook.model.Ground;
import com.app.hourbook.service.UserGroundService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/user/grounds")
@RequiredArgsConstructor
public class UserGroundController {

	private final UserGroundService service;

	@GetMapping
	public String grounds(

			@RequestParam(defaultValue = "0") int page,

			@RequestParam(defaultValue = "5") int size,

			@RequestParam(required = false) String search,

			Model model) {
		System.out.println("grounds controller called ");
		Page<Ground> groundPage = service.getGrounds(page, size, search);

		model.addAttribute("groundPage", groundPage);

		model.addAttribute("grounds", groundPage.getContent());

		model.addAttribute("search", search);

		return "user/grounds";
	}

	@GetMapping("/{id}")
	public String groundDetails(

			@PathVariable Integer id,

			Model model) {

		model.addAttribute("ground", service.getGround(id));

		return "user/ground-details";
	}

}