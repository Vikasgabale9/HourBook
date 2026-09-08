package com.app.hourbook.controller.vendor;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.app.hourbook.enums.BookingStatus;
import com.app.hourbook.model.Booking;
import com.app.hourbook.service.VendorBookingService;
import com.app.hourbook.service.VendorRevenueService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/vendor")
@RequiredArgsConstructor
public class VendorStaticController {

    private final VendorBookingService vendorBookingService;

    private final VendorRevenueService vendorRevenueService;

    // =========================
    // BOOKINGS
    // =========================

    @GetMapping("/bookings")
    public String bookings(
            @RequestParam(required = false) String status,
            HttpSession session,
            Model model) {

        Integer vendorId =
                (Integer) session.getAttribute("vendorId");

        if (vendorId == null) {
            return "redirect:/vendor/login";
        }

        // Get ALL vendor bookings
        List<Booking> allBookings =
                vendorBookingService.getVendorBookings(vendorId);

        // Filter bookings if status is selected
        List<Booking> bookings;

        if (status == null || status.isBlank()) {

            bookings = allBookings;

        } else {

            bookings =
                    vendorBookingService
                            .getVendorBookingsByStatus(
                                    vendorId,
                                    status
                            );
        }

        // =========================
        // STATISTICS
        // =========================

        long totalBookings = allBookings.size();

        long confirmedCount =
                allBookings.stream()
                        .filter(b ->
                                b.getStatus() == BookingStatus.CONFIRMED)
                        .count();

        long pendingCount =
                allBookings.stream()
                        .filter(b ->
                                b.getStatus() == BookingStatus.PENDING)
                        .count();

        long cancelledCount =
                allBookings.stream()
                        .filter(b ->
                                b.getStatus() == BookingStatus.CANCELLED)
                        .count();

        // =========================
        // SEND DATA TO HTML
        // =========================

        model.addAttribute("bookings", bookings);

        model.addAttribute(
                "selectedStatus",
                status
        );

        model.addAttribute(
                "totalBookings",
                totalBookings
        );

        model.addAttribute(
                "confirmedCount",
                confirmedCount
        );

        model.addAttribute(
                "pendingCount",
                pendingCount
        );

        model.addAttribute(
                "cancelledCount",
                cancelledCount
        );

        return "vendor/bookings";
    }


    // =========================
    // REVENUE
    // =========================

    @GetMapping("/revenue")
    public String revenue(
            HttpSession session,
            Model model) {

        Integer vendorId =
                (Integer) session.getAttribute("vendorId");

        if (vendorId == null) {
            return "redirect:/vendor/login";
        }

        List<Booking> revenueBookings =
                vendorRevenueService
                        .getVendorRevenueBookings(vendorId);

        Double totalRevenue =
                vendorRevenueService
                        .getTotalRevenue(vendorId);

        model.addAttribute(
                "revenueBookings",
                revenueBookings
        );

        model.addAttribute(
                "totalRevenue",
                totalRevenue
        );

        return "vendor/revenue";
    }
}