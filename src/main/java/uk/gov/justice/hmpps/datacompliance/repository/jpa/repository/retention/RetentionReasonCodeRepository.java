package uk.gov.justice.hmpps.datacompliance.repository.jpa.repository.retention;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.datacompliance.repository.jpa.model.retention.manual.RetentionReasonCode;
import uk.gov.justice.hmpps.datacompliance.repository.jpa.model.retention.manual.RetentionReasonCode.Code;

@Repository
public interface RetentionReasonCodeRepository extends CrudRepository<RetentionReasonCode, Code> {
}
