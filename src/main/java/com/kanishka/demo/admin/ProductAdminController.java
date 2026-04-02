package com.kanishka.demo.admin;

import com.kanishka.demo.catalog.Product;
import com.kanishka.demo.catalog.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/admin/products")
@RequiredArgsConstructor
public class ProductAdminController {

    private final ProductRepository productRepository;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("products", productRepository.findAll());
        return "admin/products/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("product", new Product());
        return "admin/products/form";
    }

    @PostMapping("/new")
    public String create(
            @RequestParam String brand,
            @RequestParam String name,
            @RequestParam String sizeLabel,
            @RequestParam BigDecimal priceLkr,
            @RequestParam(defaultValue = "0") Integer stockQty,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String imageUrl,
            @RequestParam(defaultValue = "true") Boolean active,
            RedirectAttributes ra) {

        String error = validateProduct(brand, name, sizeLabel, priceLkr, stockQty);
        if (error != null) {
            ra.addFlashAttribute("error", error);
            return "redirect:/admin/products/new";
        }

        Product product = Product.builder()
                .brand(brand.trim())
                .name(name.trim())
                .sizeLabel(sizeLabel.trim())
                .priceLkr(priceLkr)
                .stockQty(stockQty)
                .description(description != null ? description.trim() : null)
                .imageUrl(imageUrl != null ? imageUrl.trim() : null)
                .active(active)
                .build();

        productRepository.save(product);
        ra.addFlashAttribute("success", "Product \"" + name + "\" created successfully!");
        return "redirect:/admin/products";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        model.addAttribute("product", product);
        return "admin/products/form";
    }

    @PostMapping("/{id}/edit")
    public String edit(
            @PathVariable Long id,
            @RequestParam String brand,
            @RequestParam String name,
            @RequestParam String sizeLabel,
            @RequestParam BigDecimal priceLkr,
            @RequestParam Integer stockQty,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String imageUrl,
            @RequestParam(defaultValue = "true") Boolean active,
            RedirectAttributes ra) {

        String error = validateProduct(brand, name, sizeLabel, priceLkr, stockQty);
        if (error != null) {
            ra.addFlashAttribute("error", error);
            return "redirect:/admin/products/" + id + "/edit";
        }

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        product.setBrand(brand.trim());
        product.setName(name.trim());
        product.setSizeLabel(sizeLabel.trim());
        product.setPriceLkr(priceLkr);
        product.setStockQty(stockQty);
        product.setDescription(description != null ? description.trim() : null);
        product.setImageUrl(imageUrl != null ? imageUrl.trim() : null);
        product.setActive(active);

        productRepository.save(product);
        ra.addFlashAttribute("success", "Product updated successfully!");
        return "redirect:/admin/products";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        if (!productRepository.existsById(id)) {
            ra.addFlashAttribute("error", "Product not found.");
            return "redirect:/admin/products";
        }
        productRepository.deleteById(id);
        ra.addFlashAttribute("success", "Product deleted successfully!");
        return "redirect:/admin/products";
    }

    @PostMapping("/{id}/toggle-active")
    public String toggleActive(@PathVariable Long id, RedirectAttributes ra) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        product.setActive(!product.getActive());
        productRepository.save(product);
        String status = product.getActive() ? "activated" : "deactivated";
        ra.addFlashAttribute("success", "Product " + status + " successfully!");
        return "redirect:/admin/products";
    }

    // ── Shared validation ──
    private String validateProduct(String brand, String name, String sizeLabel,
                                   BigDecimal priceLkr, Integer stockQty) {
        if (brand == null || brand.trim().isBlank())
            return "Brand is required.";
        if (name == null || name.trim().isBlank())
            return "Product name is required.";
        if (sizeLabel == null || sizeLabel.trim().isBlank())
            return "Size label is required.";
        if (priceLkr == null || priceLkr.compareTo(BigDecimal.ZERO) <= 0)
            return "Price must be greater than zero.";
        if (stockQty == null || stockQty < 0)
            return "Stock quantity cannot be negative.";
        return null;
    }
}