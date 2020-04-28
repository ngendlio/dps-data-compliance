DROP TABLE IF EXISTS RETENTION_REASON_IMAGE_DUPLICATE;

CREATE TABLE RETENTION_REASON_IMAGE_DUPLICATE
(
  RET_REASON_IMG_DUP_ID             BIGSERIAL       NOT NULL,
  RETENTION_REASON_ID               BIGINT          NOT NULL,
  IMAGE_DUPLICATE_ID                BIGINT          NOT NULL,

  CONSTRAINT RET_REA_IMG_DUP_PK     PRIMARY KEY (RET_REASON_IMG_DUP_ID),
  CONSTRAINT RET_REA_IMG_DUP_RRI_FK FOREIGN KEY (RETENTION_REASON_ID) REFERENCES RETENTION_REASON(RETENTION_REASON_ID),
  CONSTRAINT RET_REA_IMG_DUP_IDI_FK FOREIGN KEY (IMAGE_DUPLICATE_ID) REFERENCES IMAGE_DUPLICATE(IMAGE_DUPLICATE_ID)
);

COMMENT ON TABLE RETENTION_REASON_IMAGE_DUPLICATE IS 'Links a retention reason to the image duplicate record';

COMMENT ON COLUMN RETENTION_REASON_IMAGE_DUPLICATE.RET_REASON_IMG_DUP_ID IS 'Primary key id';
COMMENT ON COLUMN RETENTION_REASON_IMAGE_DUPLICATE.RETENTION_REASON_ID IS 'The id of the parent retention reason';
COMMENT ON COLUMN RETENTION_REASON_IMAGE_DUPLICATE.IMAGE_DUPLICATE_ID IS 'The id of the image duplicate';

CREATE INDEX RET_REA_IMG_DUP_RRI_IDX ON RETENTION_REASON_IMAGE_DUPLICATE(RETENTION_REASON_ID);
CREATE INDEX RET_REA_IMG_DUP_IDI_IDX ON RETENTION_REASON_IMAGE_DUPLICATE(IMAGE_DUPLICATE_ID);