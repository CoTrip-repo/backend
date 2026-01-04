package com.ssafy.cotrip.api.plan.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EditInfo {

    private Long userId;
    private String username;
    private Long startedAt; // Unix timestamp in milliseconds
}
