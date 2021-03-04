package com.java.entity;

import lombok.*;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Accessors(chain = true)
public class Stock {
    private Integer id;
    private String name;
    private Integer count;
    private Integer sale;
    private Integer version;
}
