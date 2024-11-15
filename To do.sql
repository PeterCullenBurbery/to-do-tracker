CREATE TABLE to_do (
    to_do                   VARCHAR2
    (4000) NOT NULL UNIQUE,
    done                    NUMBER(
    1) DEFAULT 0 CHECK ( done IN ( 0
    , 1 ) ) NOT NULL,
    -- Additional columns for note and dates
    date_created            TIMESTAMP
    (9) WITH TIME ZONE DEFAULT systimestamp
    (9) NOT NULL,
    date_updated            TIMESTAMP
    (9) WITH TIME ZONE,
        date_created_or_updated TIMESTAMP
        (9) WITH TIME ZONE GENERATED
        ALWAYS AS ( coalesce(date_updated
        , date_created) ) VIRTUAL,
    to_do_id                RAW(16)
    DEFAULT sys_guid() PRIMARY KEY
);

-- Trigger to update date_updated for TO_DO
CREATE OR REPLACE TRIGGER set_date_updated_to_do
BEFORE
    UPDATE ON to_do
    FOR EACH ROW
BEGIN
    :new.date_updated := systimestamp
    ;
END;
/