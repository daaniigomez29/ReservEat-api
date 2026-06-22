package com.restaurant.application.port.in;

public interface ReservationReminderUseCase {

    /**
     * Finds confirmed reservations starting within the configured lead time that have
     * not been reminded yet, marks them as reminded and emails each booker.
     *
     * @return how many reminders were dispatched.
     */
    int sendDueReminders();
}
