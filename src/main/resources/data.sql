INSERT INTO training_type (training_type_name)
SELECT 'Cardio'    WHERE NOT EXISTS (SELECT 1 FROM training_type WHERE training_type_name = 'Cardio');
INSERT INTO training_type (training_type_name)
SELECT 'Strength'  WHERE NOT EXISTS (SELECT 1 FROM training_type WHERE training_type_name = 'Strength');
INSERT INTO training_type (training_type_name)
SELECT 'Yoga'      WHERE NOT EXISTS (SELECT 1 FROM training_type WHERE training_type_name = 'Yoga');
INSERT INTO training_type (training_type_name)
SELECT 'CrossFit'  WHERE NOT EXISTS (SELECT 1 FROM training_type WHERE training_type_name = 'CrossFit');
INSERT INTO training_type (training_type_name)
SELECT 'Stretching' WHERE NOT EXISTS (SELECT 1 FROM training_type WHERE training_type_name = 'Stretching');