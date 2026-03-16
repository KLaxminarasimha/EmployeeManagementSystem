package com.uniquehire.ems.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class LeaveReviewRequest {
    @NotNull  private Long   reviewerId;
    @Size(max = 500) private String comment;
}
