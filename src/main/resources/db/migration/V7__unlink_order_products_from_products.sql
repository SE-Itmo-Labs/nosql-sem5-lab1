-- Product identifiers now refer to MongoDB.
DO $$
DECLARE
    constraint_name text;
BEGIN
    FOR constraint_name IN
        SELECT c.conname
        FROM pg_constraint c
        JOIN pg_attribute a ON a.attrelid = c.conrelid AND a.attnum = c.conkey[1]
        WHERE c.conrelid = 'order_products'::regclass
          AND c.contype = 'f'
          AND cardinality(c.conkey) = 1
          AND a.attname = 'product_id'
    LOOP
        EXECUTE format('ALTER TABLE order_products DROP CONSTRAINT %I', constraint_name);
    END LOOP;
END $$;
