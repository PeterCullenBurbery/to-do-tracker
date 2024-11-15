CREATE TABLE to_do_many_to_many (
    -- the not null constraint is important
    to_do_id                RAW(16)
        REFERENCES to_do ( to_do_id )
    NOT NULL,
    category_of_to_do_id    RAW(16)
        REFERENCES category_of_to_do ( category_of_to_do_id )
    NOT NULL,
    -- we add 2 foreign key constraints for many-to-many relationship
    -- we add a unique constraint
    UNIQUE ( to_do_id,
             category_of_to_do_id ),
    -- Additional columns for note and dates
    date_created            TIMESTAMP(9) WITH TIME ZONE DEFAULT systimestamp
    (9) NOT NULL,
    date_updated            TIMESTAMP(9) WITH TIME ZONE,
        date_created_or_updated TIMESTAMP(9) WITH TIME ZONE GENERATED ALWAYS
        AS ( coalesce(date_updated, date_created) ) VIRTUAL,
    to_do_many_to_many_id   RAW(16) DEFAULT sys_guid() PRIMARY KEY
);

CREATE OR REPLACE TRIGGER trigger_set_date_updated_to_do_many_to_many BEFORE
    UPDATE ON to_do_many_to_many
    FOR EACH ROW
BEGIN
    :new.date_updated := systimestamp;
END;
/