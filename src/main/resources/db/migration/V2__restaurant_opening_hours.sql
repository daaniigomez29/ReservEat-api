-- Adds the opening/closing service window to restaurants. Reservations are only
-- accepted within this window. Nullable: existing restaurants keep unrestricted
-- hours until an owner sets them. A closing_time earlier than opening_time
-- denotes a window that crosses midnight (e.g. 19:00–02:00).
alter table restaurants
    add column opening_time time null,
    add column closing_time time null;
