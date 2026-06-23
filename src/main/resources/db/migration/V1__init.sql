-- Baseline schema for restaurant-api.
-- Generated from the JPA entity model and aligned with Spring Boot's default
-- physical naming strategy (CamelCaseToUnderscoresNamingStrategy), so that
-- `ddl-auto: validate` in production matches exactly. Do not edit by hand:
-- evolve the schema with new V2__*, V3__*, ... migrations instead.

create table users (
    id bigint not null auto_increment,
    email varchar(255) not null,
    email_verified boolean default false not null,
    global_role enum ('ADMIN','USER') not null,
    name varchar(255),
    password varchar(255) not null,
    restaurant_role enum ('OWNER','WORKER'),
    tlf varchar(255),
    username varchar(255) not null,
    primary key (id)
) engine=InnoDB;

create table restaurants (
    id bigint not null auto_increment,
    average_price decimal(10,2),
    city varchar(255),
    cuisine_type enum ('AMERICAN','CHINESE','FRENCH','FUSION','GREEK','INDIAN','ITALIAN','JAPANESE','MEDITERRANEAN','MEXICAN','MIDDLE_EASTERN','OTHER','SPANISH','THAI','VEGAN','VEGETARIAN'),
    email varchar(255) not null,
    lat decimal(10,7),
    lon decimal(10,7),
    name varchar(255) not null,
    postal_code varchar(255),
    province varchar(255),
    size integer not null,
    street varchar(255),
    tlf varchar(255) not null,
    owner_id bigint,
    primary key (id)
) engine=InnoDB;

create table restaurant_dietary_options (
    restaurant_id bigint not null,
    dietary_option enum ('DAIRY_FREE','GLUTEN_FREE','HALAL','KOSHER','LOW_CARB','NUT_FREE','ORGANIC','VEGAN','VEGETARIAN')
) engine=InnoDB;

create table restaurant_tables (
    id bigint not null auto_increment,
    active bit not null,
    capacity integer not null,
    height integer not null,
    label varchar(32) not null,
    min_capacity integer,
    restaurant_id bigint not null,
    rotation integer not null,
    shape enum ('CIRCLE','RECTANGLE','SQUARE') not null,
    width integer not null,
    x integer not null,
    y integer not null,
    zone varchar(32),
    primary key (id)
) engine=InnoDB;

create table menus (
    id bigint not null auto_increment,
    updated_at datetime(6),
    restaurant_id bigint not null,
    primary key (id)
) engine=InnoDB;

create table menu_categories (
    id bigint not null auto_increment,
    name varchar(255) not null,
    menu_id bigint not null,
    primary key (id)
) engine=InnoDB;

create table menu_items (
    id bigint not null auto_increment,
    available bit not null,
    description varchar(255),
    name varchar(255) not null,
    price decimal(10,2) not null,
    menu_category_id bigint not null,
    primary key (id)
) engine=InnoDB;

create table reservations (
    id bigint not null auto_increment,
    booker_email varchar(255) not null,
    created_at datetime(6),
    end_date datetime(6) not null,
    party_size integer not null,
    reminder_sent boolean default false not null,
    restaurant_id bigint not null,
    start_date datetime(6) not null,
    status enum ('CANCELLED','COMPLETED','CONFIRMED','EXPIRED','NO_SHOW','PENDING','SEATED') not null,
    table_id bigint,
    user_id bigint,
    primary key (id)
) engine=InnoDB;

create table email_verification_tokens (
    id bigint not null auto_increment,
    created_at datetime(6) not null,
    expires_at datetime(6) not null,
    token varchar(255) not null,
    used bit not null,
    user_id bigint not null,
    primary key (id)
) engine=InnoDB;

-- Unique constraints
alter table users
    add constraint UK6dotkott2kjsp8vw4d0m25fb7 unique (email);
alter table users
    add constraint UKr43af9ap4edm43mmtq01oddj6 unique (username);
alter table restaurants
    add constraint UKfe6aq2gbudopv20yl6b77798m unique (email);
alter table restaurant_tables
    add constraint uk_table_restaurant_label unique (restaurant_id, label);
alter table email_verification_tokens
    add constraint UKewmvysc7e9y6uy7og2c21axa9 unique (token);

-- Indexes
create index idx_reservation_restaurant on reservations (restaurant_id);
create index idx_reservation_user on reservations (user_id);
create index idx_reservation_booker on reservations (booker_email);
create index idx_reservation_start on reservations (start_date);
create index idx_reservation_table on reservations (table_id);
create index idx_table_restaurant on restaurant_tables (restaurant_id);
create index idx_table_restaurant_active on restaurant_tables (restaurant_id, active);
create index idx_evt_user on email_verification_tokens (user_id);

-- Foreign keys
alter table restaurants
    add constraint FKp5mmmypepihvmkdb83qwugr4d
    foreign key (owner_id) references users (id);
alter table restaurant_dietary_options
    add constraint FK6qabndi59m5nphs7arp1purkl
    foreign key (restaurant_id) references restaurants (id);
alter table menus
    add constraint FK49thmnytvo47ttv4ggtwo9rrj
    foreign key (restaurant_id) references restaurants (id);
alter table menu_categories
    add constraint FKax1ta71qnj1apulb8h5f5mqu2
    foreign key (menu_id) references menus (id);
alter table menu_items
    add constraint FKitumf607k1c3c0k2n3ga80svw
    foreign key (menu_category_id) references menu_categories (id);
