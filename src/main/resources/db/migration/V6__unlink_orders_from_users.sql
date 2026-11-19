-- Users are stored in MongoDB; orders retain the user's numeric ID.
DO $$
DECLARE
    constraint_name text;
BEGIN
    FOR constraint_name IN
        SELECT c.conname
        FROM pg_constraint c
        JOIN pg_attribute a ON a.attrelid = c.conrelid AND a.attnum = c.conkey[1]
        WHERE c.conrelid = 'cinema_orders'::regclass
          AND c.contype = 'f'
          AND cardinality(c.conkey) = 1
          AND a.attname = 'user_id'
    LOOP
        EXECUTE format('ALTER TABLE cinema_orders DROP CONSTRAINT %I', constraint_name);
    END LOOP;
END $$;
