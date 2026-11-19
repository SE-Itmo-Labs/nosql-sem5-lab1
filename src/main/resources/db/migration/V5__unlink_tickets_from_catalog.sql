-- Catalog references now point to MongoDB identifiers.
DO $$
DECLARE
    constraint_name text;
BEGIN
    FOR constraint_name IN
        SELECT DISTINCT c.conname
        FROM pg_constraint c
        JOIN pg_attribute a ON a.attrelid = c.conrelid AND a.attnum = ANY(c.conkey)
        WHERE c.conrelid = 'tickets'::regclass
          AND c.contype = 'f'
          AND a.attname IN ('screening_id', 'seat_id')
    LOOP
        EXECUTE format('ALTER TABLE tickets DROP CONSTRAINT %I', constraint_name);
    END LOOP;
END $$;
