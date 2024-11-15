CREATE TABLE CATEGORY_OF_TO_DO (
    CATEGORY_OF_TO_DO            VARCHAR2(4000),
    -- Additional columns for note and dates
    date_created           TIMESTAMP(9) WITH TIME ZONE DEFAULT systimestamp(9) NOT NULL,
    date_updated           TIMESTAMP(9) WITH TIME ZONE,
    date_created_or_updated TIMESTAMP(9) WITH TIME ZONE GENERATED ALWAYS AS ( coalesce(date_updated, date_created) ) VIRTUAL,
    CATEGORY_OF_TO_DO_id         RAW(16) DEFAULT sys_guid() PRIMARY KEY
);

-- Trigger to update date_updated for CATEGORY_OF_TO_DO
CREATE OR REPLACE TRIGGER set_date_updated_CATEGORY_OF_TO_DO
    BEFORE UPDATE ON CATEGORY_OF_TO_DO
    FOR EACH ROW
BEGIN
    :new.date_updated := systimestamp;
END;
/
