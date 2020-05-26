package uk.gov.justice.hmpps.datacompliance.jobs.offenderdeletion;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.hmpps.datacompliance.client.elite2api.Elite2ApiClient;
import uk.gov.justice.hmpps.datacompliance.config.OffenderDeletionConfig;
import uk.gov.justice.hmpps.datacompliance.repository.jpa.model.referral.OffenderDeletionBatch;
import uk.gov.justice.hmpps.datacompliance.repository.jpa.repository.referral.OffenderDeletionBatchRepository;
import uk.gov.justice.hmpps.datacompliance.utils.TimeSource;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OffenderDeletionTest {

    private static final long BATCH_ID = 123L;
    private static final LocalDateTime NOW = LocalDateTime.now();
    private static final LocalDateTime INITIAL_WINDOW_START = LocalDateTime.of(2020, 1, 2, 3, 4, 5);
    private static final Duration DURATION = Duration.ofDays(1);
    private static final int NONE_REMAINING_IN_WINDOW = 0;
    private static final int SOME_REMAINING_IN_WINDOW = 1;

    private static final OffenderDeletionConfig CONFIG = OffenderDeletionConfig.builder()
            .initialWindowStart(INITIAL_WINDOW_START)
            .windowLength(DURATION)
            .build();

    @Mock
    private Elite2ApiClient elite2ApiClient;

    @Mock
    private OffenderDeletionBatchRepository batchRepository;

    private OffenderDeletion offenderDeletion;

    @BeforeEach
    void setUp() {
        offenderDeletion = new OffenderDeletion(TimeSource.of(NOW), CONFIG, batchRepository, elite2ApiClient);
    }

    @Test
    void sendInitialDeletionRequest() {

        final var expectedBatch = batchWith(INITIAL_WINDOW_START);

        when(batchRepository.findFirstByOrderByRequestDateTimeDesc()).thenReturn(Optional.empty());
        when(batchRepository.save(expectedBatch)).thenReturn(expectedBatch.withBatchId(BATCH_ID));

        offenderDeletion.run();

        verify(elite2ApiClient).requestPendingDeletions(INITIAL_WINDOW_START, INITIAL_WINDOW_START.plus(DURATION), BATCH_ID);
    }

    @Test
    void sendSubsequentDeletionRequest() {

        final var expectedBatch = batchWith(INITIAL_WINDOW_START.plus(DURATION));

        when(batchRepository.findFirstByOrderByRequestDateTimeDesc()).thenReturn(Optional.of(
                completedBatchWith(INITIAL_WINDOW_START, NONE_REMAINING_IN_WINDOW)));
        when(batchRepository.save(expectedBatch)).thenReturn(expectedBatch.withBatchId(BATCH_ID));

        offenderDeletion.run();

        verify(elite2ApiClient).requestPendingDeletions(INITIAL_WINDOW_START.plusDays(1), INITIAL_WINDOW_START.plusDays(2), BATCH_ID);
    }

    @Test
    void useSameWindowForNextBatchIfRemainingOffendersInWindow() {

        final var expectedBatch = batchWith(INITIAL_WINDOW_START);

        when(batchRepository.findFirstByOrderByRequestDateTimeDesc()).thenReturn(Optional.of(
                completedBatchWith(INITIAL_WINDOW_START, SOME_REMAINING_IN_WINDOW)));
        when(batchRepository.save(expectedBatch)).thenReturn(expectedBatch.withBatchId(BATCH_ID));

        offenderDeletion.run();

        verify(elite2ApiClient).requestPendingDeletions(INITIAL_WINDOW_START, INITIAL_WINDOW_START.plus(DURATION), BATCH_ID);
    }

    @Test
    void offenderDeletionRequestFailsIfLastBatchDidNotComplete() {

        final var incompleteBatch = batchWith(INITIAL_WINDOW_START);
        incompleteBatch.setBatchId(BATCH_ID);

        when(batchRepository.findFirstByOrderByRequestDateTimeDesc()).thenReturn(Optional.of(incompleteBatch));

        assertThatThrownBy(() -> offenderDeletion.run())
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Previous referral (123) did not complete");
    }

    @Test
    void offenderDeletionRequestFailsIfStartDateInFuture() {

        when(batchRepository.findFirstByOrderByRequestDateTimeDesc()).thenReturn(Optional.of(
                completedBatchWith(NOW.plusSeconds(1), NONE_REMAINING_IN_WINDOW)));

        assertThatThrownBy(() -> offenderDeletion.run())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Deletion due date cannot be in the future, window start date is not valid");
    }

    @Test
    void offenderDeletionRequestFailsIfEndDateInFuture() {

        when(batchRepository.findFirstByOrderByRequestDateTimeDesc()).thenReturn(Optional.of(
                completedBatchWith(NOW.minusDays(2).plusSeconds(1), NONE_REMAINING_IN_WINDOW)));

        assertThatThrownBy(() -> offenderDeletion.run())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Deletion due date cannot be in the future, window end date is not valid");
    }

    @Test
    void offenderDeletionRequestFailsIfWindowDatesIllogical() {

        final var badConfig = OffenderDeletionConfig.builder()
                .initialWindowStart(INITIAL_WINDOW_START)
                .windowLength(Duration.ofDays(-1))
                .build();

        offenderDeletion = new OffenderDeletion(TimeSource.of(NOW), badConfig, batchRepository, elite2ApiClient);
        when(batchRepository.findFirstByOrderByRequestDateTimeDesc()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> offenderDeletion.run())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Deletion due window dates are illogical");
    }

    private OffenderDeletionBatch batchWith(final LocalDateTime windowStart) {
        return OffenderDeletionBatch.builder()
                .requestDateTime(NOW)
                .windowStartDateTime(windowStart)
                .windowEndDateTime(windowStart.plus(DURATION))
                .build();
    }

    private OffenderDeletionBatch completedBatchWith(final LocalDateTime windowStart, final int remainingInWindow) {
        final var batch = batchWith(windowStart);
        batch.setRemainingInWindow(remainingInWindow);
        batch.setReferralCompletionDateTime(NOW);
        return batch;
    }
}
