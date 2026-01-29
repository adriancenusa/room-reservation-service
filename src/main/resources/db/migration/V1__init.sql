CREATE EXTENSION IF NOT EXISTS btree_gist;

CREATE TABLE reservation (
                             id UUID PRIMARY KEY,
                             reservation_id CHAR(8) NOT NULL UNIQUE,

                             customer_name VARCHAR(200) NOT NULL,
                             room_number INT NOT NULL,

                             start_date DATE NOT NULL,
                             end_date DATE NOT NULL,

                             room_segment VARCHAR(20) NOT NULL,
                             payment_mode VARCHAR(20) NOT NULL,
                             payment_reference VARCHAR(120),

                             status VARCHAR(30) NOT NULL,

                             created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                             updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

                             CONSTRAINT chk_date_range CHECK (end_date > start_date)
);

ALTER TABLE reservation
    ADD CONSTRAINT reservation_no_overlap
    EXCLUDE USING gist (
    room_number WITH =,
    daterange(start_date, end_date, '[)') WITH &&
  );

CREATE INDEX idx_reservation_status_start_date ON reservation (status, start_date);
CREATE INDEX idx_reservation_room_number ON reservation (room_number);

CREATE TABLE processed_payment_event (
                                         payment_id VARCHAR(100) PRIMARY KEY,
                                         processed_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
