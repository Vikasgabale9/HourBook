package com.app.hourbook.controller.vendor;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/vendor")
public class VendorStaticController {

    @GetMapping("/bookings")
    public String bookings() {

        return "vendor/bookings";
    }

    @GetMapping("/revenue")
    public String revenue() {

        return "vendor/revenue";
    }

}