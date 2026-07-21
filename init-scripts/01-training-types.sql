CREATE TABLE IF NOT EXISTS training_type (
                                             id BIGSERIAL PRIMARY KEY,
                                             training_type_name VARCHAR(50) NOT NULL UNIQUE
);

INSERT INTO training_type (training_type_name) VALUES
                                                   ('Cardio'), ('Strength'), ('Yoga'), ('CrossFit'), ('Stretching')
ON CONFLICT (training_type_name) DO NOTHING;
