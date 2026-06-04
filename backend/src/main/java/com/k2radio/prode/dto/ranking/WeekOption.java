package com.k2radio.prode.dto.ranking;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeekOption {
    private LocalDateTime weekStart;
    private LocalDateTime weekEnd;
    private String label;
}
