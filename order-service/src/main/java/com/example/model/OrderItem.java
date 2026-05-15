package com.example.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "orderItems")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "orderItem_seq")
    @SequenceGenerator(name = "orderItem_seq", sequenceName = "orderItem_id_seq", allocationSize = 1)
    private Integer id;
    private String skuCode;
    private Double price;
    private Integer quantity;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;
}
