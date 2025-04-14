package com.example.english.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

import java.util.Date;

@Builder
@AllArgsConstructor
@Entity
@Getter
@Setter
@NoArgsConstructor
public class InvalidatedToken {
    @Id
    private String id;
    private Date expiryTime;// token hết hạn thì chúng ta có thể 1 ngày xóa đi chẳng hạn th db trong bảng sẽ nhẹ
}
