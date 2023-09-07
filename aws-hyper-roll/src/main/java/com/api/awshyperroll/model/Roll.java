package com.api.awshyperroll.model;
import lombok.Data;
import lombok.NonNull;

@Data
public class Roll {
    @NonNull
    private String player;
    @NonNull
    private Integer roll; 
}
