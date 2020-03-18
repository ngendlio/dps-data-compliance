DROP TABLE IF EXISTS MANUAL_RETENTION;

CREATE TABLE MANUAL_RETENTION
(
  MANUAL_RETENTION_ID           BIGSERIAL       NOT NULL,
  OFFENDER_NO                   VARCHAR( 10)    NOT NULL,
  RETENTION_DATE_TIME           TIMESTAMP       NOT NULL,
  STAFF_ID                      BIGINT          NOT NULL,

  CONSTRAINT MAN_RET_PK         PRIMARY KEY (MANUAL_RETENTION_ID)
);

COMMENT ON TABLE MANUAL_RETENTION IS 'Records a new/updated request by a member of staff to retain an offender record';

COMMENT ON COLUMN MANUAL_RETENTION.MANUAL_RETENTION_ID IS 'Primary key id';
COMMENT ON COLUMN MANUAL_RETENTION.OFFENDER_NO IS 'The NOMS offender number';
COMMENT ON COLUMN MANUAL_RETENTION.RETENTION_DATE_TIME IS 'The timestamp of this retention creation/update';
COMMENT ON COLUMN MANUAL_RETENTION.STAFF_ID IS 'The unique staff identifier of the member of staff that requested the retention';

CREATE INDEX MAN_RET_ON_IDX ON MANUAL_RETENTION(OFFENDER_NO);
