package com.restaurant.application.dto.response;

/**
 * Derived occupancy state of a table at a given instant for the owner's floor-plan view.
 * This is a presentation concept, not a domain status.
 */
public enum TableStatus {
    FREE,       // no active reservation occupies the table
    PENDING,    // a reservation pending confirmation occupies it
    RESERVED,   // a confirmed reservation occupies it
    SEATED      // guests are seated (produced once SEATED reservation status exists)
}
