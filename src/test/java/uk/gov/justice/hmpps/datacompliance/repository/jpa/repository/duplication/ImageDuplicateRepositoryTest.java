package uk.gov.justice.hmpps.datacompliance.repository.jpa.repository.duplication;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.transaction.TestTransaction;
import uk.gov.justice.hmpps.datacompliance.repository.jpa.model.duplication.ImageDuplicate;

import javax.transaction.Transactional;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@SpringBootTest
@Transactional
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
class ImageDuplicateRepositoryTest {

    private static final LocalDateTime DATE_TIME = LocalDateTime.now();

    @Autowired
    private OffenderImageUploadRepository offenderImageUploadRepository;

    @Autowired
    private ImageDuplicateRepository repository;

    @Test
    @Sql("image_upload_batch.sql")
    @Sql("offender_image_upload.sql")
    void persistImageDuplicateAndRetrieveById() {

        final var imageDuplicate = ImageDuplicate.builder()
                .referenceOffenderImageUpload(offenderImageUploadRepository.findById(1L).orElseThrow())
                .duplicateOffenderImageUpload(offenderImageUploadRepository.findById(2L).orElseThrow())
                .detectionDateTime(DATE_TIME)
                .build();

        repository.save(imageDuplicate);
        assertThat(imageDuplicate.getImageDuplicateId()).isNotNull();

        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        final var retrievedEntity = repository.findById(imageDuplicate.getImageDuplicateId()).orElseThrow();
        assertThat(retrievedEntity.getDetectionDateTime()).isEqualTo(DATE_TIME);
        assertThat(retrievedEntity.getReferenceOffenderImageUpload().getUploadId()).isEqualTo(1L);
        assertThat(retrievedEntity.getDuplicateOffenderImageUpload().getUploadId()).isEqualTo(2L);
    }
}
