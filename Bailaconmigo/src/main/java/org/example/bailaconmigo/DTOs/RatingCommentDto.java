package org.example.bailaconmigo.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RatingCommentDto {
    private String raterName;
    private Integer stars;
    private String comment;
}
