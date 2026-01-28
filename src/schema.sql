CREATE TABLE movie_tickets (
    id SERIAL PRIMARY KEY,
    title VARCHAR(150) NOT NULL UNIQUE,
    genre VARCHAR(50),
    duration INT CHECK (duration > 0),
    rating DOUBLE PRECISION CHECK (rating >= 0 AND rating <= 10),
    is_available BOOLEAN
);

INSERT INTO movie_tickets (title, genre, duration, rating, is_available)
VALUES
    ('Inception', 'Sci-Fi', 148, 8.8, true),
    ('Titanic', 'Drama', 195, 7.9, false);

select * from movie_tickets;
DROP TABLE movie_tickets;