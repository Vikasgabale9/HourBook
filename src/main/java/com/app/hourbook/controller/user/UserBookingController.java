package com.app.hourbook.controller.user;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.app.hourbook.dto.booking.BookingSlotDto;
import com.app.hourbook.model.Booking;
import com.app.hourbook.service.UserBookingService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/user/bookings")
@RequiredArgsConstructor
public class UserBookingController {

    private final UserBookingService userBookingService;

    // Razorpay public Key ID
    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    // =========================================
    // My Bookings
    // =========================================

    @GetMapping
    public String getBookings(
            HttpSession session,
            Model model) {

        Integer userId =
                (Integer) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/user/login";
        }

        List<Booking> bookings =
                userBookingService.getUserBookings(userId);

        model.addAttribute(
                "bookings",
                bookings
        );

        return "user/booking";
    }

    // =========================================
    // Booking Page / Available Slots
    // =========================================

    @GetMapping("/ground/{groundId}")
    public String bookingPage(

            @PathVariable Integer groundId,

            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            LocalDate date,

            HttpSession session,

            Model model,

            RedirectAttributes redirectAttributes) {

        Integer userId =
                (Integer) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/user/login";
        }

        if (date == null) {
            date = LocalDate.now();
        }

        try {

            List<BookingSlotDto> slots =
                    userBookingService.getAvailableSlots(
                            groundId,
                            date
                    );

            model.addAttribute(
                    "groundId",
                    groundId
            );

            model.addAttribute(
                    "selectedDate",
                    date
            );

            model.addAttribute(
                    "slots",
                    slots
            );

            return "user/book-slot";

        } catch (RuntimeException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );

            return "redirect:/user/grounds/" + groundId;
        }
    }

    // =========================================
    // Create Booking + Razorpay Order
    // =========================================

    @PostMapping("/create")
    public String createBooking(

            @RequestParam Integer groundId,

            @RequestParam
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            LocalDate bookingDate,

            @RequestParam
            @DateTimeFormat(pattern = "HH:mm")
            LocalTime startTime,

            HttpSession session,

            Model model,

            RedirectAttributes redirectAttributes) {

        Integer userId =
                (Integer) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/user/login";
        }

        try {

            // =========================================
            // Create Booking + Razorpay Order
            // =========================================

            Booking booking =
                    userBookingService
                            .createBookingAndPaymentOrder(
                                    userId,
                                    groundId,
                                    bookingDate,
                                    startTime
                            );

            // =========================================
            // Razorpay amount must be in paise
            // =========================================

            long razorpayAmountInPaise =
                    Math.round(
                            booking.getTotalAmount() * 100
                    );

            // =========================================
            // Send payment data to Thymeleaf
            // =========================================

            model.addAttribute(
                    "booking",
                    booking
            );

            model.addAttribute(
                    "razorpayOrderId",
                    booking.getRazorpayOrderId()
            );

            model.addAttribute(
                    "razorpayAmount",
                    razorpayAmountInPaise
            );

            model.addAttribute(
                    "razorpayKeyId",
                    razorpayKeyId
            );

            return "user/payment";

        } catch (RuntimeException e) {

            e.printStackTrace();

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );

            return "redirect:/user/bookings/ground/"
                    + groundId
                    + "?date="
                    + bookingDate;
        }
    }

    // =========================================
    // Verify Razorpay Payment
    // =========================================

    @PostMapping("/verify-payment")
    public String verifyPayment(

            @RequestParam Integer bookingId,

            @RequestParam String razorpayOrderId,

            @RequestParam String razorpayPaymentId,

            @RequestParam String razorpaySignature,

            HttpSession session,

            RedirectAttributes redirectAttributes) {

        Integer userId =
                (Integer) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/user/login";
        }

        try {

            System.out.println(
                    "========================================"
            );

            System.out.println(
                    "Razorpay Payment Verification"
            );

            System.out.println(
                    "Booking ID: " + bookingId
            );

            System.out.println(
                    "Order ID: " + razorpayOrderId
            );

            System.out.println(
                    "Payment ID: " + razorpayPaymentId
            );

            System.out.println(
                    "Signature received: "
                            + (razorpaySignature != null)
            );

            System.out.println(
                    "========================================"
            );

            boolean paymentVerified =
                    userBookingService.verifyPayment(
                            bookingId,
                            userId,
                            razorpayOrderId,
                            razorpayPaymentId,
                            razorpaySignature
                    );

            if (paymentVerified) {

                redirectAttributes.addFlashAttribute(
                        "success",
                        "Payment successful! Your booking is confirmed."
                );

            } else {

                redirectAttributes.addFlashAttribute(
                        "error",
                        "Payment verification failed."
                );
            }

        } catch (RuntimeException e) {

            e.printStackTrace();

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );
        }

        return "redirect:/user/bookings";
    }

    // =========================================
    // Cancel Booking
    // =========================================

    @PostMapping("/cancel/{bookingId}")
    public String cancelBooking(

            @PathVariable Integer bookingId,

            HttpSession session,

            RedirectAttributes redirectAttributes) {

        Integer userId =
                (Integer) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/user/login";
        }

        try {

            userBookingService.cancelBooking(
                    bookingId,
                    userId
            );

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Booking cancelled successfully."
            );

        } catch (RuntimeException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );
        }

        return "redirect:/user/bookings";
    }
}