
CREATE TABLE student_address
(
    id         SERIAL PRIMARY KEY,
    student_id INT,
    address    VARCHAR(100)
);

CREATE TABLE student_address_update
(
    id         INT,
    student_id INT,
    address    VARCHAR(100),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE OR REPLACE FUNCTION prevent_address_update()
    RETURNS TRIGGER AS
$$
BEGIN
    INSERT INTO student_address_update (id, student_id, address)
    VALUES (NEW.id, NEW.student_id, NEW.address);
    --     RAISE EXCEPTION 'Updates to student_address table are not allowed.';
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER prevent_address_update_trigger
    BEFORE UPDATE
    ON student_address
    FOR EACH ROW
EXECUTE FUNCTION prevent_address_update();

INSERT INTO student_address (student_id, address)
VALUES (1, 'Address 1'),
       (2, 'Address 2');

UPDATE student_address
SET address = 'New Address 1'
WHERE student_id = 1;
