DROP TABLE IF EXISTS REFERRAL_RESOLUTION;

CREATE TABLE REFERRAL_RESOLUTION
(
  RESOLUTION_ID           BIGSERIAL       NOT NULL,
  REFERRAL_ID             BIGINT          NOT NULL,
  RESOLUTION_STATUS       VARCHAR(255)    NOT NULL,
  RESOLUTION_DATE_TIME    TIMESTAMP       NOT NULL,

  CONSTRAINT REF_RES_PK        PRIMARY KEY (RESOLUTION_ID),
  CONSTRAINT REF_RES_REF_FK    FOREIGN KEY (REFERRAL_ID) REFERENCES OFFENDER_DELETION_REFERRAL(REFERRAL_ID)
);

COMMENT ON TABLE REFERRAL_RESOLUTION IS 'Records the outcome of a deletion referral';

COMMENT ON COLUMN REFERRAL_RESOLUTION.RESOLUTION_ID IS 'Primary key id';
COMMENT ON COLUMN REFERRAL_RESOLUTION.REFERRAL_ID IS 'The id of the referral';
COMMENT ON COLUMN REFERRAL_RESOLUTION.RESOLUTION_STATUS IS 'An enumeration of referral outcomes (e.g. DELETED, RETAINED)';
COMMENT ON COLUMN REFERRAL_RESOLUTION.RESOLUTION_DATE_TIME IS 'The timestamp of when the resolution was complete.';

CREATE INDEX REF_RES_RI_IDX ON REFERRAL_RESOLUTION(REFERRAL_ID);