/*
 *
 *  * IPB Signals client
 *  * Copyright 2024 Leibniz-Institut f. Pflanzenbiochemie
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  *
 *
 */

package de.ipb_halle.inhouse.util;

import java.util.Optional;

/**
 * Normalizes and validates IPB codes.
 * Accepts "IPB000001" and "IPB_000001" and returns "IPB_000001".
 */
public class IpbCodeNormalizer {

    private IpbCodeNormalizer() {
    }

    public static Optional<String> normalize(String raw) {
        if (raw == null) return Optional.empty();
        String s = raw.trim();
        if (s.isEmpty()) return Optional.empty();
        if (!s.startsWith("IPB")) return Optional.empty();

        // remove prefix IPB and IPB_
        String digits = s.substring(3).replaceFirst("^_", "");
        //check if the rest are digest
        if (!digits.matches("\\d+")) return Optional.empty();

        try {
            int n = Integer.parseInt(digits);
            return Optional.of(String.format("IPB_%06d", n));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    /** Extracts numeric part from normalized or raw IPB code; returns empty if invalid. */
    public static Optional<Integer> extractNumeric(String raw) {
        return normalize(raw).map(n -> Integer.parseInt(n.substring("IPB_".length())));
    }
}
