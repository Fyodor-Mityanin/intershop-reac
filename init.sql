create table if not exists items
(
    id serial primary key,
    title text not null,
    price numeric(12,2) not null,
    description text not null,
    img_path text not null
);

CREATE TABLE users (
    id          serial primary key,
    login       varchar(100) unique not null,
    password    varchar(255) not null,
    role        varchar(50) not null
);

create table if not exists orders
(
    id serial primary key,
    order_time timestamp not null default current_timestamp,
    user_login text references users (login) not null,
    status text not null
);

create table if not exists order_items
(
    id serial primary key,
    order_id integer references orders (id) not null,
    item_id integer references items (id) not null,
    quantity smallint not null default 0,
    CONSTRAINT uk_order_item UNIQUE (order_id, item_id)
);


--Наполнение таблицы

DO $$
DECLARE
    i INT := 1;
BEGIN
    WHILE i <= 100 LOOP
            INSERT INTO items (title, price, description, img_path)
            VALUES (
                       'Item ' || i,
                       (random() * 1000)::numeric(12,2),
                       'Description for item ' || i,
                       '/images/item' || i || '.jpg'
                   );
            i := i + 1;
        END LOOP;
END $$;


INSERT INTO users (login, password, role) VALUES
-- пароль "password"
('user', '{bcrypt}$2a$10$ayC7NXp7qEIbJJb21ZZIe.a3f11IC/yEuOwWhrUuwK1TIP0ptITDq', 'USER'),
-- пароль "password1"
('user1', '{bcrypt}$2a$10$EAyb8FfYBSQ74VBRgicIRePvd8szh9HN6ccyeKT4bUjEBJGyVjSu.', 'USER'),
-- пароль "password2"
('user2', '{bcrypt}$2a$10$Aa/f6s83IxwRtjpsPyIl.uSr8UvAbuOhDkqTO2.lMdv5Ii2B8UUe6', 'USER'),
-- пароль "password3"
('user3', '{bcrypt}$2a$10$wQAB87dwsz5nnXDcHJhSgOvIL6BtwD6j0yiarlPmLj.TYSZDRlgO2', 'USER'),
-- пароль "password4"
('user4', '{bcrypt}$2a$10$6BvjbbgWwTy6/nRs/DN1dOnKFYKkvRNReuvKZ77YWbn6HEbb5Qxpy', 'USER'),
-- пароль "password5"
('user5', '{bcrypt}$2a$10$qpq1te9H6GAfnpX4dKAFGuJt0VR5BosNqUXNuMFhXWNUOo5mJqUaG', 'USER'),
-- пароль "admin123"
('admin', '{bcrypt}$2a$10$Pk.yhEYQ6XHEdvWz7aULXO1NuOBUxykZasRWzAyvTN9mfEuBvTgz2', 'ADMIN');
