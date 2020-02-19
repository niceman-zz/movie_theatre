create schema aa;

create table users (
    id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
    first_name varchar(256),
    last_name varchar(256),
    email varchar(256),
    birthday date,
    PRIMARY KEY (id),
    UNIQUE (email)
);

create table events (
    id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
    name varchar(256),
    price DECIMAL(15, 3),
    rating varchar(16),
    PRIMARY KEY (id)
);

create table event_timetables (
    event_id integer,
    event_time timestamp,
    auditorium varchar(256),
    unique (event_id, event_time, auditorium),
    foreign key (event_id) references events(id) on delete cascade
);

create table tickets (
    event_id integer,
    seat integer,
    event_time timestamp,
    user_id integer,
    unique (event_id, seat, event_time, user_id),
    foreign key (event_id) references events(id) on delete cascade
);

create table lucky_winners (
    user_id integer,
    message varchar(4000)
);

create table event_counters (
    event_id integer,
    name_counter integer,
    price_check_counter integer,
    book_counter integer,
    foreign key (event_id) references events(id) on delete cascade
);

create table discount_counters (
    discount_name varchar(128),
    discount_counter integer,
    user_id integer,
    foreign key (user_id) references users(id) on delete cascade
)