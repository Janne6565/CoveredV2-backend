package com.janne.coveredv2.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProfileValidityResponse {
    private boolean isValid;
    private InvalidityReason reason;

    public enum InvalidityReason {
        GAMES_NOT_ACCESSIBLE,
        PROFILE_NOT_FOUND;

        @Override
        public String toString() {
            return switch (this) {
                case GAMES_NOT_ACCESSIBLE -> "GAMES_NOT_ACCESSIBLE";
                case PROFILE_NOT_FOUND -> "PROFILE_NOT_FOUND";
            };
        }
    }
}
