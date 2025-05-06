package org.example.bailaconmigo.DTOs;

public class MatchResponseDto {
    private boolean matched;

    public MatchResponseDto(boolean matched) {
        this.matched = matched;
    }

    public boolean isMatched() {
        return matched;
    }

    public void setMatched(boolean matched) {
        this.matched = matched;
    }
}
