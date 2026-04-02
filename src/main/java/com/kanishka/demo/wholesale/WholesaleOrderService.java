package com.kanishka.demo.wholesale;

import com.kanishka.demo.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class WholesaleOrderService {

    private final WholesaleOrderRepository orderRepository;
    private final WholesaleProductRepository productRepository;

    public void createOrder(User user,
                            Long productId,
                            int quantity) {

        if (!user.getWholesaleApproved()) {
            throw new RuntimeException("Wholesale not approved");
        }

        WholesaleProduct product =
                productRepository.findById(productId)
                        .orElseThrow();

        //  Minimum quantity check from DB
        if (quantity < product.getMinimumQuantity()) {
            throw new RuntimeException(
                    "Minimum order is " + product.getMinimumQuantity()
            );
        }

        BigDecimal unitPrice = product.getPrice();
        BigDecimal total = unitPrice.multiply(BigDecimal.valueOf(quantity));

        // Optional bulk discount
        if (quantity > 100) {
            total = total.multiply(new BigDecimal("0.90"));
        }

        WholesaleOrder order =
                WholesaleOrder.builder()
                        .user(user)
                        .product(product)
                        .quantity(quantity)
                        .unitPrice(unitPrice)
                        .totalAmount(total)
                        .status("PENDING")
                        .paymentStatus("PAY_LATER")
                        .build();

        orderRepository.save(order);
    }
}
