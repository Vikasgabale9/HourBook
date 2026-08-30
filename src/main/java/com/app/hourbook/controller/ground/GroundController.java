package com.app.hourbook.controller.ground;

import java.io.IOException;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.app.hourbook.dto.GroundRequestDto;
import com.app.hourbook.repository.VendorRepository;
import com.app.hourbook.service.GroundService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/grounds")
@RequiredArgsConstructor
public class GroundController {

    private final GroundService groundService;
    private final VendorRepository vendorRepository;

    // =================== Add Ground ===================

    @GetMapping("/add")
    public String addGroundPage(Model model) {

        model.addAttribute("ground", new GroundRequestDto());
        model.addAttribute("vendors", vendorRepository.findAll());

        return "admin/add-ground";
    }

    @PostMapping("/save")
    public String saveGround(
            @ModelAttribute GroundRequestDto request,
            @RequestParam("images") MultipartFile[] images)
            throws IOException {

    	System.out.println("Controller save : "+images.length);
        groundService.createGround(request, images);

        return "redirect:/admin/grounds";
    }

    // =================== Edit Ground ===================

    @GetMapping("/edit/{id}")
    public String editGround(
            @PathVariable Integer id,
            Model model) {

        model.addAttribute("ground",
                groundService.getGroundById(id));

        model.addAttribute("vendors",
                vendorRepository.findAll());

        return "admin/edit-ground";
    }

    @PostMapping("/update/{id}")
    public String updateGround(
            @PathVariable Integer id,
            @ModelAttribute GroundRequestDto request,
            @RequestParam(value = "images", required = false)
            MultipartFile[] images)
            throws IOException {

        groundService.updateGround(id, request, images);

        return "redirect:/admin/grounds";
    }

    // =================== View Images ===================

    @GetMapping("/images/{id}")
    public String viewImages(
            @PathVariable Integer id,
            Model model) {

        model.addAttribute("ground",
                groundService.getGroundById(id));

        return "admin/ground-images";
    }

    // =================== Approve ===================

    @GetMapping("/approve/{id}")
    public String approveGround(@PathVariable Integer id) {

        groundService.approveGround(id);

        return "redirect:/admin/grounds";
    }

    // =================== Reject ===================

    @GetMapping("/reject/{id}")
    public String rejectGround(@PathVariable Integer id) {

        groundService.rejectGround(id);

        return "redirect:/admin/grounds";
    }

    // =================== Disable ===================

    @GetMapping("/disable/{id}")
    public String disableGround(@PathVariable Integer id) {

        groundService.disableGround(id);

        return "redirect:/admin/grounds";
    }

    // =================== Enable ===================

    @GetMapping("/enable/{id}")
    public String enableGround(@PathVariable Integer id) {

        groundService.enableGround(id);

        return "redirect:/admin/grounds";
    }

    // =================== Delete ===================

    @GetMapping("/delete/{id}")
    public String deleteGround(@PathVariable Integer id) {

        groundService.deleteGround(id);

        return "redirect:/admin/grounds";
    }

}