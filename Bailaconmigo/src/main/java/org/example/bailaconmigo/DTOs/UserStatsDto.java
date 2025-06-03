package org.example.bailaconmigo.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserStatsDto {
    private Long totalUsers;
    private Long maleUsers;
    private Long femaleUsers;
    private Long otherGenderUsers;
    private Long basicUsers;
    private Long proUsers;
}

