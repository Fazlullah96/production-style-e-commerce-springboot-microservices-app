package com.example.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "address")
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "address_seq")
    @SequenceGenerator(name = "address_seq", sequenceName = "address_id_seq", allocationSize = 1)
    private Integer id;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    private String street;
    private String city;
    private String state;
    @Min(value = 100000)
    @Max(value = 999999)
    private Integer pinCode;
    private Boolean isDefault;
}
