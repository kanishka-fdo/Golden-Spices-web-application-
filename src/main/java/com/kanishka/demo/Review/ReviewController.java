package com.kanishka.demo.Review;

import com.kanishka.demo.catalog.Product;
import com.kanishka.demo.catalog.ProductRepository;
import com.kanishka.demo.user.User;
import com.kanishka.demo.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ReviewController {

        private final ReviewRepository  reviewRepository;
        private final ProductRepository productRepository;
        private final UserRepository    userRepository;

        /* ═══════════════════════════════════════
           GET /products/{id}/reviews
        ═══════════════════════════════════════ */
        @GetMapping("/products/{id}/reviews")
        @Transactional(readOnly = true)
        public String productReviews(
                @PathVariable Long id,
                @RequestParam(defaultValue = "0") int page,
                @AuthenticationPrincipal UserDetails principal,
                Model model) {

                Product product = productRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Product not found: " + id));

                Page<Review> reviewPage = reviewRepository
                        .findByProductAndApprovedTrueAndDeletedFalse(
                                product, PageRequest.of(page, 5));

                List<Review> reviews = reviewPage.getContent();

                double avgRating = reviews.stream()
                        .mapToInt(Review::getRating)
                        .average().orElse(0.0);

                long[] breakdown = new long[5];
                for (int i = 1; i <= 5; i++) {
                        breakdown[i - 1] = reviewRepository
                                .countByProductAndRatingAndApprovedTrueAndDeletedFalse(product, i);
                }

                Review  userReview = null;
                boolean canReview  = false;
                boolean hasOrdered = false;

                if (principal != null) {
                        User user = userRepository.findByEmail(principal.getUsername()).orElse(null);
                        if (user != null) {
                                userReview = reviewRepository.findByUserAndProduct(user, product).orElse(null);
                                hasOrdered = reviewRepository
                                        .hasUserOrderedProductNative(user.getId(), id) > 0;
                                canReview  = (userReview == null);
                        }
                }

                model.addAttribute("product",     product);
                model.addAttribute("reviews",     reviews);
                model.addAttribute("avgRating",   avgRating);
                model.addAttribute("reviewCount", reviewPage.getTotalElements());
                model.addAttribute("breakdown",   breakdown);
                model.addAttribute("userReview",  userReview);
                model.addAttribute("hasMore",     reviewPage.hasNext());
                model.addAttribute("nextPage",    page + 1);
                model.addAttribute("canReview",   canReview);
                model.addAttribute("hasOrdered",  hasOrdered);

                return "reviews/product-reviews";
        }

        /* ═══════════════════════════════════════
           POST /products/{id}/reviews
        ═══════════════════════════════════════ */
        @PostMapping("/products/{id}/reviews")
        @Transactional
        public String saveReview(
                @PathVariable Long id,
                @RequestParam int rating,
                @RequestParam String comment,
                @AuthenticationPrincipal UserDetails principal,
                RedirectAttributes ra) {

                if (principal == null) {
                        ra.addFlashAttribute("error", "Please log in to leave a review.");
                        return "redirect:/auth/login";
                }
                if (rating < 1 || rating > 5) {
                        ra.addFlashAttribute("error", "Rating must be between 1 and 5 stars.");
                        return "redirect:/products/" + id + "/reviews";
                }
                String trimmed = (comment != null) ? comment.trim() : "";
                if (trimmed.length() < 10) {
                        ra.addFlashAttribute("error", "Review must be at least 10 characters.");
                        return "redirect:/products/" + id + "/reviews";
                }
                if (trimmed.length() > 1000) {
                        ra.addFlashAttribute("error", "Review must not exceed 1000 characters.");
                        return "redirect:/products/" + id + "/reviews";
                }

                User user = userRepository.findByEmail(principal.getUsername())
                        .orElseThrow(() -> new RuntimeException("User not found"));
                Product product = productRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Product not found"));

                boolean hasOrdered = reviewRepository
                        .hasUserOrderedProductNative(user.getId(), id) > 0;

                Review review = reviewRepository
                        .findByUserAndProduct(user, product)
                        .orElseGet(() -> Review.builder()
                                .user(user).product(product).verifiedPurchase(false).build());

                review.setRating(rating);
                review.setComment(trimmed);
                review.setApproved(true);   // visible immediately — no moderation queue
                review.setDeleted(false);
                review.setVerifiedPurchase(hasOrdered);
                reviewRepository.save(review);

                ra.addFlashAttribute("success", "Thank you! Your review has been posted.");
                return "redirect:/products/" + id + "/reviews";
        }

        /* ═══════════════════════════════════════
           POST /products/{id}/reviews/{reviewId}/delete
        ═══════════════════════════════════════ */
        @PostMapping("/products/{id}/reviews/{reviewId}/delete")
        @Transactional
        public String deleteReview(
                @PathVariable Long id,
                @PathVariable Long reviewId,
                @AuthenticationPrincipal UserDetails principal,
                RedirectAttributes ra) {

                if (principal == null) return "redirect:/auth/login";

                User user = userRepository.findByEmail(principal.getUsername()).orElseThrow();
                Review review = reviewRepository.findById(reviewId)
                        .orElseThrow(() -> new RuntimeException("Review not found"));

                boolean isAdmin = user.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

                if (!review.getUser().getId().equals(user.getId()) && !isAdmin) {
                        ra.addFlashAttribute("error", "You cannot delete this review.");
                        return "redirect:/products/" + id + "/reviews";
                }

                review.setDeleted(true);
                reviewRepository.save(review);

                ra.addFlashAttribute("success", "Review removed.");
                return "redirect:/products/" + id + "/reviews";
        }
}