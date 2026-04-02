package com.kanishka.demo.wholesale;

import com.kanishka.demo.user.User;
import com.kanishka.demo.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.Month;
import java.util.*;

@Controller
@RequestMapping("/wholesale")
@RequiredArgsConstructor
public class WholesaleController {

    private final UserRepository userRepository;
    private final WholesaleOrderRepository orderRepository;
    private final WholesaleProductRepository productRepository;
    private final WholesaleOrderService service;

    // =====================================================
    // DASHBOARD — Users only. Admins are redirected away.
    // =====================================================
    @GetMapping
    public String dashboard(Model model, Authentication auth) {

        // ── ADMIN CHECK: send admins to the admin wholesale dashboard ──
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) {
            return "redirect:/admin/wholesale/dashboard";
        }

        // ── Normal user logic ──
        User user = userRepository
                .findByEmail(auth.getName())
                .orElseThrow();

        List<WholesaleOrder> orders =
                orderRepository.findByUserOrderByCreatedAtDesc(user);

        BigDecimal totalAmount = orders.stream()
                .map(o -> Optional.ofNullable(o.getTotalAmount())
                        .orElse(BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long pendingCount  = countByStatus(orders, "PENDING");
        long approvedCount = countByStatus(orders, "APPROVED");
        long shippedCount  = countByStatus(orders, "SHIPPED");

        Map<String, BigDecimal> monthlyTotals = generateMonthlyTotals(orders);

        model.addAttribute("orders",       orders);
        model.addAttribute("products",     productRepository.findByActiveTrue());
        model.addAttribute("totalAmount",  totalAmount);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("approvedCount",approvedCount);
        model.addAttribute("shippedCount", shippedCount);
        model.addAttribute("monthlyTotals",monthlyTotals);
        model.addAttribute("approved",     Boolean.TRUE.equals(user.getWholesaleApproved()));
        model.addAttribute("requested",    Boolean.TRUE.equals(user.getWholesaleRequested()));

        return "wholesale/dashboard";   // user-only template
    }

    // =====================================================
    // REQUEST WHOLESALE ACCESS
    // =====================================================
    @PostMapping("/request-access")
    public String requestAccess(Authentication auth, RedirectAttributes ra) {

        User user = userRepository.findByEmail(auth.getName()).orElseThrow();

        if (Boolean.TRUE.equals(user.getWholesaleApproved())) {
            ra.addFlashAttribute("success", "Your wholesale account is already approved.");
            return "redirect:/wholesale";
        }

        if (Boolean.TRUE.equals(user.getWholesaleRequested())) {
            ra.addFlashAttribute("success",
                    "Wholesale request already submitted. Please wait for admin approval.");
            return "redirect:/wholesale";
        }

        user.setWholesaleRequested(true);
        userRepository.save(user);

        ra.addFlashAttribute("success", "Wholesale access request submitted successfully.");
        return "redirect:/wholesale";
    }

    // =====================================================
    // CREATE WHOLESALE ORDER
    // =====================================================
    @PostMapping("/order")
    public String createOrder(Authentication auth,
                              @RequestParam Long productId,
                              @RequestParam int quantity,
                              RedirectAttributes ra) {

        User user = userRepository.findByEmail(auth.getName()).orElseThrow();

        if (!Boolean.TRUE.equals(user.getWholesaleApproved())) {
            ra.addFlashAttribute("error", "Your wholesale account is not approved yet.");
            return "redirect:/wholesale";
        }

        if (quantity < 50) {
            ra.addFlashAttribute("error", "Minimum order quantity is 50.");
            return "redirect:/wholesale";
        }

        try {
            service.createOrder(user, productId, quantity);
            ra.addFlashAttribute("success", "Wholesale order created successfully.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Failed to create order: " + e.getMessage());
        }

        return "redirect:/wholesale";
    }

    // =====================================================
    // HELPERS
    // =====================================================
    private long countByStatus(List<WholesaleOrder> orders, String status) {
        return orders.stream()
                .filter(o -> status.equalsIgnoreCase(o.getStatus()))
                .count();
    }

    private Map<String, BigDecimal> generateMonthlyTotals(List<WholesaleOrder> orders) {
        Map<String, BigDecimal> monthly = new LinkedHashMap<>();
        for (Month month : Month.values()) {
            monthly.put(month.name(), BigDecimal.ZERO);
        }
        for (WholesaleOrder order : orders) {
            if (order.getCreatedAt() == null) continue;
            String month = order.getCreatedAt().getMonth().name();
            monthly.put(month, monthly.get(month)
                    .add(Optional.ofNullable(order.getTotalAmount()).orElse(BigDecimal.ZERO)));
        }
        return monthly;
    }
}