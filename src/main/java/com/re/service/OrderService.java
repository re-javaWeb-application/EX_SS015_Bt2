package com.re.service;

import com.re.entity.Order;
import com.re.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional
    public void cancelFraudulentOrder(Long orderId) {
        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException("Mã đơn hàng không hợp lệ");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với ID: " + orderId));

        if ("DELIVERED".equals(order.getStatus())) {
            throw new IllegalStateException("Không thể hủy đơn hàng đã giao thành công");
        }

        orderRepository.cancelFraudulentOrder(orderId);
    }
}
